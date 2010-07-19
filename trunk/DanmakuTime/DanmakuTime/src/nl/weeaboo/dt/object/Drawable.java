package nl.weeaboo.dt.object;

import nl.weeaboo.dt.renderer.IRenderer;
import nl.weeaboo.dt.renderer.ITexture;

public abstract class Drawable implements IDrawable {

	private boolean destroyed;
	
	protected ITexture texture;
	protected double x, y;
	protected double drawAngle;
	
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
		renderer.drawRotatedQuad(x, y, tw, th, drawAngle);
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
	
}
