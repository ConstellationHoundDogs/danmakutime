package nl.weeaboo.dt.collision;

import nl.weeaboo.dt.TinyMap;
import nl.weeaboo.dt.object.IDrawable;

public class ColHost implements IColHost {

	private IDrawable owner;
	private IColField field;
	private IColHostCollisionHandler colHandler;
	private TinyMap<IColNode> nodes;
	private boolean hasRotateable;
	private double x, y;
	private double angle;
	
	public ColHost(IDrawable o, IColField f, IColHostCollisionHandler h) {
		owner = o;
		field = f;
		colHandler = h;
		nodes = new TinyMap<IColNode>();
	}
	
	//Functions
	@Override
	public void destroy() {
		for (IColNode node : nodes.getValues()) {
			field.remove(node);
			node.onDetached();
		}
		nodes.clear();
		hasRotateable = false;
	}
	
	@Override
	public void onCollide(IColNode child, int childIndex, IColNode other) {
		colHandler.onCollide(child, childIndex, other);
	}
		
	//Getters
	@Override
	public IColField getColField() {
		return field;
	}
	
	@Override
	public IDrawable getOwner() {
		return owner;
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
	public double getAngle() {
		return angle;
	}	
	
	//Setters
	@Override
	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void setColNode(int index, int type, IColNode n) {
		IColNode old = nodes.put(index, n);
		if (old != null && old != n) {
			old.onDetached();			
		}

		n.onAttached(this, index, type);
		field.add(n);

		if (n instanceof IRotateableColNode) {
			hasRotateable = true;
		} else {
			if (old instanceof IRotateableColNode) {
				//We may have removed the last rotateable col node
				
				hasRotateable = false;
				for (IColNode cn : nodes.getValues()) {
					if (cn instanceof IRotateableColNode) {
						hasRotateable = true;
						break;
					}
				}
			}
		}
		
	}

	@Override
	public void setAngle(double a) {
		if (angle != a) {
			angle = a;
			
			if (hasRotateable) {
				for (IColNode node : nodes.getValues()) {
					if (node instanceof LineSegColNode) {
						((LineSegColNode)node).setAngle(angle);
					}
				}
			}
		}
	}
	
}
