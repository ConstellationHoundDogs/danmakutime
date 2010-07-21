package nl.weeaboo.dt.collision;

import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;

import org.luaj.vm.LUserData;
import org.luaj.vm.LuaState;

public class CircleColNode extends AbstractColNode {

	private double radius;
	
	public CircleColNode() {
		this(0);
	}
	public CircleColNode(double r) {
		this.radius = r;
	}
	
	//Functions
	public void init(LuaRunState rs, LuaState vm, LUserData udata) throws LuaException {
		if (vm.gettop() >= 1 && vm.isnumber(1)) {
			radius = vm.tonumber(1);
		}
	}
	
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
