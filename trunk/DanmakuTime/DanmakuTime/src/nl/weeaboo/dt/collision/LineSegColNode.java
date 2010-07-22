package nl.weeaboo.dt.collision;

import java.awt.geom.Point2D;

import nl.weeaboo.common.FastMath;

public class LineSegColNode extends AbstractColNode implements IRotateableColNode {

	private Point2D.Double bp0;
	private Point2D.Double bp1;
	private double thickness;
	private double angle;
	
	//Cached	
	private boolean posDirty;
	private Point2D.Double p0;
	private Point2D.Double p1;
	private boolean lengthDirty;
	private double length;
	private boolean unitLengthDirty;
	private double unitLengthX, unitLengthY;
	
	public LineSegColNode(double dx0, double dy0, double dx1, double dy1,
			double thickness)
	{
		bp0 = new Point2D.Double(dx0, dy0);
		bp1 = new Point2D.Double(dx1, dy1);
		p0 = new Point2D.Double(bp0.x, bp0.y);
		p1 = new Point2D.Double(bp0.x, bp1.y);
		
		this.thickness = thickness;
		this.lengthDirty = true;
		this.unitLengthDirty = true;
	}
	
	//Functions
	@Override
	public boolean intersects(IColNode c) {
		if (c instanceof CircleColNode) {
			return ColUtil.intersectCircleLineSeg((CircleColNode)c, this);
		} else if (c instanceof LineSegColNode) {
			return ColUtil.intersectLineSegLineSeg(this, (LineSegColNode)c);
		}
		return ColUtil.intersectUndefined(this, c);
	}
	
	protected void applyRotation() {
		double sinA = FastMath.fastSin((float)angle);
		double cosA = FastMath.fastCos((float)angle);
		
		p0.x = bp0.x*cosA - bp0.y*sinA;
		p0.y = bp0.x*sinA + bp0.y*cosA;
		
		p1.x = bp1.x*cosA - bp1.y*sinA;
		p1.y = bp1.x*sinA + bp1.y*cosA;
				
		posDirty = false;
	}
	
	protected void calculateUnitLengths() {
		if (posDirty) applyRotation();
		
		double length = getLength();
		
		unitLengthX = (p1.x - p0.x) / length; 
		unitLengthY = (p1.y - p0.y) / length;
		
		unitLengthDirty = false;
	}
	
	//Getters
	@Override
	public double getBoundingRectangleRadius() {
		return .5 * getLength() + thickness;
	}
	
	@Override
	public double getCenterX() {
		if (posDirty) applyRotation();
		return super.getCenterX() + .5 * (p1.x - p0.x);
	}

	@Override
	public double getCenterY() {
		if (posDirty) applyRotation();
		return super.getCenterY() + .5 * (p1.y - p0.y);
	}
	
	public double getX0() {
		if (posDirty) applyRotation();
		return super.getCenterX() + p0.x;
	}
	
	public double getY0() {
		if (posDirty) applyRotation();
		return super.getCenterY() + p0.y;
	}
	
	public double getX1() {
		if (posDirty) applyRotation();
		return super.getCenterX() + p1.x;
	}
	
	public double getY1() {
		if (posDirty) applyRotation();
		return super.getCenterY() + p1.y;
	}
	
	public double getThickness() {
		return thickness;
	}

	public double getLength() {
		if (lengthDirty) {
			double dx = bp1.x - bp0.x;
			double dy = bp1.y - bp0.y;			
			length = Math.sqrt(dx*dx + dy*dy);
			lengthDirty = false;
		}
		return length;
	}
	
	public double getUnitLengthX() {
		if (unitLengthDirty) calculateUnitLengths();
		return unitLengthX;
	}
	
	public double getUnitLengthY() {
		if (unitLengthDirty) calculateUnitLengths();
		return unitLengthY;
	}
		
	//Setters
	@Override
	public void setAngle(double a) {
		if (angle != a) {
			angle = a;
			
			posDirty = true;
			unitLengthDirty = true;
		}
	}
	
}
