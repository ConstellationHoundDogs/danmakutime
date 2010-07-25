package nl.weeaboo.dt.collision;

import nl.weeaboo.dt.object.IDrawable;

public interface IColHost {

	// === Functions ===========================================================
	/**
	 * Marks this colhost as destroyed. Removes it and its children from any
	 * {@link IColField} they're attached to.
	 */
	public void destroy();
	
	/**
	 * This method gets called when one of this colhost's children detects a
	 * collision.
	 * 
	 * @param child Our colnode that got into a collision
	 * @param childIndex The child's index in this colhost's list of children
	 * @param other The colnode our child collided with
	 */
	public void onCollide(IColNode child, int childIndex, IColNode other);
			
	// === Getters =============================================================
	/**
	 * @return The owner of this colhost
	 */
	public IDrawable getOwner();
	
	/**
	 * @return The colfield this colhost attached its children to
	 */
	public IColField getColField();
	
	/**
	 * @return The X-coordinate of this colhost
	 */
	public double getX();
	
	/**
	 * @return The Y-coordinate of this colhost
	 */
	public double getY();
	
	/**
	 * @return The rotation of this colhost, between <code>0.0</code> and
	 *         <code>512.0</code>
	 */
	public double getAngle();
	
	// === Setters =============================================================
	/**
	 * Changes the x/y of this colhost
	 * 
	 * @param x The new X-coordinate
	 * @param y The new Y-coordinate
	 */
	public void setPos(double x, double y);
	
	/**
	 * Changes the rotation of this colhost. The new rotation gets propagated to
	 * any children implementing the {@link IRotateableColNode} interface.
	 * 
	 * @param a The new rotation, between <code>0.0</code> and
	 *        <code>512.0</code>
	 */
	public void setAngle(double a);
	
	/**
	 * Adds or replaces a child colnode. Indices may be any int and need not be
	 * sequential.
	 * 
	 * @param index The index for the new child
	 * @param type The collision type of the colnode (one of the values returned
	 *        by {@link IColMatrix#newColType()})
	 * @param n The colnode to add
	 */
	public void setColNode(int index, int type, IColNode n);	
	
}
