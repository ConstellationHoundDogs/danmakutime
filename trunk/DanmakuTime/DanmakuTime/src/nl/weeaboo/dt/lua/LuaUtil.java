package nl.weeaboo.dt.lua;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Map.Entry;

import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.Keys;
import nl.weeaboo.dt.input.VKey;
import nl.weeaboo.dt.lua.link.LuaFunctionLink;
import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.lua.link.LuaLinkedObject;
import nl.weeaboo.dt.lua.platform.LuajavaLib;

import org.luaj.vm.LFunction;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LThread;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

public class LuaUtil {
	
	public static void installLuaLib(final LuaRunState rs, LTable globals) {
		fixCoroutineLib(globals);
		FastMathLib.install(globals, rs.getRandom());		
		
		//Custom yield function taking a wait time
		globals.put("yield", new LFunction() {
			public int invoke(LuaState vm) {
				LuaLink link = rs.getCurrentLink();				
				if (link != null && vm.gettop() >= 1) {
					link.setWait(vm.tointeger(1));
				}
				
				vm.resettop();
				
				LThread r = LThread.getRunning();
				if (r == null) {
					vm.error("main thread can't yield");
					return 0;
				}
				r.yield();
				
				return -1;
			}
			
			public boolean luaStackCall(LuaState vm) {
				vm.invokeJavaFunction(this);
				return true;
			}
		});	
	}

	/** Workaround for a bug in LuaJ 1.0.3, yield doesn't end exec() */
	static void fixCoroutineLib(LTable globals) {
		LTable lib = (LTable)globals.get(LString.valueOf("coroutine"));
		lib.put("yield", new LFunction() {
			public int invoke(LuaState vm) {
				LThread r = LThread.getRunning();
				if (r == null) {
					vm.error("main thread can't yield");
					return 0;
				}
				r.yield(); 
				return -1;				
			}
			public boolean luaStackCall(LuaState vm) {
				super.luaStackCall(vm);
				return true;
			}
		});
		globals.put("coroutine", lib);
	}
	
