package nl.weeaboo.dt.object;

import java.awt.Rectangle;

import nl.weeaboo.common.FastMath;
import nl.weeaboo.dt.collision.ColHost;
import nl.weeaboo.dt.collision.IColHost;
import nl.weeaboo.dt.collision.IColNode;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.lua.link.LuaLinkedObject;
import nl.weeaboo.dt.renderer.ITexture;

public class Sprite extends Drawable implements ISprite, LuaLinkedObject {

	private IColHost colHost;
	private boolean speedVecDirty;
	private double speed, speedInc;
	private double speedX, speedY;
	private double angle, angleInc;
	protected boolean drawAngleAuto;
	protected boolean hasBeenInField, outOfBoundsDeath;
	private Rectangle visualBounds;
	
	public Sprite() {		
		clip = true;
		drawAngleAuto = true;
		outOfBoundsDeath = true;
	}
	
	//Functions
	@Override
	public void destroy() {
		super.destroy();
		
		if (isDestroyed()) {
			if (colHost != null) {
				colHost.destroy();
			}
		}
	}
	
	@Override
	public int addColNode(int type, IColNode c) {
		return colHost.add(type, c);
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
			int w, h;
			if (texture != null) {
				w = texture.getWidth();
				h = texture.getHeight();
			} else {
				w = h = 1;
			}
			
			final double scale = 1.4142135623730950488016887242097; //Math.sqrt(2)
			w = (int)Math.ceil(scale * w);
			h = (int)Math.ceil(scale * h);
			
			visualBounds = new Rectangle(0, 0, w, h);			
		}
		
		visualBounds.x = (int)Math.round(getX()) - (visualBounds.width>>1);
		visualBounds.y = (int)Math.round(getY()) - (visualBounds.height>>1);
		
		return visualBounds;
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
		super.setField(f);
		
		colHost = new ColHost(f.getColField());		
	}
}
