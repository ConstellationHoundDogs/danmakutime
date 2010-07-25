package nl.weeaboo.dt;

import nl.weeaboo.common.PreferenceStore;
import nl.weeaboo.common.PreferenceStoreView;
import nl.weeaboo.common.collections.Typed;
import nl.weeaboo.game.ConfigBase;

public class Config extends ConfigBase {
	
	public final GameView game;
	public final GraphicsView graphics;
	public final AudioView audio;
	public final CaptureView capture;
	
	public Config() {
		game = new GameView(this);
		graphics = new GraphicsView(this);
		audio = new AudioView(this);
		capture = new CaptureView(this);
		
		setProperty(new Typed("os.windows.fullscreenExclusive", Boolean.class, true));		
	}
	
	//Functions
	
	//Getters		
	public GameView getGame() { return game; }
	public GraphicsView getGraphics() { return graphics; }
	public AudioView getAudio() { return audio; }
	
	//Setters
	
	//Inner Classes
	public static class GameView extends nl.weeaboo.game.ConfigBase.GameView {
		public GameView(PreferenceStore store) {
			super(store);

			setProperty("gameName", "Danmaku Time");
			setProperty("defaultFont", "DejaVuSans");
		}
		
		public String getDefaultFont() { return (String)getProperty("defaultFont"); }
	}

	public static class GraphicsView extends nl.weeaboo.game.ConfigBase.GraphicsView {
		public GraphicsView(PreferenceStore store) {
			super(store);

			setProperty("fps", 60);
			setProperty("width", 640);
			setProperty("height", 480);
			setProperty("vsyncEnabled", true);
		}
	}

	public static class AudioView extends nl.weeaboo.game.ConfigBase.AudioView {
		public AudioView(PreferenceStore store) {
			super(store);

			setProperty("hardSync", false);
		}
		
		public boolean isHardSync() { return (Boolean)getProperty("hardSync"); }
	}
	
	public static class CaptureView extends PreferenceStoreView {
		public CaptureView(PreferenceStore store) {
			super(store, store, "capture.");

			setProperty("enabled", false);
			setProperty("width", 0);
			setProperty("height", 0);
			setProperty("forceConstantFPS", true);
			setProperty("x264.crf", 20);
		}
		
		public boolean isEnabled() { return (Boolean)getProperty("enabled"); }
		public int getWidth() { return (Integer)getProperty("width"); }
		public int getHeight() { return (Integer)getProperty("height"); }
		public boolean getForceConstantFPS() { return (Boolean)getProperty("forceConstantFPS"); }
		public int getX264CRF() { return (Integer)getProperty("x264.crf"); }
	}
	
}
