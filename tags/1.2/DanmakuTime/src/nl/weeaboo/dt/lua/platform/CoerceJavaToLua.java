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
import java.util.HashMap;
import java.util.Map;

import nl.weeaboo.dt.lua.link.LuaLinkedObject;

import org.luaj.vm.LBoolean;
import org.luaj.vm.LDouble;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;

public class CoerceJavaToLua {
	
	public static interface Coercion { 
		public LValue coerce(Object javaValue);
	};
	
	private static Map<Class<?>, Coercion> COERCIONS = new HashMap<Class<?>, Coercion>();
	
	static {
		Coercion boolCoercion = new Coercion() {
			public LValue coerce(Object javaValue) {
				return ((Boolean)javaValue ? LBoolean.TRUE : LBoolean.FALSE);
			}
		};
		Coercion intCoercion = new Coercion() {
			public LValue coerce(Object javaValue) {
				return LInteger.valueOf(((Number)javaValue).intValue());
			}
		};
		Coercion charCoercion = new Coercion() {
			public LValue coerce(Object javaValue) {
				return LInteger.valueOf(((Character)javaValue).charValue());
			}
		};
		Coercion doubleCoercion = new Coercion() {
			public LValue coerce(Object javaValue) {
				return LDouble.numberOf(((Number)javaValue).doubleValue());
			}
		};
		Coercion stringCoercion = new Coercion() {
			public LValue coerce(Object javaValue) {
				return new LString(javaValue.toString());
			}
		};
		
		COERCIONS.put(Boolean.class, boolCoercion);
		COERCIONS.put(Byte.class, intCoercion);
		COERCIONS.put(Character.class, charCoercion);
		COERCIONS.put(Short.class, intCoercion);
		COERCIONS.put(Integer.class, intCoercion);
		COERCIONS.put(Float.class, doubleCoercion);
		COERCIONS.put(Double.class, doubleCoercion);
		COERCIONS.put(String.class, stringCoercion);
	}

	public static LValue coerce(Object o) {
		if (o == null) return LNil.NIL;
		
		Class<?> clazz = o.getClass();
		Coercion c = (Coercion)COERCIONS.get(clazz);
		if (c != null) {
			//A specialized coercion was found, use it
			return c.coerce(o);
		}
		
		if (clazz.isArray()) {
			//Return a special LUserData object that supports luaLength()
			ClassInfo info = LuajavaLib.getClassInfo(clazz);
			return new LUserData(o, info.getMetaTable()) {
				public int luaLength() {
					return Array.getLength(toJavaInstance());
				}
			};
		}
		
		if (o instanceof LuaLinkedObject) {
			//Object already contains a Lua converted version of itself
			return ((LuaLinkedObject)o).getLuaObject();
		}
		
		//Use the general Java Object -> Lua conversion
		return LuajavaLib.toUserdata(o, clazz);
	}

}
