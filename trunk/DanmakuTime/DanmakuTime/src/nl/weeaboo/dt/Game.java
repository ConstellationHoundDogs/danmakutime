package nl.weeaboo.dt;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
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
import nl.weeaboo.dt.io.HashUtil;
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
import nl.weeaboo.dt.renderer.ITextureStore;
import nl.weeaboo.dt.renderer.Renderer;
import nl.weeaboo.dt.renderer.TextureStore;
import nl.weeaboo.game.GameBase;
import nl.weeaboo.game.ResourceManager;
import nl.weeaboo.game.gl.GLManager;
import nl.weeaboo.game.gl.Screenshot;
import nl.weeaboo.game.input.UserInput;
import nl.weeaboo.game.text.MutableTextStyle;
import nl.weeaboo.game.text.ParagraphRenderer;
import nl.weeaboo.game.text.layout.ParagraphLayouter;

import org.luaj.vm.LFunction;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

public class Game extends GameBase {
	
	private boolean error;
	private Notifier notifier;
	private GameVideoCapture videoCapture;
	private List<DelayedScreenshot> pendingScreenshots;
	
	private Keys keys;
	private List<JoyInput> joyInputs;
	private IKeyConfig keyConfig;
	private IPersistentStorageFactory storageFactory;
	private ITextureStore texStore;	
	private ISoundEngine soundEngine;
	
	private LuaRunState luaRunState;
	private boolean paused, pauseRequest;
	private LuaLink pauseThread;
	
	private long frame;
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
	public void hostNetGame(int tcpPort) throws IOException {
		inputBuffer.clear();
		
		IPersistentStorage ps = storageFactory.createPersistentStorage();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ps.save(bout);
		
		byte hash[] = HashUtil.calculateResourcesHash(this);
		System.out.println("Resources Hash: " + new BigInteger(hash).toString(16));
		
		server = new ServerNetworkState(hash, 2);
		server.host(tcpPort, ByteBuffer.wrap(bout.toByteArray()));
	}

	public void joinNetGame(InetAddress targetAddress, int targetTCPPort,
			int localUDPPort) throws IOException
	{
		inputBuffer.clear();
		
		byte hash[] = HashUtil.calculateResourcesHash(this);
		System.out.println("Resources Hash: " + new BigInteger(hash).toString(16));
		
		networkState = new ClientNetworkState(hash, inputBuffer, maxInputLag);
		networkState.join(targetAddress, targetTCPPort, localUDPPort);
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
		
		reset();
	}
	
	public void unloadResources() {
		videoCapture.stop();
		
		super.unloadResources();
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
		
		error = false;
		paused = pauseRequest = false;
		pauseThread = null;
		pendingScreenshots = new ArrayList<DelayedScreenshot>();		
		keyConfig = null;
		texStore = null;
		soundEngine = null;		
	}
	
