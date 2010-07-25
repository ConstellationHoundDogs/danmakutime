package nl.weeaboo.dt.collision;

public interface IColNode {

	// === Functions ===========================================================
	/**
	 * Gets called when this colnode gets attached to a host
	 * 
	 * @param host The host now containing this colnode
	 * @param index The position in the host's collection of children where this
	 *        colnode was inserted
	 * @param type The collision type as which this colnode was inserted into
	 *        the colhost
	 */
	public void onAttached(IColHost host, int index, int type);
	
	/**
	 * Gets called when this colnode gets removed from its host
	 */
	public void onDetached();
		
	/**
	 * Gets called in case of a collision with another colnode
	 * 
	 * @param c The other colnode
	 */
	public void onCollide(IColNode c);

	/**
	 * Intersection test for colnodes, must be symmetric:
	 * <code>a.intersects(b) == b.instersects(a)</code>
	 * 
	 * @param c The colnode to test for intersection with
	 * @return <code>true</code> if the two colnodes intersect
	 */
	public boolean intersects(IColNode c);
	
	// === Getters =============================================================
	/**
	 * @return The colhost this colnode is currently attached to
	 * @see #onAttached(IColHost, int, int)
	 */
	public IColHost getHost();
	
	/**
	 * @return The collision type this colnode currently has
	 * @see #onAttached(IColHost, int, int)
	 */
	public int getType();
	
	/**
	 * @return The radius of the bounding circle for this colnode 
	 */
	public double getBoundingCircleRadius();
	
	/**
	 * @return The center X-coordinate for this colnode's bounding circle
	 */
	public double getCenterX();

	/**
	 * @return The center Y-coordinate for this colnode's bounding circle
	 */
	public double getCenterY();
	
	// === Setters =============================================================
	
}
