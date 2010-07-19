package nl.weeaboo.dt.lua;

import nl.weeaboo.dt.lua.link.LuaLink;

import org.luaj.vm.LuaState;

public class LuaFunctionLink extends LuaLink {
	
	private String funcName;
	private Object[] args;
	
	public LuaFunctionLink(LuaRunState runState, LuaState vm, String funcName, Object... args) {
		super(runState, vm);
		
		this.funcName = funcName;
		this.args = args;
	}
	
	//Functions
	@Override
	protected void init() throws LuaException {
		pushCall(funcName, args);
	}
	
	//Getters
	
	//Setters
	
}
