package nl.weeaboo.dt.collision;


public interface IColNode {

	// === Functions ===========================================================
	public void onAttached(IColHost host, int index, int type);
	public void onDetached();
		
	public void onCollide(IColNode c);
	
	public boolean intersects(IColNode c);
	
	// === Getters =============================================================
	public IColHost getHost();
	public int getType();
	
	public double getBoundingRectangleRadius();
	public double getCenterX();
	public double getCenterY();
	
	// === Setters =============================================================
	
}
