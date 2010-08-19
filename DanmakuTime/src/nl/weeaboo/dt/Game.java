package nl.weeaboo.dt;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2ES1;

import net.java.games.input.Controller;
import nl.weeaboo.common.GraphicsUtil;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.dt.audio.HardSyncSoundEngine;
import nl.weeaboo.dt.audio.ISoundEngine;
import nl.weeaboo.dt.audio.SoftSyncSoundEngine;
import nl.weeaboo.dt.field.Field;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.input.IKeyConfig;
import nl.weeaboo.dt.input.Input;
import nl.weeaboo.dt.input.InputBuffer;
import nl.weeaboo.dt.input.JoyInput;
import nl.weeaboo.dt.input.KeyConfig;
import nl.weeaboo.dt.input.Keys;
import nl.weeaboo.dt.io.IPersistentStorage;
import nl.weeaboo.dt.io.IPersistentStorageFactory;
import nl.weeaboo.dt.io.PersistentStorageFactory;
import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;
import nl.weeaboo.dt.lua.LuaThreadPool;
import nl.weeaboo.dt.lua.LuaUtil;
import nl.weeaboo.dt.lua.link.LuaFunctionLink;
import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.lua.platform.LuaPlatform;
import nl.weeaboo.dt.lua.platform.LuajavaLib;
import nl.weeaboo.dt.netplay.ClientNetworkState;
import nl.weeaboo.dt.netplay.ServerNetworkState;
import nl.weeaboo.dt.renderer.IRenderer;
import nl.weeaboo.dt.renderer.ITextureStore;
import nl.weeaboo.dt.renderer.Renderer;
import nl.weeaboo.dt.renderer.TextureStore;
import nl.weeaboo.dt.replay.IReplay;
import nl.weeaboo.dt.replay.IReplayPlayback;
import nl.weeaboo.dt.replay.Replay;
import nl.weeaboo.dt.replay.ReplayPlayback;
import nl.weeaboo.game.GameBase;
import nl.weeaboo.game.ResourceManager;
import nl.weeaboo.game.gl.GLManager;
import nl.weeaboo.game.gl.Screenshot;
import nl.weeaboo.game.gl.ScreenshotManager;
import nl.weeaboo.game.input.UserInput;
import nl.weeaboo.game.text.MutableTextStyle;
import nl.weeaboo.game.text.ParagraphRenderer;
import nl.weeaboo.game.text.layout.ParagraphLayouter;

import org.luaj.vm.LFunction;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

public class Game extends GameBase {
	
	private boolean runtimeScriptsChanged;
	private boolean error;
	private Notifier notifier;
	private GameVideoCapture videoCapture;
	private ScreenshotManager screenshotManager;
	
	private Keys keys;
	private List<JoyInput> joyInputs;
	private IKeyConfig keyConfig;
	private IPersistentStorageFactory storageFactory;
	private IPersistentStorage storage;
	private IReplayPlayback replayPlayback;
	private IReplay replay;
	private ITextureStore texStore;	
	private ISoundEngine soundEngine;
	
	private LuaRunState luaRunState;
	private boolean paused, pauseRequest;
	private LuaLink pauseThread;
	
	private long randomSeed;
	private long frame;
	private int framesPerDraw;
	private InputBuffer inputBuffer;
	private int inputLag = 6;
	private final int maxInputLag = 15;
	private ClientNetworkState networkState;
	private ServerNetworkState server;
	
	public Game(Config c, ResourceManager rm, GameFrame gf) {
		super(c, rm, gf);
		
		videoCapture = new GameVideoCapture(this);
		inputBuffer = new InputBuffer(maxInputLag);
		storageFactory = new PersistentStorageFactory(this);
	}
	
	//Functions
	public void stopNetGame() {
		if (server != null) {
			server.stop();
			server = null;
		}
		if (networkState != null) {
			networkState.stop();
			networkState = null;
		}
	}
	
