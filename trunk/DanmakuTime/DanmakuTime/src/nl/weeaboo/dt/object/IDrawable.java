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
	 * @return The sprite's X-coordinate
	 */
	public double getX();

	/**
	 * @return The sprite's Y-coordinate
	 */
	public double getY();

	/**
	 * @return The sprite's angle (between 0.0 and 512.0)
	 */
	public double getDrawAngle();

	/**
	 * @return The sprite's current texture (used by the default implementation
	 *         of #draw(IRenderer) to texture the rendered quad.
	 */
	public ITexture getTexture();

	// === Setters =============================================================

	/**
	 * Changes the sprite's position on-screen
	 * 
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 */
	public void setPos(double x, double y);

	/**
	 * @param a The sprite's rotation (between 0.0 and 512.0)
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
}
