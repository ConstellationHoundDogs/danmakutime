package nl.weeaboo.dt.field;

import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.object.IDrawable;
import nl.weeaboo.dt.renderer.IRenderer;

public interface IField {

	/**
	 * Add a drawable to the field
	 * 
	 * @param d The new drawable to add
	 * @throws IllegalArgumentException If <code>d</code> is <code>null</code>
	 */
	public void add(IDrawable d);

	/**
	 * Removes a drawable from the field
	 * 
	 * @param d The drawable to remove
	 * @return <code>true</code> if the drawable successfully gets removed from
	 *         the field
	 */
	public boolean remove(IDrawable d);

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
	
	/**
	 * @return The number of active objects in this field
	 */
	public int getObjectCount();
}
