package nl.weeaboo.dt.object;

import java.awt.Rectangle;

import nl.weeaboo.dt.collision.IColHost;
import nl.weeaboo.dt.collision.IColNode;

public interface ISprite extends IDrawable {

	// === Functions ===========================================================
	public int addColNode(int type, IColNode c);
	
	// === Getters =============================================================
	public IColHost getColHost();
	
	public double getSpeed();
	public double getSpeedInc();
	public double getAngle();
	public double getAngleInc();
	
	public Rectangle getVisualBounds();
	
	// === Setters =============================================================
	public void setSpeed(double s);
	public void setSpeedInc(double si);
	public void setAngle(double a);
	public void setAngleInc(double ai);
	
	public void setDrawAngleAuto(boolean a);
	public void setOutOfBoundsDeath(boolean d);
	
}
