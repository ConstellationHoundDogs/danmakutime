package nl.weeaboo.dt.lua;

import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.lua.link.LuaLinkedObject;

import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.vm.LFunction;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;

public class ConstructorFunction<T extends LuaLinkedObject> extends LFunction {

	private LuaRunState runState;
	private Class<T> clazz;
	
	public ConstructorFunction(LuaRunState rs, Class<T> c) {
		runState = rs;
		clazz = c;		
	}
	
	//Functions	
	public int invoke(LuaState vm) {
		LuaLink oldLink = runState.getCurrentLink();
		
		try {
			final T javaInstance = clazz.newInstance();
						
			LTable table;
			if (vm.gettop() >= 1) {
				table = vm.checktable(1);
			} else {
				table = new LTable();
			}
					
			//LUserData's metatable is mutable, but shared among all instances of a Java class
			LUserData sharedUserData = LuajavaLib.toUserdata(javaInstance, clazz);
			LTable sharedMeta = sharedUserData.luaGetMetatable();
			
			LTable meta = new LTable();
			
			LFunction metaIndex = (LFunction)sharedMeta.get(LValue.TM_INDEX);
			LFunction metaNewIndex = (LFunction)sharedMeta.get(LValue.TM_NEWINDEX);
			
			meta.put(LValue.TM_INDEX, new MetaIndexFunction(table, metaIndex, true));
			meta.put(LValue.TM_NEWINDEX, new MetaIndexFunction(table, metaNewIndex, false));		
			
			LUserData udata = new LUserData(javaInstance, meta);
			udata.luaSetTable(vm, LString.valueOf("super"), udata);
			
			javaInstance.init(runState, vm, udata);
			
			vm.pushlvalue(udata);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			throw new LuaErrorException(e);
		} finally {
			runState.setCurrentLink(oldLink);
		}
	}
	
	//Getters
	
	//Setters
	
}
