package nl.weeaboo.dt.object;

public interface ISprite extends IDrawable {

	// === Functions ===========================================================

	// === Getters =============================================================
	public double getSpeed();
	public double getSpeedInc();
	public double getAngle();
	public double getAngleInc();
	
	// === Setters =============================================================
	public void setSpeed(double s);
	public void setSpeedInc(double si);
	public void setAngle(double a);
	public void setAngleInc(double ai);
	
}