	public void restart(String mainFuncName) {
		reset();
		
		ResourceManager rm = getResourceManager();
		int width = getWidth();
		int height = getHeight();
		
		keyConfig = createKeyConfig(keys);
		texStore = createTextureStore();
		soundEngine = createSoundEngine();

		IPersistentStorage storage;
		if (networkState != null && networkState.isGameStarted()) {
			if (!isHostingNetworkGame()) {
				//We received the other player's save file. Use a special subclass to
				//avoid overwriting any of our own save files with theirs.
				storage = storageFactory.createNonPersistentStorage(networkState.getPersistentStorage());
			} else {
				//We've received our own save file. We can just use that...
				storage = storageFactory.createPersistentStorage(networkState.getPersistentStorage());
			}
		} else {
			storage = storageFactory.createPersistentStorage();
		}
		
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
		
		long randomSeed = (networkState != null ? networkState.getRandomSeed() : System.nanoTime());
		luaRunState = new LuaRunState(randomSeed, platform, keys,
				threadPool, fieldMap, texStore, soundEngine, storage);
		
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
				return;
			} finally {
				try {
					if (in != null) in.close();
				} catch (IOException ioe) { }
			}
		}

		//Install default functions
		vm._G.put("pause", luaPauseFunc());
		vm._G.put("screenshot", luaScreenshotFunc());
		vm._G.put("globalReset", luaResetFunc());
		vm._G.put("quit", luaQuitFunc());
		
		//Start main thread
		threadPool.add(new LuaFunctionLink(luaRunState, vm, mainFuncName));
				
		error = false;
	}
	
	public void update(UserInput gameInput, float dt) {
		
		super.update(gameInput, dt);

		notifier.update(Math.round(1000f * dt));
		
		networkReceive();
		
		if (server != null && !server.isGameStarted()) {
			return;
		}

		if (networkState == null) {
			if (frame == 0) {
				restart("main");
			}			
		} else {
			//Control input laf
			if (gameInput.consumeKey(KeyEvent.VK_PAGE_UP)) {
				inputLag++;
			} else if (gameInput.consumeKey(KeyEvent.VK_PAGE_DOWN)) {
				inputLag--;
			}
			inputLag = Math.max(1, Math.min(maxInputLag, inputLag));
			
			//Check if network ready
			if (!networkState.isGameStarted()) {
				return;
			} else if (frame == 0) {
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
		IInput futureInput = new Input(gameInput.copy());
		for (JoyInput joypadInput : joyInputs) {
			joypadInput.update(futureInput);
		}
		
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
		
		if (error) {
			return;
		}
		
		IInput ii = (frame-inputLag > 1 ? inputBuffer.get(frame-inputLag) : new Input());
				
		if (ii.consumeKey(KeyEvent.VK_F7)) {
			//Screen capture activation key
			pendingScreenshots.add(new DelayedScreenshot(this, 0, 0, getWidth(), getHeight()) {
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
		} else if (ii.consumeKey(KeyEvent.VK_F5)) {
			restart("main");
			return;
		}
		
		videoCapture.update(ii);
		
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
		int rw = getRealWidth();
		int rh = getRealHeight();
				
		Renderer r = new Renderer(glm, createParagraphRenderer(), w, h, rw, rh);
		if (luaRunState != null) {
			luaRunState.draw(r);
		}
		r.flush();
				
		//Take screen capture
		if (!pendingScreenshots.isEmpty()) {
			Screenshot ss = Screenshot.screenshot(glm, w, h, rw, rh);
			
			int argb[] = ss.getARGB();
			for (DelayedScreenshot ds : pendingScreenshots) {
				Rectangle2D cr = ds.getCaptureRect();
				Point p0 = new Point(r.virtualToReal(cr.getX(), cr.getY()));
				Point p1 = new Point(r.virtualToReal(cr.getX()+cr.getWidth(), cr.getY()+cr.getHeight()));
				Rectangle take = new Rectangle(Math.min(p0.x, p1.x), Math.min(p0.y, p1.y),
						Math.abs(p1.x-p0.x), Math.abs(p1.y-p0.y));
				
				int pixels[] = DelayedScreenshot.copyRect(argb, ss.width, ss.height, take);				
				ds.set(pixels, take.width, take.height);
			}
			pendingScreenshots.clear();
		}
									
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
			
	public DelayedScreenshot screenshot(double x, double y, double w, double h,
			int blurMagnitude)
	{
		DelayedScreenshot ss;
		if (blurMagnitude > 1) {
			ss = new BlurringScreenshot(this, x, y, w, h, blurMagnitude);
		} else {
			ss = new DelayedScreenshot(this, x, y, w, h);
		}
		pendingScreenshots.add(ss);
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
				
				vm.resettop();
				DelayedScreenshot ds = screenshot(x, y, w, h, blurMagnitude);
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
	
	protected LFunction luaQuitFunc() {
		return new LFunction() {
			public int invoke(LuaState vm) {
				dispose();
				return 0;
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
	
	//Setters
	
}
