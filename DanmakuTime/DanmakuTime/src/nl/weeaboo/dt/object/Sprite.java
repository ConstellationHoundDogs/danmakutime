package nl.weeaboo.dt.object;

import nl.weeaboo.common.FastMath;
import nl.weeaboo.common.Log;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;
import nl.weeaboo.dt.lua.link.LuaLinkedObject;
import nl.weeaboo.dt.lua.link.LuaObjectLink;

import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

public class Sprite extends Drawable implements ISprite, LuaLinkedObject {

	protected LuaObjectLink luaLink;
	
	private boolean speedVecDirty;
	private double speed, speedInc;
	private double speedX, speedY;
	private double angle, angleInc;
	private boolean drawAngleAuto;
	
	public Sprite() {
		/*
		angle = 256;
		speed = speedY = 2;
		*/
	}
	
	//Functions
	@Override
	public void init(LuaRunState runState, LuaState vm, LUserData udata)
		throws LuaException
	{
		IField field = runState.getField(0);
		field.add(this);
		
		luaLink = new LuaObjectLink(runState, vm, udata);
	}
	
	@Override
	public LValue call(String methodName, Object... args) throws LuaException {
		return luaLink.call(methodName, args);
	}
		
	public Object test(String arg0, double arg1) {
		System.out.println(arg0 + " " + arg1);
		return this;
	}

	@Override
	public void update(IInput input) {
		if (luaLink != null && !luaLink.isFinished()) {
			try {
				luaLink.update();
			} catch (LuaException e) {
				Log.warning(e);
				luaLink = null;
			}
		}
		
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
