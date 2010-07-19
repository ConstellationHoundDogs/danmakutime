package nl.weeaboo.dt.lua.link;

import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;

import org.luaj.lib.j2se.CoerceJavaToLua;
import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LThread;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;

public abstract class LuaLink {

	protected final LuaRunState runState;
	protected final LuaState rootVM;
	protected LThread thread;
	protected LuaState vm;
	
	protected int wait;
	protected boolean inited;
	protected boolean finished;
	
	public LuaLink(LuaRunState runState, LuaState vm) {
		this.runState = runState;
		this.rootVM = vm;
	}
	
	//Functions
	protected int pushMethod(String methodName) {
		LFunction func = getMethod(methodName);
		if (func == null) {
			return 0;
		}
		
		if (thread == null) {
			thread = new LThread(func, rootVM._G);
			vm = thread.vm;
		} else {
			vm.pushlvalue(func);
		}
		return 1;
	}
	
	public LFunction getMethod(String methodName) {
		rootVM.getglobal(methodName);
		if (!rootVM.isfunction(-1)) {
			rootVM.pop(1);
			return null;
		}		
		return (LFunction)rootVM.poplvalue();
	}

	protected int pushCall(String methodName, Object... args) throws LuaException {		
		int methodPushed = pushMethod(methodName);
		if (methodPushed <= 0) {
			if (vm != null) {
				vm.pop(-pushMethod(methodName));
			}
			//throw new LuaException(String.format("function \"%s\" not found", methodName));
			return 0;
		}

		for (Object arg : args) {
			vm.pushlvalue(CoerceJavaToLua.coerce(arg));
		}
		
		return (methodPushed-1) + args.length;
	}
	
	public LValue call(String methodName, Object... args) throws LuaException {
		LValue result = LNil.NIL;
		
		runState.setCurrentLink(this);		
		try {
			int pushed = pushCall(methodName, args);
			if (pushed > 0) {
				vm.call(pushed, 1);
				result = vm.poplvalue();
			}
		} catch (LuaErrorException e) {
			if (vm != null) vm.pop(1);
			if (e.getCause() instanceof NoSuchMethodException) {
				//Ignore methods that don't exist
			} else {
				throw new LuaException(e.getMessage(), e.getCause());
			}
		} catch (RuntimeException e) {
			if (vm != null) vm.pop(1);
			throw new LuaException(e);
		} finally {
			runState.setCurrentLink(null);
		}
		
		return result;
	}

	protected abstract void init() throws LuaException;
	
	public void update() throws LuaException {
		if (!inited) {
			inited = true;
			init();
		}
		
		if (thread == null || finished
				|| thread.getStatusCode() == LThread.STATUS_DEAD)
		{
			finished = true;
			return;
		}
		
		if (wait > 0) {			
			wait--;
			if (wait > 0) {
				return;
			}
		}
		
		runState.setCurrentLink(this);		
		try {
			thread.resumeFrom(vm, 0);
		} catch (LuaErrorException e) {
			if (e.getCause() instanceof NoSuchMethodException) {
				//Ignore methods that don't exist
			} else {
				finished = true;
				throw new LuaException(e.getMessage(), e.getCause());
			}
		} catch (RuntimeException e) {
			finished = true;
			throw new LuaException(e);
		} finally {
			runState.setCurrentLink(null);
			if (thread.getStatusCode() == LThread.STATUS_RUNNING) {
				finished = true;
			}
		}
		
		if (finished) {
			thread = null;
		}
	}
	
	//Getters
	public final boolean isFinished() {
		return finished;
	}
	public boolean isCurrent() {
		return runState.getCurrentLink() == this;
	}
	public int getWait() {
		return wait;
	}
	
	//Setters
	public void setWait(int w) { wait = w; }
	
}
