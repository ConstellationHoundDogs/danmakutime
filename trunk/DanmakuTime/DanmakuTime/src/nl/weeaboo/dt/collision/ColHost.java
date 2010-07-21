package nl.weeaboo.dt.collision;

import nl.weeaboo.dt.TinyMap;

public class ColHost implements IColHost {

	private IColField field;
	private IColHostCollisionHandler colHandler;
	private TinyMap<IColNode> nodes;
	private double x, y;
	
	public ColHost(IColField f, IColHostCollisionHandler h) {
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
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
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
	}
	
}
