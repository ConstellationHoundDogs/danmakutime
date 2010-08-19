package nl.weeaboo.dt.audio;

import nl.weeaboo.game.sound.Music;
import nl.weeaboo.game.sound.SoundManager;

/**
 * {@link ISoundEngine} implementation that favors gapless playback over perfect
 * synchronization with the game logic.
 */
public class SoftSyncSoundEngine extends AbstractSoundEngine {

	private Music bgm;
	private int fadeInTime = 2000;
	private int fadeOutTime = 1000;
	
	public SoftSyncSoundEngine(SoundManager sm, int fps) {
		super(sm, fps);
	}
	
	//Functions
	@Override
	public void update(int frameInc) {
		super.update(frameInc);
		
		if (bgm != null) {			
			if (frameInc <= 0) {
				if (!bgm.isPaused()) bgm.pauseFast();
			} else {
				if (bgm.isPaused()) bgm.unpause();
			}
		}
	}
		
	@Override
	public void stopBGM() {
		sm.setBGM(null, fadeOutTime, 0f);
	}
	
	//Getters
	
	//Setters
	public void setBGM(String path) {
		bgmPath = path;		
		if (bgmPath == null) {
			stopBGM();
			return;
		}
		
		bgm = sm.setBGM(bgmPath, fadeInTime, 1f);
	}
	
}
