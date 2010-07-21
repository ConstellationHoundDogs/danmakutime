package nl.weeaboo.dt.lua.link;

import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;

import org.luaj.vm.LUserData;
import org.luaj.vm.LuaState;

public interface LuaLinkedObject {

	public void init(LuaRunState rs, LuaState vm, LUserData udata) throws LuaException;
		
}
