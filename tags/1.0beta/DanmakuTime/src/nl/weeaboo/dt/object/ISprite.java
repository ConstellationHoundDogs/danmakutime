package nl.weeaboo.dt.object;

import java.awt.Rectangle;

import nl.weeaboo.dt.collision.IColHost;
import nl.weeaboo.dt.collision.IColNode;

public interface ISprite extends IDrawable {

	// === Functions ===========================================================
	
	/**
	 * @see nl.weeaboo.dt.collision.IColHost#setColNode(int, int, IColNode)
	 */
	public void setColNode(int index, int type, IColNode c);
	
	// === Getters =============================================================
	/**
	 * @return The colhost object for this sprite
	 */
	public IColHost getColHost();
	
	/**
	 * @return The current speed
	 */
	public double getSpeed();
	
	/**
	 * @return The current acceleration (speed increase)
	 */
	public double getSpeedInc();
	
	/**
	 * @return The current rotation, between <code>0.0</code> and
	 *         <code>512.0</code>
	 */
	public double getAngle();
	
	/**
	 * @return The current angular velocity (angle increase)
	 */
	public double getAngleInc();
	
	/**
	 * @return A bounding rectangle around the visual bounds of the sprite.
	 */
	public Rectangle getVisualBounds();
	
	/**
	 * @return <code>true</code> if the draw angle is locked to the angle
	 */
	public boolean isDrawAngleAuto();
	
	/**
	 * @return <code>true</code> if this sprite should be automatically
	 *         destroyed if it leaves the field it's in (determined by checking
	 *         if the visual bounds intersect with the field bounds).
	 */
	public boolean getOutOfBoundsDeath();
	
	// === Setters =============================================================
	
	/**
	 * Changes the speed. The motion vector has length=speed and rotation=angle
	 */
	public void setSpeed(double s);
	
	/**
	 * Changes the acceleration (speed increase) 
	 */
	public void setSpeedInc(double si);

	/**
	 * Changes the angle. The motion vector has length=speed and rotation=angle
	 * 
	 * Unless {@link #isDrawAngleAuto()} is <code>false</code>, changes to the
	 * angle get propagated to the draw angle.
	 */
	public void setAngle(double a);
	
	/**
	 * Changes the angular velocity (angle increase) 
	 */
	public void setAngleInc(double ai);
	
	/**
	 * @param a If <code>true</code>, changes to angle get propagated to the draw angle.
	 */
	public void setDrawAngleAuto(boolean a);
	
	/**
	 * @see #getOutOfBoundsDeath() 
	 */
	public void setOutOfBoundsDeath(boolean d);
	
}
