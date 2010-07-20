package nl.weeaboo.dt;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import nl.weeaboo.common.GraphicsUtil;
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
import nl.weeaboo.dt.object.Drawable;
import nl.weeaboo.dt.renderer.Blur;
import nl.weeaboo.dt.renderer.ITextureStore;
import nl.weeaboo.dt.renderer.Renderer;
import nl.weeaboo.dt.renderer.Texture;
import nl.weeaboo.dt.renderer.TextureStore;
import nl.weeaboo.game.GameBase;
import nl.weeaboo.game.ResourceManager;
import nl.weeaboo.game.gl.GLImage;
import nl.weeaboo.game.gl.GLManager;
import nl.weeaboo.game.gl.GLVideoCapture;
import nl.weeaboo.game.gl.Screenshot;
import nl.weeaboo.game.input.UserInput;
import nl.weeaboo.game.text.MutableTextStyle;
import nl.weeaboo.game.text.ParagraphRenderer;
import nl.weeaboo.game.text.layout.ParagraphLayouter;

import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.vm.LuaState;

public class Game extends GameBase {

	private boolean error;
	private GLVideoCapture videoCapture;
	private Drawable screenshot;
	private boolean screenshotRequest, screenshotRequestSave;
	private Notifier notifier;
	
	private ITextureStore texStore;	
	private ISoundEngine soundEngine;
	
	private LuaRunState luaRunState;
	private boolean paused;
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
		
		videoCapture = new GLVideoCapture(this, filename, config.capture.getX264CRF(),
				config.graphics.getFPS(), rw, rh, d.width, d.height, true);
		
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
			Log.warning(e);
		}
		
		Log.getInstance().start(isDebug(), notifier, err);

		restart();
	}
	
	public void unloadResources() {
		stopRecordingVideo();
		
		super.unloadResources();
	}
	
	public void restart() {
		error = false;

		ResourceManager rm = getResourceManager();
		int width = getWidth();
		int height = getHeight();
		
		texStore = createTextureStore();
		soundEngine = createSoundEngine();
				
		TinyMap<IField> fieldMap = new TinyMap<IField>();
		fieldMap.put(0, new Field(0, 0, width, height, 0)); //Full-screen field (0)
		fieldMap.put(1, new Field(0, 0, width, height, 0)); //Game field (1)
		fieldMap.put(2, new Field(0, 0, width, height, 0)); //Overlay field (2)
		
		//Init Lua
		LuaThreadPool threadPool = new LuaThreadPool();
		
		luaRunState = new LuaRunState(System.nanoTime(), threadPool, fieldMap,
				texStore, soundEngine);
		
		LuaState vm = getLuaState();
				
		//Compile code
		for (String path : rm.getFolderContents("script")) {
			InputStream in = null;
			try {				
				in = rm.getInputStream(path);
				LuaUtil.load(vm, path, in);
			} catch (Exception e) {
				Log.showError(e);
				error = true;
				return;
			} finally {
				try {
					if (in != null) in.close();
				} catch (IOException ioe) { }
			}
		}
		
		//Start main thread
		threadPool.add(new LuaFunctionLink(luaRunState, vm, "main"));
				
		error = false;
	}
	
	public void update(UserInput input, float dt) {
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
			screenshotRequest = true;
			screenshotRequestSave = true;
		} else if (ii.consumeKey(KeyEvent.VK_F8)) {
			//Video capture activation key
			if (isRecordingVideo()) {
				stopRecordingVideo();
			} else {			
				try {
					startRecordingVideo("capture.mkv");
					Log.message("Starting video recording");
				} catch (IOException e) {
					Log.showError(e);
				}
			}
		}
		
		if (paused) {
			luaRunState.updatePaused();
			
			if (!pauseThread.isFinished()) {
				try {
					pauseThread.update();
				} catch (LuaException e) {
					Log.warning(e);
					paused = false;
				}
			} else {
				paused = false;				
			}
			
			if (!paused && screenshot != null) {
				screenshot.destroy();
				screenshot = null;
			}
		} else {
			luaRunState.update(ii);
			
			if (input.consumeKey(KeyEvent.VK_ESCAPE)) {				
				paused = true;
				screenshotRequest = true;
				pauseThread = new LuaFunctionLink(luaRunState, vm, "pauseHandler");
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
		if (screenshotRequest) {
			screenshotRequest = false;
			
			Screenshot ss = Screenshot.screenshot(glm, w, h, rw, rh);			
			if (screenshotRequestSave) {
				screenshotRequest = false;

				try {
					BufferedImage img = GraphicsUtil.createBufferedImage(ss.width, ss.height, ss.getARGB());
					ImageIO.write(img, "png", new File("capture-" + System.currentTimeMillis() + ".png"));
					Log.message("Screenshot saved");
				} catch (IOException e) {
					Log.showError(e);
				}
			} else {
				int argb[] = ss.getARGB();
				Dimension size = new Dimension(ss.width, ss.height);
				argb = Blur.process(argb, size, 4, true, true);
				
				GLImage gli = addGeneratedImage(IntBuffer.wrap(argb),
						size.width, size.height, true, false);
				
				IField overlayField = luaRunState.getField(2);
				
				screenshot = new Drawable();
				screenshot.setField(overlayField);
				screenshot.setPos(overlayField.getWidth()/2, overlayField.getHeight()/2);
				screenshot.setColor(0xFFAAAAAA);
				screenshot.setZ(1000);
				screenshot.setTexture(new Texture(gli));				
			}
		}
		
		if (videoCapture != null) {
			try {
				videoCapture.update(glm, rw, rh);
			} catch (IOException e) {
				Log.showError(e);
				videoCapture = null;
			}
		}
		
		//Draw HUD
		ParagraphRenderer pr = createParagraphRenderer();
		pr.setBounds(20, 20, w-40, h-40);
		
		MutableTextStyle mts = pr.getDefaultStyle().mutableCopy();
		mts.setAnchor(9);
		pr.setDefaultStyle(mts.immutableCopy());
		
		String hudText = String.format("%.2f FPS\n%d Objects\n",
				getFPS(), luaRunState.getObjectCount());
		pr.drawText(glm, hudText);
		
		//Draw notifier
		notifier.draw(glm, w, h);
		
		super.draw(glm);
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
		return new SoftSyncSoundEngine(getSoundManager(), getConfig().graphics.getFPS());
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
