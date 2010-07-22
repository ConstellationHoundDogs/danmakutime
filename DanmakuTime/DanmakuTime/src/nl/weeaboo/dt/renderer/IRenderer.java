package nl.weeaboo.dt.renderer;

import java.awt.Rectangle;

import nl.weeaboo.dt.object.StyledText;

public interface IRenderer {

	// === Functions ===========================================================

	/**
	 * Draws a quad at the specified position with the specified size, using the
	 * current texture.
	 * 
	 * @param x The top-left X-coordinate
	 * @param y The top-left Y-coordinate
	 * @param w The quad's width
	 * @param h The quad's height
	 * @param z The quad's Z-coordinate
	 */
	public void drawQuad(double x, double y, double w, double h, short z);

	/**
	 * Draws a rotated quad at the specified position with the specified size
	 * and rotation, using the current texture.
	 * 
	 * @param cx The quad's center X-coordinate
	 * @param cy The quad's center Y-coordinate
	 * @param w The quad's width
	 * @param h The quad's height
	 * @param z The quad's Z-coordinate
	 * @param angle The quad's rotation, between <code>0.0</code> and
	 *        <code>512.0</code>
	 */
	public void drawRotatedQuad(double cx, double cy, double w, double h, short z,
			double angle);

	/**
	 * Draws a paragraph of text
	 * 
	 * @param txt The text to draw
	 * @param x The text block's top-left X-coordinate
	 * @param y The text block's top-left Y-coordinate
	 * @param z The text block's Z-coordinate
	 * @param angle The text block's rotation, between <code>0.0</code> and
	 *        <code>512.0</code>
	 * @param wrapWidth The text block's word-wrap width, use <code>0</code> to
	 *        disable word wrapping
	 * @param blockAnchor The anchor determines how the text block is positioned
	 *        relative to its position (x, y). The anchor values correspond to
	 *        the directions of the numpad numbers, so <code>7</code> is
	 *        top-left and <code>5</code> is center-middle.
	 */
	public void drawText(StyledText txt, double x, double y, short z, double angle,
			double wrapWidth, int blockAnchor);
	
	/**
	 * Translates the origin of the coordinate system
	 * 
	 * @param dx The change in X-coordinates
	 * @param dy The change in Y-coordinates
	 */
	public void translate(double dx, double dy);
	
	// === Getters =============================================================

	/**
	 * @return The current clipping rectangle
	 */
	public Rectangle getClipRect();
	
	/**
	 * @return <code>true</code> if clipping is enabled
	 */
	public boolean isClipEnabled();
	
	/**
	 * @return The current color
	 */
	public int getColor();
	
	/**
	 * @return The current blend mode
	 */
	public BlendMode getBlendMode();
	
	// === Setters =============================================================
	
	/**
	 * Changes the current texture
	 * 
	 * @param tex The new texture
	 */
	public void setTexture(ITexture tex);

	/**
	 * Changes the current clipping rectangle
	 * 
	 * @param x Top-left X-coordinate
	 * @param y Top-left Y-coordinate
	 * @param w Clipping rectangle width
	 * @param h Clipping rectangle height
	 */
	public void setClipRect(int x, int y, int w, int h);

	/**
	 * Toggles clipping on or off for future draw operations
	 */
	public void setClipEnabled(boolean ce);
	
	/**
	 * Changes the color blended with future drawing operations
	 * 
	 * @param argb The new color packed as an <code>int</code> (in ARGB order)
	 */
	public void setColor(int argb);
	
	/**
	 * Changes the blend mode for future drawing operations
	 * 
	 * @param b The new blend mode
	 */
	public void setBlendMode(BlendMode b);
	
}
