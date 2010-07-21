package nl.weeaboo.dt.collision;

public interface IColHost {

	// === Functions ===========================================================
	public void destroy();
	
	public int add(int type, IColNode n);	
	//public IColNode remove(int id);
	
	// === Getters =============================================================
	public IColField getColField();
	public double getX();
	public double getY();
	
	// === Setters =============================================================
	public void setPos(double x, double y);
	
}
