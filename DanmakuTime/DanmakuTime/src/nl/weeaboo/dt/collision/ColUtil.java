package nl.weeaboo.dt.collision;

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
	
}
