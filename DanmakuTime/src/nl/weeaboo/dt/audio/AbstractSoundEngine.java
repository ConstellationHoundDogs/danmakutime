package nl.weeaboo.dt.audio;

import nl.weeaboo.game.sound.SoundEffect;
import nl.weeaboo.game.sound.SoundManager;

public abstract class AbstractSoundEngine implements ISoundEngine {

	protected final SoundManager sm;
	protected final int fps;
	
	protected String bgmPath;	
	protected long frame;
	
	public AbstractSoundEngine(SoundManager sm, int fps) {
		this.sm = sm;
		this.fps = fps;
	}
	
	//Functions
	@Override
	public void update(int frameInc) {
		frame += frameInc;
	}
		
	@Override
	public void stopAll() {
		sm.stopAll();
	}
	
	@Override
	public SoundEffect playSound(String filename) {
		return playSound(filename, 1);
	}
	
	@Override
	public SoundEffect playSound(String filename, int loops) {
		return sm.playSound(filename, loops, 1f);
	}
	
	//Getters
	@Override
	public String getBGMFilename() {
		return bgmPath;
	}	
	
	//Setters
	
}
