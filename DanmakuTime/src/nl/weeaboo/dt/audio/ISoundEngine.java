package nl.weeaboo.dt.audio;

import nl.weeaboo.game.sound.SoundEffect;

public interface ISoundEngine {

	// === Functions ===========================================================

	/**
	 * @param frameInc The number of game frames that have passed since the last
	 *        call to {@link #update(int)}. Use <code>0</code> when the game is
	 *        paused.
	 */
	public void update(int frameInc);

	/**
	 * Stops the current background music
	 */
	public void stopBGM();

	/**
	 * Stops all music and sound effects.
	 */
	public void stopAll();
	
	/**
	 * @see #playSound(String, int) 
	 */
	public SoundEffect playSound(String filename);
	
	/**
	 * Plays an Ogg Vorbis sound effect, looping the specified number of times.
	 * 
	 * @param filename Relative path to the audio file (relative to
	 *        &quot;snd&quot;)
	 * @param loops The number of times the sound effect should loop. Use
	 *        <code>-1</code> for unlimited looping.
	 * @return A sound effect object that can be used to further manipulate
	 *         playback of the sound effect.
	 */
	public SoundEffect playSound(String filename, int loops);
	
	// === Getters =============================================================

	/**
	 * @return The relative path of the currently playing background music,
	 *         <code>null</code> if no music is playing.
	 */
	public String getBGMFilename();
	
	// === Setters =============================================================

	/**
	 * Changes the background music, use <code>null</code> to just stop the
	 * current music.
	 * 
	 * @param filename The relative path to an Ogg Vorbis file (relative to
	 *        &quot;snd&quot;).
	 */
	public void setBGM(String filename);
}
