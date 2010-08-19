package nl.weeaboo.dt.object;

import java.awt.Rectangle;

import nl.weeaboo.common.FastMath;
import nl.weeaboo.dt.DTLog;
import nl.weeaboo.dt.collision.ColHost;
import nl.weeaboo.dt.collision.IColHost;
import nl.weeaboo.dt.collision.IColHostCollisionHandler;
import nl.weeaboo.dt.collision.IColNode;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.link.LuaLinkedObject;
import nl.weeaboo.dt.renderer.ITexture;

public class Sprite extends Drawable implements ISprite, LuaLinkedObject,
	IColHostCollisionHandler
{

	private IColHost colHost;
	private boolean speedVecDirty;
	private double speed, speedInc;
	private double speedX, speedY;
	private double angle, angleInc;
	protected boolean drawAngleAuto;
	protected boolean hasBeenInField, outOfBoundsDeath;
	private Rectangle visualBounds;
	
	public Sprite() {
		drawAngleAuto = true;
		outOfBoundsDeath = true;
	}
	
	//Functions	
	
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

		setPos(getX() + speedX, getY() + speedY);
		
		//Bounds check
		if (outOfBoundsDeath && field != null) {
			Rectangle fieldBounds = new Rectangle(0, 0, field.getWidth(), field.getHeight());
			Rectangle imageBounds = getVisualBounds();
			
			if (fieldBounds.intersects(imageBounds)) {
				hasBeenInField = true;
			} else {
				fieldBounds.grow(field.getPadding(), field.getPadding());				
				if (hasBeenInField || !fieldBounds.intersects(imageBounds)) {				
					destroy();
					return;
				}
			}
		}
	}
	
	@Override
	public void onCollide(IColNode child, int childIndex, IColNode other) {
		if (isDestroyed()) {
			return;
		}

		/*
		System.out.printf("COLLISION :: %s(%d) [#%d] hit by %s(%d)\n",
				child.getClass().getSimpleName(), child.getType(), childIndex,
				other.getClass().getSimpleName(), other.getType());
		*/
		
		if (updateLink != null) {
			try {
				IDrawable owner = other.getHost().getOwner();
				if (owner != null) {
					updateLink.call(true, "onCollision", owner.getLuaObject(), child, other);
				}
			} catch (LuaException e) {
				DTLog.warning(e);
			}
		}		
	}
	
	//Getters
	@Override
	public IColHost getColHost() {
		return colHost;
	}
	
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
	
	@Override
	public Rectangle getVisualBounds() {
		if (visualBounds == null) {			
			final double scale = 1.4142135623730950488016887242097; //Math.sqrt(2)
			int iw = (int)Math.ceil(scale * getWidth());
			int ih = (int)Math.ceil(scale * getHeight());
			
			visualBounds = new Rectangle(0, 0, iw, ih);			
		}
		
		visualBounds.x = ((int)getX()) - (visualBounds.width>>1);
		visualBounds.y = ((int)getY()) - (visualBounds.height>>1);
		
		return visualBounds;
	}
	
	@Override
	public boolean isDrawAngleAuto() {
		return drawAngleAuto;
	}

	@Override
	public boolean getOutOfBoundsDeath() {
		return outOfBoundsDeath;
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
	
	@Override
	public void setDrawAngle(double a) {
		super.setDrawAngle(a);
		
		colHost.setAngle(a);
	}

	@Override
	public void setOutOfBoundsDeath(boolean d) {
		outOfBoundsDeath = d;
	}
	
	@Override
	public void setTexture(ITexture tex) {
		super.setTexture(tex);
		
		visualBounds = null;
	}
	
	@Override
	public void setPos(double x, double y) {
		super.setPos(x, y);
		
		colHost.setPos(x, y);
	}
		
	
	@Override
	public void setField(IField f) {
		if (colHost != null) {
			colHost.destroy();
			colHost = null;
		}
		
		super.setField(f);
		
		if (f != null) {
			colHost = new ColHost(this, f.getColField(), this);
		}
	}

	@Override
	public void setColNode(int index, int type, IColNode c) {
		colHost.setColNode(index, type, c);
	}
	
	@Override
	public void setScaleX(double sx) {
		super.setScaleX(sx);
		
		visualBounds = null;
	}
	
	@Override
	public void setScaleY(double sy) {
		super.setScaleY(sy);
		
		visualBounds = null;
	}
	
}
