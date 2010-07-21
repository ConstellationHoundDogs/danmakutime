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
		}
		return ColUtil.intersectUndefined(this, c);
	}

	@Override
	public double getBoundingRectangleRadius() {
		return radius;
	}
	
	//Getters
	
	//Setters
	
}
