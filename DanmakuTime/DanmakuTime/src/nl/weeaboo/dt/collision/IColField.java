package nl.weeaboo.dt.collision;

/**
 * A parallel to {@link nl.weeaboo.dt.field.IField}, but contains
 * {@link IColNode} objects instead of drawables.
 */
public interface IColField {

	// === Functions ===========================================================
	/**
	 * Adds a new colnode to this colfield
	 * 
	 * @param c The colnode to add to this field
	 */
	public void add(IColNode c);
	
	/**
	 * Removes a colnode from this field
	 * 
	 * @param c The colnode to remove from this field
	 */
	public void remove(IColNode c);
	
	/**
	 * Perform collision detection between all nodes in this field
	 */
	public void processCollisions();
	
	// === Getters =============================================================
	
	// === Setters =============================================================
	/**
	 * Change the {@link IColMatrix} used by this colfield to limit the number
	 * of collision checks needed
	 * 
	 * @param m The new colmatrix to use
	 */
	public void setColMatrix(IColMatrix m);
	
}
