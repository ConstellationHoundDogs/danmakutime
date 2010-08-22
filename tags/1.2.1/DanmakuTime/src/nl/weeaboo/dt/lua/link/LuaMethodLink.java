package nl.weeaboo.dt.lua.link;

import nl.weeaboo.dt.lua.LuaRunState;

import org.luaj.vm.LFunction;
import org.luaj.vm.LString;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

public class LuaMethodLink extends LuaFunctionLink {

	public final LUserData self;
	
	public LuaMethodLink(LuaRunState runState, LuaState vm, LUserData self,
			String methodName, Object... args)
	{
		super(runState, vm, methodName, args);
		
		this.self = self;
	}
	
	public LuaMethodLink(LuaRunState runState, LuaState vm, LUserData self,
			LFunction func, LValue... args)
	{
		super(runState, vm, func, args);

		this.self = self;
	}
	
	//Functions
	protected int pushFunc(LFunction func) {
		int result = super.pushFunc(func);
		if (result <= 0) return result;
		
		vm.pushlvalue(self);
		return result + 1;
	}
	
	protected LFunction getFunc(String methodName) {
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
	
	//Getters
	
	//Setters
	
}
