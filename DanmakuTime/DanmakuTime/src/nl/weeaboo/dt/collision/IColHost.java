package nl.weeaboo.dt.collision;

import nl.weeaboo.dt.object.IDrawable;

public interface IColHost {

	// === Functions ===========================================================
	public void destroy();
	public void onCollide(IColNode child, int childIndex, IColNode other);
			
	// === Getters =============================================================
	public IDrawable getOwner();
	public IColField getColField();
	public double getX();
	public double getY();
	public double getAngle();
	
	// === Setters =============================================================
	public void setPos(double x, double y);
	public void setAngle(double a);
	public void setColNode(int index, int type, IColNode n);	
	
}
