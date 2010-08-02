package nl.weeaboo.dt.input;

public interface IInput extends Cloneable {

	/**
	 * @return A copy of this object
	 */
	public IInput clone();
	
	/**
	 * @param keycode The key to check the state of
	 * @return If the specified key is currently pressed
	 */
	public boolean isKeyHeld(int keycode);

	/**
	 * @param keycode The key to check the state of
	 * @return If the specified key got pressed since the last frame
	 */
	public boolean isKeyPressed(int keycode);

	/**
	 * @param keycode The key to check/change the state of
	 * @return Like #isKeyPressed(Key), but makes further calls to
	 *         #isKeyPressed(Key) and #consumeKey(Key) return false.
	 */
	public boolean consumeKey(int keycode);

	/**
	 * Clears all pressed key states.
	 */
	public void clear();
	
	/**
	 * @return An array containing all keycodes of keys that were pressed this
	 *         frame.
	 */
	public int[] getKeysPressed();

	/**
	 * @return An array containing all keycodes of keys that were currently
	 *         pressed this frame.
	 */
	public int[] getKeysHeld();
	
	/**
	 * Sets the key state for the specified key to pressed
	 * 
	 * @param keycode The keycode of the key
	 */
	public void setKeyPressed(int keycode);
	
	/**
	 * Sets the key state for the specified key to held
	 * 
	 * @param keycode The keycode of the key
	 */
	public void setKeyHeld(int keycode);
	
	/**
	 * Clears the key state for the specified key
	 * 
	 * @param keycode The keycode of the key
	 */
	public void setKeyReleased(int keycode);
	
}
