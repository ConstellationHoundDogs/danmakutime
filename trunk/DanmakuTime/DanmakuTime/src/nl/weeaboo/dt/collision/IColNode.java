package nl.weeaboo.dt.collision;


public interface IColNode {

	// === Functions ===========================================================
	public void init(IColHost host, int type);
	
	public boolean intersects(IColNode c);
	
	public void onCollide(IColNode c);
	
	// === Getters =============================================================
	public int getType();
	
	public double getBoundingRectangleRadius();
	public double getCenterX();
	public double getCenterY();
	
	// === Setters =============================================================
	
}
