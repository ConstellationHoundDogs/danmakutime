package nl.weeaboo.dt.collision;

import java.awt.geom.Rectangle2D;

public class RectColNode extends AbstractColNode {

	private Rectangle2D.Double rect;
	private double centerOffsetX, centerOffsetY;
	
	public RectColNode(double x, double y, double w, double h) {
		rect = new Rectangle2D.Double(x, y, w, h);

		centerOffsetX = rect.x + .5 * rect.width;
		centerOffsetY = rect.y + .5 * rect.height;
	}
	
	//Functions
	@Override
	public boolean intersects(IColNode c) {
		if (c instanceof RectColNode) {
			return ColUtil.intersectRectRect(this, (RectColNode)c);
		} else if (c instanceof CircleColNode) {
			return ColUtil.intersectCircleRect((CircleColNode)c, this);
		} else if (c instanceof LineSegColNode) {
			return ColUtil.intersectLineSegRect((LineSegColNode)c, this);
		}
		return ColUtil.intersectUndefined(this, c);
	}
	
	//Getters
	public double getX0() {
		return super.getCenterX() + rect.x;
	}

	public double getY0() {
		return super.getCenterY() + rect.y;
	}

	public double getWidth() {
		return rect.width;
	}

	public double getHeight() {
		return rect.height;
	}
	
	@Override
	public double getBoundingRectangleRadius() {
		return .5 * Math.max(rect.width, rect.height);
	}
	
	@Override
	public double getCenterX() {
		return super.getCenterX() + centerOffsetX;
	}
	
	@Override
	public double getCenterY() {
		return super.getCenterY() + centerOffsetY;
	}
	
	//Setters
	
}
