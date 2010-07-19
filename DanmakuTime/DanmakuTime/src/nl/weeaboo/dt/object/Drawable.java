package nl.weeaboo.dt.object;

import nl.weeaboo.dt.renderer.IRenderer;
import nl.weeaboo.dt.renderer.ITexture;

public abstract class Drawable implements IDrawable {

	private boolean destroyed;
	
	protected ITexture texture;
	protected double x, y;
	private short z;
	private double drawAngle;
	
	public Drawable() {		
	}
	
	//Functions
	@Override
	public void destroy() {
		destroyed = true;
	}
	
	@Override
	public void draw(IRenderer renderer) {
		if (texture == null) return;
		
		int tw = texture.getWidth();
		int th = texture.getHeight();
		
		renderer.setTexture(texture);
		renderer.drawRotatedQuad(x, y, tw, th, z, drawAngle);
	}
	
	//Getters
	@Override
	public boolean isDestroyed() {
		return destroyed;
	}
	
	@Override
	public double getDrawAngle() {
		return drawAngle;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}
	
	@Override
	public ITexture getTexture() {
		return texture;
	}
	
	@Override
	public short getZ() {
		return z;
	}
	
	//Setters
	@Override
	public void setDrawAngle(double a) {
		drawAngle = a;
	}

	@Override
	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void setTexture(ITexture tex) {
		texture = tex;
	}
	
	@Override
	public void setZ(short z) {
		this.z = z;
	}
	
	/**
	 * Ease-of use method for setting Z. WARNING: Z will be truncated to a
	 * short.
	 * 
	 * @param z The new Z-coordinate
	 */
	public void setZ(int z) {
		setZ((short) z);
	}
	
}
