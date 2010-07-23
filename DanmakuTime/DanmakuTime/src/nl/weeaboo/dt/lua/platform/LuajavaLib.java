/*******************************************************************************
* Copyright (c) 2007 LuaJ. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/

//Modified version of Luajava 1.1

package nl.weeaboo.dt.lua.platform;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.luaj.vm.LFunction;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;


public final class LuajavaLib extends LFunction {

	private static final int INIT			= 0;
	private static final int BINDCLASS		= 1;
	private static final int NEWINSTANCE	= 2;
	private static final int NEW			= 3;
	private static final int CREATEPROXY	= 4;
	private static final int LOADLIB		= 5;
	
	private static final String[] NAMES = {
		"luajava",
		"bindClass", 
		"newInstance", 
		"new", 
		"createProxy", 
		"loadLib"
	};
	
	private static final int METHOD_MODIFIERS_VARARGS = 0x80;

	private static Map<Class<?>, ClassInfo> classInfoMap = new HashMap<Class<?>, ClassInfo>();
	
	private int id;
	
	private LuajavaLib(int id) {
		this.id = id;
	}

	public static void install(LTable globals) {
		LTable luajava = new LTable();
		for (int i = 0; i < NAMES.length; i++) {
			luajava.put(NAMES[i], new LuajavaLib(i));
		}
		globals.put("luajava", luajava);
	}
	
	public boolean luaStackCall(final LuaState vm) {
		switch (id) {
		case INIT:
			install(vm._G);
			break;
		case BINDCLASS:
			try {
				String className = vm.tostring(2);

				Class<?> clazz = Class.forName(className);
				
				vm.resettop();
				vm.pushlvalue(toUserdata(clazz, clazz));
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
			break;
		case NEWINSTANCE:
		case NEW:
			try {
				LValue c = vm.topointer(2);
				
				Class<?> clazz;
				if (id == NEWINSTANCE) {
					clazz = Class.forName(c.toJavaString());
				} else {
					clazz = (Class<?>)c.toJavaInstance();
				}

				ClassInfo info = getClassInfo(clazz);
				Object javaObject = info.newInstance(getArgs(vm));
				
				vm.resettop();
				vm.pushlvalue(toUserdata(javaObject, clazz));
			} catch (InvocationTargetException ite) {
				throw new LuaErrorException(ite.getTargetException());
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
			break;
		case CREATEPROXY:
			final int ninterfaces = Math.max(0,vm.gettop()-2);
			if ( ninterfaces <= 0 )
				throw new LuaErrorException("no interfaces");
			final LValue lobj = vm.totable(-1);
			try {
				// get the interfaces
				final Class<?>[] ifaces = new Class[ninterfaces];
				for ( int i=0; i<ninterfaces; i++ ) 
					ifaces[i] = Class.forName(vm.tostring(i+2));
				
				// create the invocation handler
				InvocationHandler handler = new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						vm.pushlvalue(lobj);
						vm.getfield( -1, LString.valueOf(method.getName()) );
						vm.remove( -2 );
						LValue result;
						if ( !vm.isnil( -1 ) ) {
							boolean isvarargs = ((method.getModifiers() & METHOD_MODIFIERS_VARARGS) != 0);
							int n = ( args != null ) ? args.length : 0;
							if ( isvarargs ) {								
								Object o = args[--n];
								int m = Array.getLength( o );
								for ( int i=0; i<n; i++ )
									vm.pushlvalue( CoerceJavaToLua.coerce(args[i]) );
								for ( int i=0; i<m; i++ )
									vm.pushlvalue( CoerceJavaToLua.coerce(Array.get(o,i)) );								
								vm.call(n+m, 1);
							} else {
								for ( int i=0; i<n; i++ )
									vm.pushlvalue( CoerceJavaToLua.coerce(args[i]) );
								vm.call(n, 1);
							}
						}
						result = vm.poplvalue();
						return CoerceLuaToJava.coerceArg(result, method.getReturnType());
					}
				};
				
				// create the proxy object
				Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), ifaces, handler);
				
				// return the proxy
				vm.resettop();
				vm.pushuserdata(proxy);				
				
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
			break;
		case LOADLIB:
			try {
				// get constructor
				String classname = vm.tostring(2);
				String methodname = vm.tostring(3);
				Class<?> clazz = Class.forName(classname);
				Method method = clazz.getMethod(methodname, new Class[] { LuaState.class });
				Object result = method.invoke(clazz, new Object[] { vm });
				if ( result instanceof Integer ) {
					int nresults = ((Integer)result).intValue();
					int nremove = vm.gettop() - nresults;
					for ( int i=0; i<nremove; i++ )
						vm.remove(1);
				} else {
					vm.resettop();
				}
			} catch (InvocationTargetException ite) {
				throw new LuaErrorException(ite.getTargetException());
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
			break;
		default:
			throw new LuaErrorException("Not yet supported: "+this);
		}
		return false;
	}

	public static LUserData toUserdata(Object obj, Class<?> clazz) {
		ClassInfo info = getClassInfo(clazz);
		return new LUserData(obj, info.getMetaTable());
	}
	
	//Getters
	@Override
	public String toString() {
		return "luajava." + NAMES[id];
	}

	protected static ClassInfo getClassInfo(Class<?> clazz) {
		ClassInfo info = classInfoMap.get(clazz);
		if (info == null) {
			info = new ClassInfo(clazz);
			classInfoMap.put(clazz, info);
		}
		return info;
	}
	
	public static LValue[] getArgs(LuaState vm) {
		int numArgs = Math.max(vm.gettop() - 2, 0);

		LValue values[] = new LValue[numArgs];
		for (int n = 0; n < numArgs; n++) {
			values[n] = vm.topointer(n - numArgs);
		}

		return values;
	}
	
}