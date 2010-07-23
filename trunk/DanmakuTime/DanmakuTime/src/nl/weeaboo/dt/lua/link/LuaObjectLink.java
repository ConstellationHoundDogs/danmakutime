package nl.weeaboo.dt.lua.link;

import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;

import org.luaj.vm.LFunction;
import org.luaj.vm.LString;
import org.luaj.vm.LUserData;
import org.luaj.vm.LuaState;

public class LuaObjectLink extends LuaLink {

	public final LUserData self;
	
	public LuaObjectLink(LuaRunState runState, LuaState vm, LUserData self) {
		super(runState, vm);
		
		this.self = self;
	}
	
	//Functions
	protected int pushMethod(String methodName) {
		int result = super.pushMethod(methodName);
		if (result <= 0) return result;
		
		vm.pushlvalue(self);
		return result + 1;
	}
	
	protected LFunction getMethod(String methodName) {
		rootVM.pushlvalue(self);		
		rootVM.getfield(-1, LString.valueOf(methodName));
		if (!rootVM.isfunction(-1)) {
			rootVM.pop(2);
			return null;
		}		
		
		LFunction func = (LFunction)rootVM.poplvalue(); //Method
		rootVM.pop(1); //Table used for method lookup
		return func;
	}
	
	public void init() throws LuaException {
		inited = true;
		
		int pushed = pushMethod("update");
		finished = (pushed <= 0);
	}
	
	//Getters
	
	//Setters
	
}
