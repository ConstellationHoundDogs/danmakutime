package nl.weeaboo.dt.input;

public interface IInput {

	/**
	 * @param key The key to check the state of
	 * @return If the specified key is currently pressed
	 */
	public boolean isKeyHeld(int keycode);

	/**
	 * @param key The key to check the state of
	 * @return If the specified key got pressed since the last frame
	 */
	public boolean isKeyPressed(int keycode);

	/**
	 * @param key The key to check/change the state of
	 * @return Like #isKeyPressed(Key), but makes further calls to
	 *         #isKeyPressed(Key) and #consumeKey(Key) return false.
	 */
	public boolean consumeKey(int keycode);

	/**
	 * Clears all pressed key states.
	 */
	public void clear();
	
}
