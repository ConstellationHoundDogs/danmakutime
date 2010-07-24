package nl.weeaboo.dt;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import nl.weeaboo.common.GraphicsUtil;
import nl.weeaboo.dt.audio.HardSyncSoundEngine;
import nl.weeaboo.dt.audio.ISoundEngine;
import nl.weeaboo.dt.audio.SoftSyncSoundEngine;
import nl.weeaboo.dt.field.Field;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.Input;
import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;
import nl.weeaboo.dt.lua.LuaThreadPool;
import nl.weeaboo.dt.lua.LuaUtil;
import nl.weeaboo.dt.lua.link.LuaFunctionLink;
import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.lua.platform.LuaPlatform;
import nl.weeaboo.dt.lua.platform.LuajavaLib;
import nl.weeaboo.dt.renderer.ITextureStore;
import nl.weeaboo.dt.renderer.Renderer;
import nl.weeaboo.dt.renderer.TextureStore;
import nl.weeaboo.game.GameBase;
import nl.weeaboo.game.ResourceManager;
import nl.weeaboo.game.gl.GLManager;
import nl.weeaboo.game.gl.GLVideoCapture;
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
	private GLVideoCapture videoCapture;
	private Notifier notifier;
	private List<DelayedScreenshot> pendingScreenshots;
	
	private ITextureStore texStore;	
	private ISoundEngine soundEngine;
	
	private LuaRunState luaRunState;
	private boolean paused, pauseRequest;
	private LuaLink pauseThread;
	
	public Game(Config c, ResourceManager rm, GameFrame gf) {
		super(c, rm, gf);
	}
	
	//Functions
	public static void loadConfig(Config config, ResourceManager rm) throws IOException {
		String configFilename = "prefs.xml";

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
	
	public void startRecordingVideo(String filename) throws IOException {
		stopRecordingVideo();
		
		Config config = getConfig();
		
		int rw = getWidth();
		int rh = getHeight();
		int scw = config.capture.getWidth();
		int sch = config.capture.getHeight();
		
		if (scw <= 0) scw = rw;
		if (sch <= 0) sch = rh;
		
		Dimension d = GraphicsUtil.getProportionalScale(rw, rh, scw, sch);
		
		int captureFPS = config.graphics.getFPS();
		//while (captureFPS > 30) captureFPS /= 2;
		
		videoCapture = new GLVideoCapture(this, filename, config.capture.getX264CRF(),
				captureFPS, rw, rh, d.width, d.height, true);
		
		try {
			videoCapture.start();
		} catch (IOException ioe) {
			videoCapture = null;
			throw ioe;
		}
	}
	
	public void stopRecordingVideo() {
		if (videoCapture != null) {
			videoCapture.stop();
			videoCapture = null;
		}		
	}
	
	public void loadResources() {
		super.loadResources();
						
		ResourceManager rm = getResourceManager();
		
		notifier = new Notifier(createParagraphRenderer());
		
		OutputStream err = null;
		try {
			rm.getOutputStream("err.txt");
		} catch (IOException e) {
			DTLog.warning(e);
		}
		
		DTLog.getInstance().start(isDebug(), notifier, err);
			
		restart("main");
	}
	
	public void unloadResources() {
		stopRecordingVideo();
		
		super.unloadResources();
	}
	
	public void restart(String mainFuncName) {
		error = false;

		paused = pauseRequest = false;
		pauseThread = null;
		pendingScreenshots = new ArrayList<DelayedScreenshot>();
		
		ResourceManager rm = getResourceManager();
		int width = getWidth();
		int height = getHeight();
		
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
		
		luaRunState = new LuaRunState(System.nanoTime(), platform, threadPool,
				fieldMap, texStore, soundEngine);
		
		LuaState vm = getLuaState();
				
		//Compile code
		SortedSet<String> scripts = new TreeSet<String>(new Comparator<String>() {
			Collator c = Collator.getInstance(Locale.ROOT);
			public int compare(String o1, String o2) {
				return c.compare(o1, o2);
			}	
		});
		scripts.addAll(rm.getFolderContents("script"));
		
		for (String path : scripts) {
			InputStream in = null;
			try {				
				in = rm.getInputStream(path);
				LuaUtil.loadModule(vm, path, in);
				LuaUtil.initModule(vm);
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

		//Install pause function
		vm._G.put("pause", new LFunction() {
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
		});
		
		//Install screenshot function
		vm._G.put("screenshot", new LFunction() {
			public int invoke(LuaState vm) {
				int x = (vm.isnumber(1) ? vm.tointeger(1) : 0);
				int y = (vm.isnumber(2) ? vm.tointeger(2) : 0);
				int w = (vm.isnumber(3) ? vm.tointeger(3) : getWidth());
				int h = (vm.isnumber(4) ? vm.tointeger(4) : getHeight());
				boolean blur = (vm.isboolean(5) ? vm.toboolean(5) : false);
				
				vm.resettop();
				DelayedScreenshot ds = screenshot(x, y, w, h, blur);
				vm.pushlvalue(LuajavaLib.toUserdata(ds, ds.getClass()));
				return 1;
			}
		});
		
		//Install globalReset function
		vm._G.put("globalReset", new LFunction() {
			public int invoke(LuaState vm) {
				String funcName = vm.tostring(1);
				vm.resettop();
				
				restart(funcName);
				return 0;
			}
		});

		//Install quit function
		vm._G.put("quit", new LFunction() {
			public int invoke(LuaState vm) {
				dispose();
				return 0;
			}
		});
		
		//Start main thread
		threadPool.add(new LuaFunctionLink(luaRunState, vm, mainFuncName));
				
		error = false;
	}
	
	public void update(UserInput input, float dt) {
		//Update paused state
		paused = pauseRequest;
		if (!paused) pauseThread = null;
		
		super.update(input, dt);
				
		notifier.update(Math.round(1000f * dt));
		
		soundEngine.update(paused ? 0 : 1);
		
		if (error) {
			return;
		}

		LuaState vm = getLuaState();
		Input ii = new Input(input);

		//global IInput input
		vm.pushlvalue(LuajavaLib.toUserdata(ii, ii.getClass()));
		vm.setglobal("input");

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
		} else if (ii.consumeKey(KeyEvent.VK_F8)) {
			//Video capture activation key
			if (isRecordingVideo()) {
				stopRecordingVideo();
			} else {			
				try {
					startRecordingVideo("capture.mkv");
					DTLog.message("Starting video recording");
				} catch (IOException e) {
					DTLog.showError(e);
				}
			}
		}
		
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
		luaRunState.draw(r);
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
		if (isDebug()) {
			ParagraphRenderer pr = createParagraphRenderer();
			pr.setBounds(2, -2, w-6, h-4);
			
			MutableTextStyle mts = pr.getDefaultStyle().mutableCopy();
			mts.setAnchor(9);
			pr.setDefaultStyle(mts.immutableCopy());
			
			String hudText = String.format("%.2f FPS\n%d Objects\n",
					getFPS(), luaRunState.getObjectCount());
			pr.drawText(glm, hudText);
		}

		//Video capture
		if (videoCapture != null) {
			try {
				videoCapture.update(glm, rw, rh);
			} catch (IOException e) {
				DTLog.showError(e);
				videoCapture = null;
			}
		}
				
		//Draw notifier
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
	
	public ParagraphRenderer createParagraphRenderer(ParagraphLayouter layouter) {	
		ParagraphRenderer pr = super.createParagraphRenderer(layouter);
		
		MutableTextStyle mts = pr.getDefaultStyle().mutableCopy();
		mts.setFontName("DejaVuSans");
		mts.setFontSize(16);
		pr.setDefaultStyle(mts.immutableCopy());
		
		return pr;
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
	
	public DelayedScreenshot screenshot(double x, double y, double w, double h, boolean blur) {
		DelayedScreenshot ss;
		if (blur) {
			ss = new BlurringScreenshot(this, x, y, w, h);
		} else {
			ss = new DelayedScreenshot(this, x, y, w, h);
		}
		pendingScreenshots.add(ss);
		return ss;
	}
	
	public void pause() {
		pauseRequest = true;
	}
	
	public void unpause() {
		pauseRequest = false;
	}
	
	//Getters
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
	public boolean isRecordingVideo() {
		return videoCapture != null;
	}
	
	//Setters
	
}