	public void hostNetGame(int numPlayers, int tcpPort) throws IOException {
		if (runtimeScriptsChanged) throw new IOException("Scripts changed at runtime => netplay disallowed");
		
		GameEnv genv = GameEnv.fromGame(this);		
		server = new ServerNetworkState(genv, numPlayers);
		server.host(tcpPort);
	}

	public void joinNetGame(String targetAddress, int targetTCPPort,
			int localUDPPort) throws IOException
	{
		if (runtimeScriptsChanged) throw new IOException("Scripts changed at runtime => netplay disallowed");

		InetAddress addr = InetAddress.getLocalHost();
		if (!targetAddress.equals("localhost")) {
			addr = InetAddress.getByName(targetAddress);
		}
		
		GameEnv genv = GameEnv.fromGame(this);		
		networkState = new ClientNetworkState(genv, inputBuffer, maxInputLag);
		networkState.join(addr, targetTCPPort, localUDPPort);
	}
	
	public static void loadConfig(Config config, ResourceManager rm) throws IOException {
		String configFilename = "save/prefs.xml";

		config.load(rm.getInputStream("prefs.default.xml"));
		if (rm.getFileExists(configFilename)) {
			InputStream in = null;
			try {
				in = new BufferedInputStream(rm.getInputStream(configFilename));
				config.load(in);
			} finally {
				if (in != null) in.close();
			}
		}
	}
		
	public void loadResources() {
		super.loadResources();
						
		ResourceManager rm = getResourceManager();
		screenshotManager = new ScreenshotManager(this);
		notifier = new Notifier(createParagraphRenderer());
		
		OutputStream err = null;
		try {
			rm.getOutputStream("save/err.txt");
		} catch (IOException e) {
			DTLog.warning(e);
		}
		
		DTLog.getInstance().start(isDebug(), notifier, err);
			
		keys = new Keys(KeyEvent.class);
		joyInputs = new ArrayList<JoyInput>();
		for (Controller con : JoyInput.getControllers()) {
			joyInputs.add(new JoyInput(joyInputs.size()+1, con));
		}
		
		randomSeed = System.nanoTime();
		storage = storageFactory.createPersistentStorage();
		
		reset();
		
		saveConfig();
	}
	
	public void unloadResources() {
		videoCapture.stop();
		saveConfig();
		
		super.unloadResources();
	}
	
	protected void saveConfig() {
		OutputStream out = null;
		try {
			out = getOutputStream("save/prefs.xml");
			getConfig().save(out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException ioe) { }
		}		
	}
	
	private void reset() {
		if (luaRunState != null) {
			luaRunState.dispose();
			luaRunState = null;
		}
		if (soundEngine != null) {
			soundEngine.stopAll();
		}

		if (networkState == null) {
			inputLag = 0;			
		}
		
		runtimeScriptsChanged = false;
		error = false;
		paused = pauseRequest = false;
		pauseThread = null;
		screenshotManager.clear();
		keyConfig = null;
		texStore = null;
		soundEngine = null;
		replayPlayback = null;
		framesPerDraw = 1;
	}
	
	public void startGame(String mainFuncName) {
		restart(mainFuncName);
		
		if (replayPlayback == null && !runtimeScriptsChanged) {
			try {
				replay = startReplay(GameEnv.fromGame(this), mainFuncName);
			} catch (IOException e) {
				DTLog.warning(e);
			}
		}
	}
	
	public void startGameReplay(String path) throws IOException {
		reset();
		
		if (!runtimeScriptsChanged) {
			IReplay replay = loadReplay(path);
			
			replayPlayback = new ReplayPlayback(storageFactory);
			replayPlayback.start(replay);
			
			GameEnv genv = replayPlayback.getGameEnv();
			randomSeed = genv.getRandomSeed();
			
			restart0(replayPlayback.getMainFuncName());
		} else {
			restart0("main");
		}
	}
	
