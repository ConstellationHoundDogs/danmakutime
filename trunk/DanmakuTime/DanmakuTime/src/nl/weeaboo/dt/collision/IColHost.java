package nl.weeaboo.dt.collision;

public interface IColHost {

	// === Functions ===========================================================
	public void destroy();
	public void onCollide(IColNode child, int childIndex, IColNode other);
			
	// === Getters =============================================================
	public IColField getColField();
	public double getX();
	public double getY();
	
	// === Setters =============================================================
	public void setPos(double x, double y);
	public void setColNode(int index, int type, IColNode n);	
	
}
