package nl.weeaboo.dt.lua.link;

import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;

import org.luaj.vm.LUserData;
import org.luaj.vm.LuaState;

public class LuaMethodLink extends LuaObjectLink {

	private String methodName;
	
	public LuaMethodLink(LuaRunState runState, LuaState vm, LUserData self,
			String methodName)
	{
		super(runState, vm, self);
		
		this.methodName = methodName;
	}
	
	//Functions
	public void init() throws LuaException {
		pushMethod(methodName);
		finished = false;
	}
	
	//Getters
	
	//Setters
	
}
