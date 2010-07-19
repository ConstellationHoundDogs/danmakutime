package nl.weeaboo.dt.object;

import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.renderer.IRenderer;
import nl.weeaboo.dt.renderer.ITexture;

public interface IDrawable {

	// === Functions ===========================================================

	/**
	 * @param input User input (keyboard presses)
	 */
	public void update(IInput input);

	/**
	 * @param renderer The 'drawing' object, abstracts over the underlying
	 *        drawing engine.
	 */
	public void draw(IRenderer renderer);

	/**
	 * Marks the object as destroyed
	 */
	public void destroy();

	// === Getters =============================================================

	/**
	 * @return <code>true</code> if the object is scheduled to be removed next
	 *         frame.
	 */
	public boolean isDestroyed();

	/**
	 * @return The drawable's X-coordinate
	 */
	public double getX();

	/**
	 * @return The drawable's Y-coordinate
	 */
	public double getY();

	/**
	 * @return The drawable's angle (between 0.0 and 512.0)
	 */
	public double getDrawAngle();

	/**
	 * @return The drawable's current texture (used by the default
	 *         implementation of #draw(IRenderer) to texture the rendered quad.
	 */
	public ITexture getTexture();

	/**
	 * @return The drawable's Z-coordinate
	 */
	public short getZ();

	/**
	 * @return <code>true</code> if clipping is enabled for this drawable
	 */
	public boolean isClip();
	
	// === Setters =============================================================

	/**
	 * Changes the drawable's position on-screen
	 * 
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 */
	public void setPos(double x, double y);

	/**
	 * @param a The drawable's rotation (between 0.0 and 512.0)
	 */
	public void setDrawAngle(double a);

	/**
	 * Changed the current texture
	 * 
	 * @param tex The new texture
	 * 
	 * @see #getTexture()
	 */
	public void setTexture(ITexture tex);

	/**
	 * Changes the draw layer
	 * 
	 * @param z The new Z-coordinate, higher Z is deeper into the screen.
	 */
	public void setZ(short z);

	/**
	 * Enable/disable clipping for this drawable 
	 */
	public void setClip(boolean c);
}
