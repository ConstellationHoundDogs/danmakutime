package nl.weeaboo.dt.collision;

import java.awt.geom.Line2D;

import nl.weeaboo.dt.object.IDrawable;

public final class ColUtil {

	public static boolean intersectCircleCircle(CircleColNode c0, CircleColNode c1) {
		double dx = c1.getCenterX() - c0.getCenterX();
		double dy = c1.getCenterY() - c0.getCenterY();
		double r = c0.getBoundingRectangleRadius() + c1.getBoundingRectangleRadius();
		
		return (dx*dx) + (dy*dy) <= (r*r);
	}

	public static boolean intersectCircleLineSeg(CircleColNode c0, LineSegColNode c1) {
		double distSq = Line2D.ptSegDistSq(c1.getX0(), c1.getY0(), c1.getX1(), c1.getY1(),
				c0.getCenterX(), c0.getCenterY());
		double r = c0.getBoundingRectangleRadius() + c1.getThickness();
		return distSq <= (r*r);
	}
	
	public static boolean intersectLineSegLineSeg(LineSegColNode c0, LineSegColNode c1) {
		double r = c0.getThickness() + c1.getThickness();
		double rsq = r * r;
		
		Line2D.Double l0 = new Line2D.Double(c0.getX0(), c0.getY0(), c0.getX1(), c0.getY1());
		Line2D.Double l1 = new Line2D.Double(c1.getX0(), c1.getY0(), c1.getX1(), c1.getY1());
		
		return l0.ptLineDistSq(l1.x1, l1.y1) <= rsq
			|| l0.ptLineDistSq(l1.x2, l1.y2) <= rsq
			|| l1.ptLineDistSq(l0.x1, l0.y1) <= rsq
			|| l1.ptLineDistSq(l0.x2, l0.y2) <= rsq
			|| l0.intersectsLine(l1);
	}
	
	public static boolean intersectUndefined(IColNode c0, IColNode c1) {
		throw new IllegalArgumentException(String.format(
				"Undefined combination of collision node types: %s <-> %s",
				c0.getClass().getSimpleName(), c1.getClass().getSimpleName()));
	}
	
	public static void collide(IColNode a, IColNode b, boolean atob, boolean btoa) {
		if (a == b) {
			return;
		}
		
		IDrawable ao = a.getHost().getOwner();
		IDrawable bo = b.getHost().getOwner();
		if (ao == null || ao.isDestroyed() || bo == null || bo.isDestroyed()) {
			return;
		}
		
		if (a.intersects(b)) {
			if (atob) {
				a.onCollide(b);
			}
			if (btoa) {
				b.onCollide(a);
			}			
		}
	}
	
}
