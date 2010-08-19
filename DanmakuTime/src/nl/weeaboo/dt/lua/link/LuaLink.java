package nl.weeaboo.dt.lua.link;

import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;
import nl.weeaboo.dt.lua.platform.CoerceJavaToLua;

import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LThread;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;

public abstract class LuaLink {

	public final LuaRunState runState;
	public final LuaState rootVM;
	protected LThread thread;
	protected LuaState vm;
	
	protected int wait;
	private boolean inited;
	protected boolean finished;
	
	public LuaLink(LuaRunState runState, LuaState vm) {
		this.runState = runState;
		this.rootVM = vm;
	}
	
	//Functions
	public void destroy() {
		finished = true;
	}
	
	protected int pushFunc(String methodName) {
		LFunction func = getFunc(methodName);
		if (func == null) {
			return 0;
		}		
		return pushFunc(func);
	}
	
	protected int pushFunc(LFunction func) {
		if (thread == null) {
			thread = new LThread(func, rootVM._G);
			vm = thread.vm;
		} else {
			vm.pushlvalue(func);
		}
		return 1;
	}
	
	protected LFunction getFunc(String methodName) {
		rootVM.getglobal(methodName);
		if (!rootVM.isfunction(-1)) {
			rootVM.pop(1);
			return null;
		}		
		return (LFunction)rootVM.poplvalue();
	}

	protected int pushCall(String methodName, Object... args) throws LuaException {		
		int methodPushed = pushFunc(methodName);
		if (methodPushed <= 0) {
			if (vm != null) {
				vm.pop(-pushFunc(methodName));
			}
			//throw new LuaException(String.format("function \"%s\" not found", methodName));
			return 0;
		}

		if (args != null) {
			for (Object arg : args) {
				if (arg instanceof LValue) {
					vm.pushlvalue((LValue)arg);
				} else {
					vm.pushlvalue(CoerceJavaToLua.coerce(arg));
				}
			}
		}
		
		return (methodPushed-1) + (args != null ? args.length : 0);
	}
	
	public LValue call(boolean ignoreMissing,
			String methodName, Object... args) throws LuaException
	{
		LValue result = LNil.NIL;
		
		LuaLink oldLink = runState.getCurrentLink();
		runState.setCurrentLink(this);		
		try {
			int pushed = pushCall(methodName, args);
			if (pushed > 0) {
				vm.call(pushed, 1);
				result = vm.poplvalue();
			}
		} catch (LuaErrorException e) {
			if (vm != null) vm.pop(1);
			
			if (ignoreMissing && e.getCause() instanceof NoSuchMethodException) {
				//Ignore methods that don't exist
			} else {
				e.printStackTrace();
				throw new LuaException(e.getMessage(), e.getCause() != null ? e.getCause() : e);
			}
		} catch (RuntimeException e) {
			if (vm != null) vm.pop(1);
			throw new LuaException(e);
		} finally {
			runState.setCurrentLink(oldLink);
		}
		
		return result;
	}

	protected abstract void init() throws LuaException;
	
	public void update() throws LuaException {
		if (!inited) {
			inited = true;
			init();
		}
		
		if (finished) {
			return;
		} else if (thread == null || thread.getStatusCode() == LThread.STATUS_DEAD) {
			destroy();
			return;
		}
		
		if (wait > 0) {			
			wait--;
			if (wait > 0) {
				return;
			}
		}
		
		LuaLink oldLink = runState.getCurrentLink();
		runState.setCurrentLink(this);		
		try {
			thread.resumeFrom(vm, 0);
		} catch (LuaErrorException e) {
			destroy();
			if (e.getCause() instanceof NoSuchMethodException) {
				throw new LuaException(e.getCause().getMessage());
			} else {
				throw new LuaException(e.getMessage(), e.getCause() != null ? e.getCause() : e);
			}
		} catch (RuntimeException e) {
			destroy();
			throw new LuaException(e);
		} finally {
			runState.setCurrentLink(oldLink);
			if (finished || thread.getStatusCode() == LThread.STATUS_RUNNING) {
				destroy();
				thread = null;
			}
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
