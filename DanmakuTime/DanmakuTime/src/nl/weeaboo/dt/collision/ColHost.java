package nl.weeaboo.dt.collision;

import nl.weeaboo.dt.TinyMap;

public class ColHost implements IColHost {

	private IColField field;
	private int idGenerator;
	private TinyMap<IColNode> nodes;
	private double x, y;
	
	public ColHost(IColField f) {
		field = f;
		nodes = new TinyMap<IColNode>();
	}
	
	//Functions
	@Override
	public void destroy() {
		for (IColNode node : nodes.getValues()) {
			field.remove(node);
		}
		nodes.clear();
	}
	
	@Override
	public int add(int type, IColNode n) {
		int id = ++idGenerator;

		n.init(this, type);
		
		nodes.put(id, n);
		field.add(n);
		
		return id;
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
	
}
