package nl.weeaboo.dt.collision;

import nl.weeaboo.dt.lua.link.LuaLinkedObject;

public abstract class AbstractColNode implements IColNode, LuaLinkedObject {

	private IColHost host;
	private int type;
	
	protected AbstractColNode() {		
	}
	
	//Functions	
	@Override
	public void init(IColHost host, int ty) {
		this.host = host;
		this.type = ty;
	}
	
	@Override
	public void onCollide(IColNode c) {
		
		System.out.printf("COLLISION :: %s(%d) hit by %s(%d)\n",
				getClass().getSimpleName(), getType(),
				c.getClass().getSimpleName(), c.getType());
		
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
