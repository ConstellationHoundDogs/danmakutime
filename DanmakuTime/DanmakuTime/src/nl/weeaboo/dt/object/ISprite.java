package nl.weeaboo.dt.object;

import java.awt.Rectangle;

public interface ISprite extends IDrawable {

	// === Functions ===========================================================

	// === Getters =============================================================
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
