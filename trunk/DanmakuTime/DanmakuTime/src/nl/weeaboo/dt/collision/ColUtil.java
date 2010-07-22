package nl.weeaboo.dt.collision;

import java.awt.geom.Point2D;

import nl.weeaboo.dt.DTLog;
import nl.weeaboo.dt.object.IDrawable;

public final class ColUtil {

	public static boolean intersectCircleCircle(CircleColNode c0, CircleColNode c1) {
		double dx = c1.getCenterX() - c0.getCenterX();
		double dy = c1.getCenterY() - c0.getCenterY();
		double r = c0.getBoundingRectangleRadius() + c1.getBoundingRectangleRadius();
		
		return (dx*dx) + (dy*dy) <= (r*r);
	}

	public static boolean intersectCircleLineSeg(CircleColNode c0, LineSegColNode c1) {
		double c0x = c0.getCenterX();
		double c0y = c0.getCenterY();

		Point2D p = closestPointOnLineSegment(c1, c0x, c0y);
		
		double dx = c0x - p.getX();
		double dy = c0y - p.getY();
		double r = c0.getBoundingRectangleRadius() + c1.getThickness();

		//System.out.printf("(%.2f,%.2f) (dx=%.2f,dy=%.2f) %.2f\n",
				//c1.getCenterX(), c1.getCenterY(), dx, dy, r);
		
		return (dx*dx) + (dy*dy) <= (r*r);
	}

	public static boolean intersectLineSegLineSeg(LineSegColNode c0, LineSegColNode c1) {
		DTLog.message("lineseg <-> lineseg not implemented yet");
		return false;
	}
	
	public static boolean intersectUndefined(IColNode c0, IColNode c1) {
		throw new IllegalArgumentException(String.format(
				"Undefined combination of collision node types: %s <-> %s",
				c0.getClass().getSimpleName(), c1.getClass().getSimpleName()));
	}

	public static Point2D closestPointOnLineSegment(LineSegColNode node, double x, double y) {
		double x0 = node.getX0();
		double y0 = node.getY0();
		double dx = x - x0;
		double dy = y - y0;

		double ux = node.getUnitLengthX();
		double uy = node.getUnitLengthY();

		double t = Math.abs(ux*dx) + Math.abs(uy*dy);
		
		if (t <= 0) {
			return new Point2D.Double(x0, y0);
		} else if (t >= node.getLength()) {
			return new Point2D.Double(node.getX1(), node.getY1());
		} else {
			return new Point2D.Double(x0 + ux * t, y0 + uy * t);
		}
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