	public LuaLink restart(String mainFuncName) {
		reset();
		
		return restart0(mainFuncName);
	}
	
	protected LuaLink restart0(String mainFuncName) {		
		ResourceManager rm = getResourceManager();
		int width = getWidth();
		int height = getHeight();
		
		IPersistentStorage ps = storage;
		if (replayPlayback != null) {
			ps = replayPlayback.getStorage();
		}
		
		keyConfig = createKeyConfig(keys);
		texStore = createTextureStore();
		soundEngine = createSoundEngine();
		
		Map<Integer, IField> fieldMap = new HashMap<Integer, IField>();
		fieldMap.put(0, new Field(0, 0, width, height, 0)); //Full-screen field (0)
		fieldMap.put(1, new Field(0, 0, width, height, 0)); //Game field (1)
		fieldMap.put(999, new Field(0, 0, width, height, 0)); //Pause field (2)
		
		//Init Lua
		LuaPlatform platform = new LuaPlatform() {
			@Override
			public InputStream openFile(String fileName) {
				return null;
			}
		};
		LuaThreadPool threadPool = new LuaThreadPool();
		
		luaRunState = new LuaRunState(randomSeed, platform, keys,
				threadPool, fieldMap, texStore, soundEngine, ps);
		
		LuaState vm = getLuaState();
		
		//Compile code
		SortedSet<String> scripts = new TreeSet<String>(new Comparator<String>() {
			Collator c = Collator.getInstance(Locale.ROOT);
			public int compare(String o1, String o2) {
				//Sort scripts by their paths
				return c.compare(o1, o2);
			}	
		});
		scripts.addAll(rm.getFolderContents("script"));
		
		for (String path : scripts) {
			InputStream in = null;
			try {				
				in = rm.getInputStream(path);
				LuaUtil.loadModule(vm, path, in);
				LuaUtil.initModule(vm, path);
			} catch (Exception e) {
				DTLog.showError(e);
				error = true;
				return null;
			} finally {
				try {
					if (in != null) in.close();
				} catch (IOException ioe) { }
			}
		}

		//Install default functions
		vm._G.put("pause", luaPauseFunc());
		vm._G.put("screenshot", luaScreenshotFunc());
		vm._G.put("saveReplay", luaSaveReplayFunc());
		vm._G.put("globalReset", luaResetFunc());
		vm._G.put("startGame", luaStartGameFunc());
		vm._G.put("joinNetGame", luaJoinNetGameFunc());
		vm._G.put("hostNetGame", luaHostNetGameFunc());
		vm._G.put("startReplay", luaStartReplayFunc());
		vm._G.put("quit", luaQuitFunc());
		
		//Start main thread
		LuaLink mainThread = new LuaFunctionLink(luaRunState, vm, mainFuncName);
		threadPool.add(mainThread);
				
		error = false;
		return mainThread;
	}
	
