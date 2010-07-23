package nl.weeaboo.dt.field;

import nl.weeaboo.dt.collision.IColField;
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
	 * Adds all pending new objects to the current list of objects.
	 */
	public void flushStandbyList();
	
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
	 * @return An array of all objects currently attached to this field
	 */
	public IDrawable[] getAllObjects();
	
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
		
	/**
	 * A field's padding is used by objects to determine if they're so far away
	 * from the field that it may be a good idea to destroy themselves as it's
	 * unlikely they'll ever return.
	 * 
	 * @return The field's padding (the padding gets added to the bounds in all
	 *         directions)
	 */
	public int getPadding();
	
	/**
	 * @return The collision field associated with this field
	 */
	public IColField getColField();
	
	// === Setters =============================================================
	
}
