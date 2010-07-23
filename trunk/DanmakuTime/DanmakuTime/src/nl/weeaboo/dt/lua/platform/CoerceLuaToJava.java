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

import org.luaj.vm.LBoolean;
import org.luaj.vm.LDouble;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNumber;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaErrorException;


public class CoerceLuaToJava {

	public static interface Coercion {
		public Object coerce(LValue value);
		public int score(LValue value);
	};
	
	private static Map<Class<?>, Coercion> COERCIONS = new HashMap<Class<?>, Coercion>();
	
	static {
		Coercion boolCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return value.toJavaBoolean() ? Boolean.TRUE: Boolean.FALSE;
			}
			public int score(LValue value) {
				if (value instanceof LBoolean || value.isNil()) return 0;
				if (value instanceof LNumber) return 1;
				return 4;
			}
		};
		Coercion byteCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return new Byte(value.toJavaByte());
			}

			public int score(LValue value) {
				if (value instanceof LInteger) return 1;
				if (value instanceof LNumber) return 2;
				return 4;
			}
		};
		Coercion charCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return new Character(value.toJavaChar());
			}

			public int score(LValue value) {
				if (value instanceof LInteger) return 1;
				if (value instanceof LNumber) return 2;
				return 4;
			}
		};
		Coercion shortCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return new Short(value.toJavaShort());
			}

			public int score(LValue value) {
				if (value instanceof LInteger) return 1;
				if (value instanceof LNumber) return 2;
				return 4;
			}
		};
		Coercion intCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return new Integer(value.toJavaInt());
			}

			public int score(LValue value) {
				if (value instanceof LInteger) return 0;
				if (value instanceof LNumber) return 1;
				if (value instanceof LBoolean || value.isNil()) return 2;
				return 4;
			}
		};
		Coercion longCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return new Long(value.toJavaLong());
			}

			public int score(LValue value) {
				if (value instanceof LInteger) return 1;
				if (value instanceof LNumber) return 2;
				return 4;
			}
		};
		Coercion floatCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return new Float(value.toJavaFloat());
			}

			public int score(LValue value) {
				if (value instanceof LNumber) return 1;
				if (value instanceof LBoolean) return 2;
				return 4;
			}
		};
		Coercion doubleCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return new Double(value.toJavaDouble());
			}

			public int score(LValue value) {
				if (value instanceof LDouble) return 0;
				if (value instanceof LNumber) return 1;
				if (value instanceof LBoolean) return 2;
				return 4;
			}
		};
		Coercion stringCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return value.toJavaString();
			}

			public int score(LValue value) {
				if (value instanceof LUserData) return 0;
				return 1;
			}
		};
		Coercion objectCoercion = new Coercion() {
			public Object coerce(LValue value) {
				if (value instanceof LUserData) return ((LUserData) value).m_instance;
				if (value instanceof LString) return value.toJavaString();
				if (value instanceof LInteger) return new Integer(value.toJavaInt());
				if (value instanceof LDouble) return new Double(value.toJavaDouble());
				if (value instanceof LBoolean) return Boolean.valueOf(value.toJavaBoolean());
				if (value.isNil()) return null;
				return value;
			}

			public int score(LValue value) {
				if (value instanceof LString) return 0;
				return 0x10;
			}
		};
		
		COERCIONS.put(Boolean.TYPE, boolCoercion);
		COERCIONS.put(Boolean.class, boolCoercion);
		COERCIONS.put(Byte.TYPE, byteCoercion);
		COERCIONS.put(Byte.class, byteCoercion);
		COERCIONS.put(Character.TYPE, charCoercion);
		COERCIONS.put(Character.class, charCoercion);
		COERCIONS.put(Short.TYPE, shortCoercion);
		COERCIONS.put(Short.class, shortCoercion);
		COERCIONS.put(Integer.TYPE, intCoercion);
		COERCIONS.put(Integer.class, intCoercion);
		COERCIONS.put(Long.TYPE, longCoercion);
		COERCIONS.put(Long.class, longCoercion);
		COERCIONS.put(Float.TYPE, floatCoercion);
		COERCIONS.put(Float.class, floatCoercion);
		COERCIONS.put(Double.TYPE, doubleCoercion);
		COERCIONS.put(Double.class, doubleCoercion);
		COERCIONS.put(String.class, stringCoercion);
		COERCIONS.put(Object.class, objectCoercion);
	}

	static Object[] coerceArgs(LValue[] luaArgs, Class<?>[] javaParams) {
		Object[] result = new Object[javaParams.length];
		
		// Convert lua args to java types, leave any remaining elems in the
		// result array null
		int len = Math.min(luaArgs.length, javaParams.length);
		for (int n = 0; n < len; n++) {
			result[n] = coerceArg(luaArgs[n], javaParams[n]);
		}
		
		return result;
	}

	public static Object coerceArg(LValue a, Class<?> c) {
		//The lua arg is a Java object
		if (a instanceof LUserData) {
			Object o = ((LUserData) a).m_instance;
			if (c.isAssignableFrom(o.getClass())) {
				return o;
			}
		}
		
		//Try to use a specialized coercion function if one is available
		Coercion co = (Coercion) COERCIONS.get(c);
		if (co != null) {
			return co.coerce(a);
		}
		
		//Special coercion for arrays
		if (c.isArray()) {
			Class<?> inner = c.getComponentType();
			if (a instanceof LTable) {
				//LTable -> Array				
				LTable table = (LTable)a;
				int len = table.luaLength();
				Object result = Array.newInstance(inner, len);
				for (int n = 0; n < len; n++) {
					LValue val = table.get(n+1);
					if (val != null) {
						Array.set(result, n, coerceArg(val, inner));
					}
				}
				return result;
			} else {
				//Single element -> Array
				Object result = Array.newInstance(inner, 1);
				Array.set(result, 0, coerceArg(a, inner));
				return result;
			}
		}

		//Special case for nil
		if (a.isNil()) {
			return null;
		}
		
		throw new LuaErrorException("Invalid coercion: " + a.getClass() + " -> " + c);
	}

	/**
	 * @return The score, lower scores are better matches
	 */
	public static int scoreParamTypes(LValue[] luaArgs, Class<?>[] javaParams) {
		//Init score & minimum length
		int score;
		int len;
		if (javaParams.length == luaArgs.length) {
			score = 0;
			len = javaParams.length; 
		} else if (javaParams.length > luaArgs.length) {
			score = 0x4000;
			len = luaArgs.length;
		} else {
			score = 0x8000;
			len = javaParams.length;
		}
		
		//Compare args
		for (int n = 0; n < len; n++) {
			score += scoreParam(luaArgs[n], javaParams[n]);
		}
		return score;
	}

	private static int scoreParam(LValue a, Class<?> c) {
		//The lua arg is a Java object
		if (a instanceof LUserData) {
			Object o = ((LUserData) a).m_instance;
			if (c.isAssignableFrom(o.getClass())) {
				return 0; //Perfect match
			}
		}
		
		//Try to use a specialized scoring function if one is available
		Coercion co = (Coercion) COERCIONS.get(c);
		if (co != null) {
			return co.score(a);
		}
		
		//Special scoring for arrays
		if (c.isArray()) {
			Class<?> inner = c.getComponentType();
			if (a instanceof LTable) {
				//Supplying a table as an array arg, compare element types
				return scoreParam(((LTable) a).get(1), inner);
			} else {
				//Supplying a single element as an array argument
				return 0x10 + (scoreParam(a, inner) << 8);
			}
		}
		
		return 0x1000;
	}
	
}
