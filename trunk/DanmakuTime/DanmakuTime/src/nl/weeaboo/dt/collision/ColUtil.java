package nl.weeaboo.dt.collision;

import nl.weeaboo.dt.object.IDrawable;

public final class ColUtil {

	public static boolean intersectCircleCircle(CircleColNode c0, CircleColNode c1) {
		double dx = c1.getCenterX() - c0.getCenterX();
		double dy = c1.getCenterY() - c0.getCenterY();
		double r = c0.getBoundingRectangleRadius() + c1.getBoundingRectangleRadius();
		
		return (dx*dx) + (dy*dy) <= (r*r);
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
