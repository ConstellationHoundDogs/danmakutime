package nl.weeaboo.dt.collision;

public abstract class AbstractColNode implements IColNode {

	private IColHost host;
	private int index;
	private int type;
	
	protected AbstractColNode() {		
	}
	
	//Functions	
	@Override
	public void onAttached(IColHost host, int index, int ty) {
		this.host = host;
		this.index = index;
		this.type = ty;
	}
	
	@Override
	public void onDetached() {
		host = null;
		index = 0;
		type = 0;
	}
	
	@Override
	public void onCollide(IColNode c) {
		host.onCollide(this, index, c);				
	}

	//Getters
	@Override
	public int getType() {
		return type;
	}
	
	@Override
	public double getCenterX() {
		return host.getX();
	}

	@Override
	public double getCenterY() {
		return host.getY();
	}
	
	//Setters
	
}
