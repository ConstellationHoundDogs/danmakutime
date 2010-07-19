package nl.weeaboo.dt.object;

import nl.weeaboo.common.FastMath;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.lua.link.LuaLinkedObject;

public class Sprite extends Drawable implements ISprite, LuaLinkedObject {
	
	private boolean speedVecDirty;
	private double speed, speedInc;
	private double speedX, speedY;
	private double angle, angleInc;
	private boolean drawAngleAuto;
	
	public Sprite() {
		setClip(true);
	}
	
	//Functions		
	public Object test(String arg0, double arg1) {
		System.out.println(arg0 + " " + arg1);
		return this;
	}

	@Override
	public void update(IInput input) {
		super.update(input);
		
		//Update speed/angle/pos
	    speed += speedInc;

	    if (angleInc != 0) {
	    	setAngle(angle + angleInc);
	    }

	    if (speedVecDirty || speedInc != 0 || angleInc != 0) {
	        speedVecDirty = false;
	        
	        speedX = speed * FastMath.fastSin((float)angle);
	        speedY = -speed * FastMath.fastCos((float)angle);
	    }

		x += speedX;
		y += speedY;
	}
	
	//Getters
	@Override
	public double getAngle() {
		return angle;
	}

	@Override
	public double getAngleInc() {
		return angleInc;
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	@Override
	public double getSpeedInc() {
		return speedInc;
	}
	
	//Setters
	@Override
	public void setAngle(double a) {
		a %= 512.0;
		
		angle = a;
		speedVecDirty = true;
		if (drawAngleAuto) {
			setDrawAngle(angle);
		}
	}

	@Override
	public void setAngleInc(double ai) {
		angleInc = ai;
	}

	@Override
	public void setSpeed(double s) {
		speed = s;
		speedVecDirty = true;
	}

	@Override
	public void setSpeedInc(double si) {
		speedInc = si;
	}

	@Override
	public void setDrawAngleAuto(boolean a) {
		drawAngleAuto = a;
	}
		
}
