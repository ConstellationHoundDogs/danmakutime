package nl.weeaboo.dt.renderer;

public interface IRenderer {

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
	 * Changes the current texture
	 * 
	 * @param tex The new texture
	 */
	public void setTexture(ITexture tex);

}
