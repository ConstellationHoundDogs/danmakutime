package nl.weeaboo.dt.object;

public interface ITextDrawable extends IDrawable {

	// === Functions ===========================================================

	// === Getters =============================================================

	/**
	 * @return The word-wrap width, or <code>0</code> if word-wrapping is
	 *         disabled
	 */
	public int getWordWrap();
	
	// === Setters =============================================================
	
	/**
	 * Changes the string of text displayed by this text drawable
	 * 
	 * @param txt The new text to display
	 */
	public void setText(String txt);
	
	/**
	 * Changes the word-wrapping width. Use <code>0</code> to disable
	 * word-wrapping.
	 * 
	 * @param width The new word-wrap width
	 */
	public void setWidth(int width);
	
	/**
	 * The anchor determines how the text is positioned relative to its position
	 * (x, y). The anchor values correspond to the directions of the numpad
	 * numbers, so <code>7</code> is top-left and <code>5</code> is
	 * center-middle.
	 */
	public void setAnchor(int anchor);
	
}
