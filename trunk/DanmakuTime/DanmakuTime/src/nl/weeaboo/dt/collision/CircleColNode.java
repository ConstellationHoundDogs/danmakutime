package nl.weeaboo.dt.collision;

public class CircleColNode extends AbstractColNode {

	private double radius;
	
	public CircleColNode(double r) {
		this.radius = r;
	}
	
	//Functions	
	@Override
	public boolean intersects(IColNode c) {
		if (c instanceof CircleColNode) {
			return ColUtil.intersectCircleCircle(this, (CircleColNode)c);
		} else if (c instanceof RectColNode) {
			return ColUtil.intersectCircleRect(this, (RectColNode)c);
		} else if (c instanceof LineSegColNode) {
			return ColUtil.intersectCircleLineSeg(this, (LineSegColNode)c);
		}
		return ColUtil.intersectUndefined(this, c);
	}

	@Override
	public double getBoundingCircleRadius() {
		return radius;
	}
	
	//Getters
	
	//Setters
	
}
