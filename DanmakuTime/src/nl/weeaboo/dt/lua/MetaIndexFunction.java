package nl.weeaboo.dt.lua;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

public class MetaIndexFunction extends LFunction {

	private static final Set<String> reserved = new HashSet<String>(Arrays.asList(
			"init", "update", "animate"));
	
	private LTable table;
	private LValue superTable;
	private boolean isGet;
	
	public MetaIndexFunction(LTable t, LValue st, boolean get) {
		table = t;
		superTable = st;
		isGet = get;
	}
	
	//Functions
	public boolean luaStackCall(LuaState vm) {
		//LValue instance = vm.topointer(2);
		LValue key = vm.topointer(3);
		
		if (isGet) {
			LValue retval = table.luaGetTable(vm, key);
			//System.out.println(table + " " + key + " " + retval);
			
			if (retval == LNil.NIL && superTable != null
				&& (!key.isString() || !reserved.contains(key.toJavaString())))
			{
				
				if (superTable instanceof LFunction) {
					LFunction func = (LFunction)superTable;
					return func.luaStackCall(vm);
				} else {					
					retval = superTable.luaGetTable(vm, key);
				}
			}
			vm.resettop();
			vm.pushlvalue(retval);
		} else {
			LValue value = vm.topointer(4);
			vm.resettop();

			//System.out.println(table + " " + key);
			table.put(key, value);
		}
		
		return false;
	}
	
	//Getters
	
	//Setters
	
}