	void addRuntimeScript(String scriptFilename, String script)
		throws LuaException, IOException
	{
		stopNetGame();
		if (replay != null) replay = null;
		if (replayPlayback != null) replayPlayback = null;
		runtimeScriptsChanged = true;
		
		LuaState vm = luaRunState.vm;
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(script.getBytes("UTF-8"));
			LuaUtil.loadModule(vm, scriptFilename, in);
			LuaUtil.initModule(vm, scriptFilename);
		} finally {
			if (in != null) in.close();
		}
	}
	
	public void update(UserInput gameInput, float dt) {		
		super.update(gameInput, dt);

		notifier.update(Math.round(1000f * dt));

		if (replayPlayback != null) {
			//Control replay speed
			if (gameInput.consumeKey(KeyEvent.VK_PAGE_UP)) {
				framesPerDraw++;
			} else if (gameInput.consumeKey(KeyEvent.VK_PAGE_DOWN)) {
				framesPerDraw--;
			}
			
			if (gameInput.consumeKey(KeyEvent.VK_ESCAPE)) {
				restart("main");
				return;
			}
			
			framesPerDraw = Math.max(0, Math.min(30, framesPerDraw));			
		} else if (networkState != null) {
			//Control input lag
			if (gameInput.consumeKey(KeyEvent.VK_PAGE_UP)) {
				inputLag++;
			} else if (gameInput.consumeKey(KeyEvent.VK_PAGE_DOWN)) {
				inputLag--;
			}
			inputLag = Math.max(1, Math.min(maxInputLag, inputLag));					
		}
		
		for (int n = 0; n < framesPerDraw; n++) {
			IInput futureInput;		
			if (replayPlayback != null && replayPlayback.hasNextFrame()) {
				futureInput = replayPlayback.nextFrame();
			} else {				
				futureInput = new Input(gameInput.copy());
				for (JoyInput joypadInput : joyInputs) {
					joypadInput.update(futureInput);
				}
			}
			
			updateFrame(futureInput);
		}
	}
	
	protected void updateFrame(IInput futureInput) {
		networkReceive();
		
		if (server != null && !server.isGameStarted()) {
			return;
		}

		if (networkState == null) {
			if (frame == 0) {
				restart("main");
			}			
		} else {			
			//Check if network ready
			if (!networkState.isGameStarted()) {
				return;
			} else if (frame == 0) {
				if (!isHostingNetworkGame()) {
					//We received the other player's save file. Use a special subclass to
					//avoid overwriting any of our own save files with theirs.
					storage = storageFactory.createNonPersistentStorage(networkState.getPersistentStorage());
				} else {
					//We've received our own save file. We can just use that...
					storage = storageFactory.createPersistentStorage(networkState.getPersistentStorage());
				}
				randomSeed = networkState.getRandomSeed();

				restart("main");				
			}
			
			//Make sure the input is available
			final long newInputReadFrame = frame - inputLag + 1;
			if (newInputReadFrame > 0 && !inputBuffer.hasReceived(newInputReadFrame)) {
				long frameTime = 1000000000L / getConfig().graphics.getFPS();
				long t0 = System.nanoTime();
				try {
					networkSend();

					do {
						networkReceive();
						Thread.sleep(1);
						
						if (System.nanoTime()-t0 >= frameTime) {
							//Wait up to frametime at once
							break;
						}
					} while (!inputBuffer.hasReceived(newInputReadFrame)); 
				} catch (InterruptedException e) {				
				}				
					
				if (!inputBuffer.hasReceived(newInputReadFrame)) {
					return;
				}
			}
		}
				
		frame++;
		inputBuffer.setLocalFrame(frame);

		//Update paused state
		paused = pauseRequest;
		if (!paused) {
			pauseThread = null;
		}

		soundEngine.update(paused ? 0 : 1);
		
		//Update input		
		int futureKeysPressed[] = futureInput.getKeysPressed();
		int futureKeysHeld[] = futureInput.getKeysHeld();
		int futureVKeysPressed[] = keyConfig.getVKeysPressed(futureInput);
		int futureVKeysHeld[] = keyConfig.getVKeysHeld(futureInput);
		
		if (networkState == null) {
			//If non-networked, set the virtual keys in the input object
			for (int key : futureVKeysPressed) futureInput.setKeyPressed(key);
			for (int key : futureVKeysHeld) futureInput.setKeyHeld(key);

			inputBuffer.addKeys(frame, 0, futureInput);
		} else {
			networkState.buffer(frame, futureKeysPressed, futureKeysHeld,
					futureVKeysPressed, futureVKeysHeld);			
			networkSend();
		}
		
		IInput ii = (frame-inputLag > 1 ? inputBuffer.get(frame-inputLag) : new Input());
		if (replay != null) {
			replay.addFrame(ii);
		}
		if (ii.consumeKey(KeyEvent.VK_F5)) {
			restart("main");
			return;
		}								
		if (ii.consumeKey(KeyEvent.VK_F7)) {
			//Screen capture activation key
			screenshotManager.add(new Screenshot(0, 0, getWidth(), getHeight()) {
				public void set(int argb[], int w, int h) {
					try {
						BufferedImage img = GraphicsUtil.createBufferedImage(w, h, argb);
						ImageIO.write(img, "png", new File("capture-" + System.currentTimeMillis() + ".png"));
						DTLog.message("Screenshot saved");
					} catch (IOException e) {
						DTLog.showError(e);
					}					
				}
			});
		}
		
		videoCapture.update(ii);
		
		//Early out
		if (replayPlayback != null && !replayPlayback.hasNextFrame()) {
			return;
		}		
		if (error) {
			return;
		}

		LuaState vm = getLuaState();

		//global IInput input
		vm.pushlvalue(LuajavaLib.toUserdata(ii, ii.getClass()));
		vm.setglobal("input");
				
		luaRunState.update(ii, paused);
		
		if (paused) {
			try {
				pauseThread.update();
			} catch (LuaException e) {
				DTLog.warning(e);
				unpause();
			}
			
			if (pauseThread == null || pauseThread.isFinished()) {
				unpause();
			}
		}
	}
	
	public void draw(GLManager glm) {	
		int w = getWidth();
		int h = getHeight();
		
		Renderer r = (Renderer)createRenderer(glm);
		if (luaRunState != null) {
			luaRunState.draw(r);
		}
		r.flush();
				
		screenshotManager.update(glm);
									
		//Draw HUD
		if (isDebug() && luaRunState != null) {
			ParagraphRenderer pr = createParagraphRenderer();
			pr.setBounds(2, -2, w-6, h-4);
			
			MutableTextStyle mts = pr.getDefaultStyle().mutableCopy();
			mts.setAnchor(9);
			pr.setDefaultStyle(mts.immutableCopy());
			
			String hudText = String.format("%.2f FPS\n%d Objects\n",
					getFPS(), luaRunState.getObjectCount());
			pr.drawText(glm, hudText);			
		}
		
		//Draw lag
		if (networkState != null) {
			StringBuilder sb = new StringBuilder("[netplay]\n");
			sb.append("delay: " + inputLag + " / " + maxInputLag + "\n");
			sb.append(inputBuffer.getDelayString() + "\n");
			
			ParagraphRenderer pr = createParagraphRenderer();
			pr.setBounds(2, h-12*3, w, 0);
			MutableTextStyle mts = pr.getDefaultStyle().mutableCopy();
			mts.setFontSize(10);
			pr.setDefaultStyle(mts.immutableCopy());
			
			pr.drawText(glm, sb.toString());
		}
		
		//Draw connecting
		if ((server != null && !server.isGameStarted())
				|| (networkState != null && !networkState.isGameStarted()))
		{
			ParagraphRenderer pr = createParagraphRenderer();
			pr.setBounds(10, h-100, w-20, 25);
			pr.drawText(glm, "Waiting for players" + StringUtil.repeatString(".", (int)(System.currentTimeMillis()/1000)%4));			
		}

		videoCapture.updateGL(glm);
		notifier.draw(glm, w, h);
		
		super.draw(glm);
		
		//Set FBO parameters
		int fboTex = glm.getFBOTexturePtr();
		if (fboTex != 0) {
			GL2ES1 gl = glm.getGL();
			gl.glBindTexture(GL.GL_TEXTURE_2D, fboTex);
			gl.glTexParameteri(GL2ES1.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
			glm.setTexture(null);
		}
	}
	
	protected void networkReceive() {
		if (server != null) {
			try {
				server.update();
			} catch (IOException e) {
				DTLog.error(e);
				server.stop();
				server = null;
				error = true;
			}
		}
		if (networkState != null) {
			try {
				networkState.receive();
			} catch (IOException e) {
				DTLog.error(e);
				networkState.stop();
				networkState = null;
				error = true;
			}
		}
	}
			
	protected void networkSend() {
		if (networkState != null) {
			networkState.send();
		}
	}
	
	public ParagraphRenderer createParagraphRenderer(ParagraphLayouter layouter) {	
		ParagraphRenderer pr = super.createParagraphRenderer(layouter);
		
		MutableTextStyle mts = pr.getDefaultStyle().mutableCopy();
		mts.setFontName(getConfig().game.getDefaultFont());
		mts.setFontSize(16);
		pr.setDefaultStyle(mts.immutableCopy());
		
		return pr;
	}	
	
	protected IKeyConfig createKeyConfig(Keys k) {
		KeyConfig conf = null;
		
		InputStream in = null;
		try {
			if (getFileExists("save/keyconfig.ini")) {
				in = new BufferedInputStream(getInputStream("save/keyconfig.ini"));
			} else if (getFileExists("keyconfig.ini")) {
				in = new BufferedInputStream(getInputStream("keyconfig.ini"));
			}
			
			if (in != null) {
				conf = KeyConfig.load(k, in);
			}
		} catch (IOException e) {
			DTLog.warning(e);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) { }
		}
		
		if (conf == null) {
			conf = new KeyConfig(k);
		}
		

		if (!getFileExists("save/keyconfig.ini")) {
			//Write user-modifiable keyconfig to save folder if there was no
			//keyconfig file there already
			try {
				OutputStream out = getOutputStream("save/keyconfig.ini");
				conf.save(out);
				out.close();
			} catch (IOException e) {
				DTLog.warning(e);
			}
		}
		
		return conf;
	}
	
	protected ITextureStore createTextureStore() {
		return new TextureStore(getImageStore());		
	}
	
	protected ISoundEngine createSoundEngine() {
		if (getConfig().audio.isHardSync()) {
			return new HardSyncSoundEngine(getSoundManager(), getConfig().graphics.getFPS());
		} else {
			return new SoftSyncSoundEngine(getSoundManager(), getConfig().graphics.getFPS());
		}
	}
			
	protected IRenderer createRenderer() {
		return createRenderer(null);
	}
	protected IRenderer createRenderer(GLManager glm) {
		int w = getWidth();
		int h = getHeight();
		int rw = getRealWidth();
		int rh = getRealHeight();
				
		Renderer r = new Renderer(glm, createParagraphRenderer(), w, h, rw, rh);
		return r;
	}
	
	protected IReplay startReplay(GameEnv genv, String mainFuncName) {
		return new Replay(genv, mainFuncName);
	}
	
	public IReplay loadReplay(String path) throws IOException {
		return Replay.fromInputStream(getInputStream("save/replay/" + path + ".rpy"));
	}
	
	public boolean saveReplay(String path) {
		if (replay == null) return false;
		
		OutputStream out = null;
		try {
			out = getOutputStream("save/replay/" + path + ".rpy");
			replay.save(out);
		} catch (IOException ioe) {
			DTLog.warning(ioe);
			return false;
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException ioe2) { }
		}

		return true;
	}
	
	public DelayedScreenshot screenshot(double x, double y, double w, double h,
			int blurMagnitude)
	{
		DelayedScreenshot ss;
		if (blurMagnitude > 1) {
			ss = new BlurringScreenshot(this, x, y, w, h, blurMagnitude);
		} else {
			ss = new DelayedScreenshot(this, x, y, w, h);
		}
		screenshotManager.add(ss);
		return ss;
	}
	
	protected LFunction luaPauseFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				LFunction func = vm.checkfunction(1);
				LValue args[] = new LValue[vm.gettop()-1];
				for (int n = 0; n < args.length; n++) {
					args[n] = vm.topointer(2+n);
				}
				
				pause();
				
				pauseThread = new LuaFunctionLink(luaRunState, luaRunState.vm, func, args);
				
				vm.resettop();
				return 0;
			}
		};		
	}
	
	protected LFunction luaScreenshotFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				int x = (vm.isnumber(1) ? vm.tointeger(1) : 0);
				int y = (vm.isnumber(2) ? vm.tointeger(2) : 0);
				int w = (vm.isnumber(3) ? vm.tointeger(3) : getWidth());
				int h = (vm.isnumber(4) ? vm.tointeger(4) : getHeight());
				
				int blurMagnitude = 4;
				if (vm.isnumber(5)) {
					blurMagnitude = vm.tointeger(5);
				}
				
				IRenderer r = createRenderer();
				Point p0 = new Point(r.virtualToReal(x, y));
				Point p1 = new Point(r.virtualToReal(x + w, y + h));
				Rectangle take = new Rectangle(
						Math.min(p0.x, p1.x), Math.min(p0.y, p1.y),
						Math.abs(p1.x - p0.x), Math.abs(p1.y - p0.y));
				
				vm.resettop();
				DelayedScreenshot ds = screenshot(take.x, take.y,
						take.width, take.height, blurMagnitude);
				vm.pushlvalue(LuajavaLib.toUserdata(ds, ds.getClass()));
				return 1;
			}
		};		
	}
	
	protected LFunction luaResetFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				String funcName = (vm.isstring(1) ? vm.tostring(1) : "main");
				vm.resettop();
				
				restart(funcName);
				return 0;
			}
		};		
	}
	
	protected LFunction luaStartGameFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				String funcName = (vm.isstring(1) ? vm.tostring(1) : "start");
				vm.resettop();
				
				startGame(funcName);
				return 0;
			}
		};		
	}
	
	protected LFunction luaStartReplayFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				boolean res = false;
				if (vm.isstring(1)) {
					try {
						startGameReplay(vm.tostring(1));
						res = true;
					} catch (IOException e) {
						DTLog.warning(e);
					}
				}
				vm.resettop();
				vm.pushboolean(res);
				return 1;
			}
		};		
	}
	
	protected LFunction luaQuitFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				dispose();
				return 0;
			}
		};		
	}

	protected LFunction luaSaveReplayFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				boolean res = false;
				if (vm.isstring(1)) {
					res = saveReplay(vm.tostring(1));
				}				
				vm.resettop();
				vm.pushboolean(res);
				return 1;
			}
		};		
	}

	protected LFunction luaJoinNetGameFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				String addr = vm.tostring(1);
				int targetTCPPort = vm.tointeger(2);
				int localUDPPort = vm.tointeger(3);
				vm.resettop();
				try {
					stopNetGame();					
					inputBuffer.clear();
					frame = 0;

					joinNetGame(addr, targetTCPPort, localUDPPort);
					return 0;
				} catch (IOException ioe) {
					DTLog.warning(ioe);
					vm.pushstring(ioe.toString());
					return 1;
				}
			}
		};		
	}

	protected LFunction luaHostNetGameFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				int numPlayers = vm.tointeger(1);
				String addr = vm.tostring(2);
				int port = vm.tointeger(3);
				vm.resettop();
				try {
					stopNetGame();
					inputBuffer.clear();
					frame = 0;
					
					hostNetGame(numPlayers, port);
					joinNetGame(addr, port, port);
					return 0;
				} catch (IOException ioe) {
					DTLog.warning(ioe);
					vm.pushstring(ioe.toString());
					return 1;					
				}
			}
		};		
	}
	
	public void pause() {
		pauseRequest = true;
	}
	
	public void unpause() {
		pauseRequest = false;
	}
		
	//Getters
	protected boolean isHostingNetworkGame() {
		return server != null;
	}
	public Config getConfig() {
		return (Config)super.getConfig();
	}
	public LuaRunState getLuaRunState() {
		return luaRunState;
	}
	public LuaState getLuaState() {
		return luaRunState.vm;
	}
	public float getFPS() {
		return getGameFrame().getCurrentFPS();
	}	
	public IPersistentStorageFactory getPersistentStorageFactory() {
		return storageFactory;
	}
	
	//Setters
	
}