	public static void loadModule(LuaState vm, File file) throws LuaException, IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			loadModule(vm, file.getName(), in);
		} finally {
			in.close();
		}
	}
	
	public static void loadModule(LuaState vm, String filename, InputStream in) throws LuaException {
        int res = vm.load(in, filename);
		if (res != 0) {			
			throw new LuaException(String.format("Compile Error (%d) in \"%s\" :: %s", res, filename, vm.tostring(-1)));
		}
	}
	
	public static void initModule(LuaState vm, String filename) throws LuaException {		
		int res = vm.pcall(0, 0);
		if (res != 0) {			
			throw new LuaException(String.format("Initialization Error (%d) in \"%s\" :: %s", res, filename, vm.tostring(-1)));
		}
	}

	public static void registerClass(LuaRunState rs, LuaState vm, Class<?> c) throws LuaException {		
		String filename = "ClassBinding-" + c.getName();

		String code = String.format(
				  "luajava.bindClass(\"%s\")\n"
				+ "%s = {}\n"
				+ "%s.new = function(...)\n"
				+ "    return luajava.newInstance(\"%s\", unpack(arg))\n"
				+ "end\n",
				c.getName(), c.getSimpleName(), c.getSimpleName(), c.getName());
		
		ByteArrayInputStream bin = null;
		try {
			bin = new ByteArrayInputStream(code.getBytes("UTF-8"));
			LuaUtil.loadModule(vm, filename, bin);
			LuaUtil.initModule(vm, filename);
		} catch (IOException ioe) {
			throw new LuaException(ioe);
		} finally {
			try {
				if (bin != null) bin.close();
			} catch (IOException ioe) {
				throw new LuaException(ioe);
			}
		}
	}
	
	public static <T extends LuaLinkedObject> void registerClass2(
			LuaRunState rs, LuaState vm, Class<T> c) throws LuaException
	{
		Constructor<?> constr = null;
		try {
			constr = c.getConstructor();
		} catch (NoSuchMethodException nsme) {
			//Just pretend getConstructor() returned null
		} catch (Exception e) {
			throw new LuaException(e);
		}
		
		if (constr == null) {
			throw new LuaException(String.format("No default constructor for class \"%s\" -- unable to register", c.getName()));
		}
		
		LTable table = new LTable();
		table.put("new", new ConstructorFunction<T>(rs, c));		
		vm.pushlvalue(table);
		vm.setglobal(c.getSimpleName());
	}

	public static void registerEnum(LuaState vm, Class<? extends Enum<?>> c) {
		LTable table = new LTable();
		for (Enum<?> e : c.getEnumConstants()) {
			table.put(e.name(), LuajavaLib.toUserdata(e, e.getClass()));
		}
		vm.pushlvalue(table);
		vm.setglobal(c.getSimpleName());
	}
	
	/**
	 * @param table The table to clone
	 * @return A shallow copy of the input table
	 */
	@SuppressWarnings("deprecation")
	public static LTable clone(LTable table) {
		LTable result = new LTable();
		
		LValue[] keys = table.getKeys();
		for (LValue key : keys) {
			result.put(key, table.get(key));
		}
		
		LTable meta = result.luaGetMetatable();
		if (meta != null) {
			result.luaSetMetatable(clone(meta));
		}
		
		return result;
	}

	/**
	 * Registers key constants as a Lua table
	 * 
	 * @param vm The LuaState to install the new bindings in
	 * @param keys A class containing the full set of keycode constants
	 */
	public static void registerKeyCodes(LuaState vm, Keys keys) {
		LTable table = new LTable();
		for (Entry<String, Integer> entry : keys) {
			table.put(entry.getKey(), entry.getValue());
		}
		vm.pushlvalue(table);
		vm.setglobal("Keys");
		
		LTable vkeys = new LTable();
		for (int player = 1; player <= VKey.MAX_PLAYERS; player++) {
			LTable t = new LTable();
			for (VKey k : VKey.values()) {
				t.put(k.name(), k.toKeyCode(player));
			}
			vkeys.put(player, t);
		}
		vm.pushlvalue(vkeys);
		vm.setglobal("vkeys");
	}
		
	/**
	 * Registers the Thread.* functions
	 * 
	 * @param rs The LuaRunState object
	 * @param vm The LuaState to install the new bindings in
	 */
	public static void registerThreadLib(final LuaRunState rs, LuaState vm) {
		LTable table = new LTable();
		table.put("new", new LFunction() {
			public int invoke(LuaState vm) {
				if (vm.gettop() >= 1 && vm.isfunction(1)) {
					LFunction func = vm.checkfunction(1);
					LValue args[] = new LValue[vm.gettop()-1];
					for (int n = 0; n < args.length; n++) {
						args[n] = vm.topointer(2+n);
					}
					
					LuaFunctionLink link = new LuaFunctionLink(rs, vm, func, args);					
					rs.addThread(link);										
					vm.pushlvalue(LuajavaLib.toUserdata(link, link.getClass()));
				} else {
					vm.pushnil();
				}
				return 1;
			}
		});
		vm.pushlvalue(table);
		vm.setglobal("Thread");
	}

	/**
	 * Registers the Field.* functions
	 * 
	 * @param rs The LuaRunState object
	 * @param vm The LuaState to install the new bindings in
	 */
	public static void registerFieldLib(final LuaRunState rs, LuaState vm) {
		LTable table = new LTable();
		table.put("new", new LFunction() {
			public int invoke(LuaState vm) {
				if (vm.gettop() >= 6) {
					IField field = rs.createField(vm.checkint(1), vm.checkint(2),
							vm.checkint(3), vm.checkint(4), vm.checkint(5),
							vm.checkint(6));
					vm.pushlvalue(LuajavaLib.toUserdata(field, field.getClass()));
				} else if (vm.gettop() >= 5) {
					IField field = rs.createField(vm.checkint(1), vm.checkint(2),
							vm.checkint(3), vm.checkint(4), vm.checkint(5));
					vm.pushlvalue(LuajavaLib.toUserdata(field, field.getClass()));
				} else {
					vm.pushnil();
				}
				return 1;
			}
		});
		vm.pushlvalue(table);
		vm.setglobal("Field");
		
		//global int screenWidth
		//global int screenHeight
		IField field0 = rs.getField(0);
		vm.pushinteger(field0.getWidth());
		vm.setglobal("screenWidth");
		vm.pushinteger(field0.getHeight());
		vm.setglobal("screenHeight");		
	}
	
}
