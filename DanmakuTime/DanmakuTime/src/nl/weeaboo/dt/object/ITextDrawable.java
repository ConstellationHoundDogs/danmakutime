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
	public void setTextAnchor(int anchor);

	/**
	 * Modifies the positioning of the block relative to its origin. 
	 * 
	 * @see #setTextAnchor(int)
	 */
	public void setBlockAnchor(int anchor);
	
	/**
	 * @see TextStyle#setFontName(String) 
	 */
	public void setFontName(String f);
	
	/**
	 * @see TextStyle#setFontStyle(FontStyle)
	 */
	public void setFontStyle(FontStyle s);

	/**
	 * @see TextStyle#setFontSize(double)
	 */
	public void setFontSize(double s);

	/**
	 * @see TextStyle#setFont(String, FontStyle, double)
	 */
	public void setFont(String fn, FontStyle fs, double sz);

	/**
	 * @see TextStyle#setColor(double, double, double)
	 */
	public void setTextColor(double r, double g, double b);

	/**
	 * @see TextStyle#setColor(int)
	 */
	public void setTextColor(int rgb);

	/**
	 * @see TextStyle#setUnderlined(boolean)
	 */
	public void setUnderlined(boolean u);

	/**
	 * @see TextStyle#setOutlineSize(double)
	 */
	public void setOutlineSize(double s);

	/**
	 * @see TextStyle#setOutlineColor(double, double, double)
	 */
	public void setOutlineColor(double r, double g, double b);

	/**
	 * @see TextStyle#setOutlineColor(int)
	 */
	public void setOutlineColor(int rgb);

}
