package nl.weeaboo.dt.lua;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import nl.weeaboo.common.Log;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.lua.link.LuaFunctionLink;
import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.lua.link.LuaLinkedObject;

import org.luaj.lib.j2se.LuajavaLib;
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
		
		globals.put("yield", new LFunction() {
			public int invoke(LuaState vm) {
				LuaLink link = rs.getCurrentLink();
				if (link != null && vm.gettop() >= 1) {
					link.setWait(vm.tointeger(1));
				}
				
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
	
	public static void load(LuaState vm, File file) throws LuaException, IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			load(vm, file.getName(), in);
		} finally {
			in.close();
		}
	}
	
	public static void load(LuaState vm, String filename, InputStream in) throws LuaException {
		int res;
		
        res = vm.load(in, filename);
		if (res != 0) {			
			throw new LuaException(String.format("Compile Error (%d) in \"%s\" :: %s", res, filename, vm.tostring(-1)));
		}
		
		res = vm.pcall(0, 0);
		if (res != 0) throw new LuaException(String.format("Runtime Error (%d) in \"%s\" :: %s", res, filename, vm.tostring(-1)));		
	}

	public static void registerClass(LuaRunState rs, LuaState vm, Class<?> c) throws LuaException {
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
			LuaUtil.load(vm, "ClassBinding-" + c.getName(), bin);
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
			LuaRunState rs, LuaState vm, Class<T> c)
	{
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
	 * Creates a table "Keys" in Lua containing all the <code>VK_???</code>
	 * static fields from <code>c</code>. The fields in Lua have the same name,
	 * just without the <code>VK_</code> prefix.
	 * 
	 * @param vm The LuaState to install the new bindings in
	 * @param c The class that contains the static fields with the key codes
	 *        (AWT or JavaFX KeyEvents)
	 */
	public static void registerKeyCodes(LuaState vm, Class<?> c) {
		LTable table = new LTable();
		for (Field field : c.getFields()) {
			String fname = field.getName();
			if (fname.startsWith("VK_")) {
				try {
					int intval = field.getInt(null);
					table.put(fname.substring(3), intval);
				} catch (IllegalArgumentException e) {
					Log.warning(e);
					break;
				} catch (IllegalAccessException e) {
					Log.warning(e);
					break;
				}
			}
		}
		vm.pushlvalue(table);
		vm.setglobal("Keys");
	}
	
	/**
	 * Registers the Thread.* functions
	 * 
	 * @param rs The LuaRunState object
	 * @param vm The LuaState to install the new bindings in
	 * @param pool The thread pool object that the new threads get added to
	 */
	public static void registerThreadLib(final LuaRunState rs, LuaState vm,
			final LuaThreadPool pool)
	{
		LTable table = new LTable();
		table.put("new", new LFunction() {
			public int invoke(LuaState vm) {
				if (vm.gettop() >= 1 && vm.isfunction(1)) {
					LuaFunctionLink link = new LuaFunctionLink(rs, vm, vm.checkfunction(1));
					pool.add(link);
										
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
