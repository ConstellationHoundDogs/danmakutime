package nl.weeaboo.dt.input;

public interface IKeyConfig {

	/**
	 * @param player The player number
	 * @param vkey The virtual key
	 * @return The keycodes of the physical keys associated with the virtual key.
	 */
	public int[] getPhysicalKeyCodes(int player, VKey vkey);
	
	/**
	 * @param i The input object to check
	 * @return An array containing all vkeys held in the input
	 */
	public int[] getVKeysHeld(IInput i);
	
	/**
	 * @param i The input object to check
	 * @return An array containing all vkeys pressed in the input
	 */
	public int[] getVKeysPressed(IInput i);
	
}
