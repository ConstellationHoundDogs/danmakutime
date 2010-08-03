package nl.weeaboo.dt.collision;

public interface IColMatrix extends Cloneable {

	// === Functions ===========================================================
	/**
	 * @return A copy of this object
	 */
	public IColMatrix clone();
	
	/**
	 * @return A unique collision type identifier
	 */
	public int newColType();
	
	/**
	 * @see #setColliding(int, int, boolean)
	 */
	public void setColliding(int t0, int t1);

	/**
	 * Alters the value of <code>[t0][t1]</code> in the matrix. If the value is
	 * <code>true</code>, it enables collisions between <code>t0</code> and
	 * <code>t1</code>. If <code>[t0][t1]</code> is true, but
	 * <code>[t1][t0]</code> is false, collisions will be checked for but only
	 * <code>t0</code> will be notified in case of a collision.
	 * 
	 * @param t0 The collision type to receive the collision event
	 * @param t1 The other collision type
	 * @param c The new value
	 */
	public void setColliding(int t0, int t1, boolean c);
	
	/**
	 * @see #setColliding2(int, int, boolean)
	 */
	public void setColliding2(int t0, int t1);
	
	/**
	 * Calls <code>setColliding(t0, t1, c}</code>, and then
	 * <code>setColliding(t1, t0, c)</code>.
	 */
	public void setColliding2(int t0, int t1, boolean c);
	
	// === Getters =============================================================
	/**
	 * @return The value of cell <code>[t0][t1]</code>
	 */
	public boolean isColliding(int t0, int t1);
	
	/**
	 * @return The width/height of the matrix (it's always square)
	 */
	public int getSize();
	
	// === Setters =============================================================
	
}
