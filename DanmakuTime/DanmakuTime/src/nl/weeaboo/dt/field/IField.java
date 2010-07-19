package nl.weeaboo.dt.field;

import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.object.IDrawable;
import nl.weeaboo.dt.renderer.IRenderer;

public interface IField {

	// === Functions ===========================================================
	
	/**
	 * Add a drawable to the field
	 * 
	 * @param d The new drawable to add
	 * @throws IllegalArgumentException If <code>d</code> is <code>null</code>
	 */
	public void add(IDrawable d);

	/**
	 * Updates this field and all objects it contains
	 * 
	 * @param input The current user input
	 */
	public void update(IInput input);
	
	/**
	 * Draws the field and all objects it contains
	 * 
	 * @param renderer The renderer to use for drawing
	 */
	public void draw(IRenderer renderer);
	
	// === Getters =============================================================
	
	/**
	 * @return The number of active objects in this field
	 */
	public int getObjectCount();
	
	/**
	 * @return The X-coordinate of the top-left pixel of this field
	 */
	public int getX();

	/**
	 * @return The Y-coordinate of the top-left pixel of this field
	 */
	public int getY();
	
	/**
	 * @return The width of this field in pixels
	 */
	public int getWidth();
	
	/**
	 * @return The height of this field in pixels
	 */
	public int getHeight();
	
	// === Setters =============================================================
	
}
