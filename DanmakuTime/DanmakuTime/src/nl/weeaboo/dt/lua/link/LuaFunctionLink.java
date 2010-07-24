package nl.weeaboo.dt.lua.link;

import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;

import org.luaj.vm.LFunction;
import org.luaj.vm.LThread;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

public class LuaFunctionLink extends LuaLink {
	
	private LFunction func;
	
	private String funcName;
	private Object[] args;
		
	public LuaFunctionLink(LuaRunState runState, LuaState vm, String funcName, Object... args) {
		super(runState, vm);
		
		this.funcName = funcName;
		this.args = args;
	}
	public LuaFunctionLink(LuaRunState runState, LuaState vm, LFunction func, LValue... args) {
		super(runState, vm);
		
		this.func = func;
		this.args = args;
	}
	
	//Functions
	@Override
	protected void init() throws LuaException {
		if (func != null) {
			if (thread == null) {
				thread = new LThread(func, rootVM._G);
				vm = thread.vm;
			} else {
				vm.pushlvalue(func);
			}
			for (Object arg : args) {
				vm.pushlvalue((LValue)arg);
			}
		} else {
			pushCall(funcName, args);
		}
	}
	
	//Getters
	
	//Setters
	
}
