package nl.weeaboo.dt.lua.platform;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.luaj.vm.LFunction;
import org.luaj.vm.LInteger;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;

final class ClassInfo {

	private static LString LENGTH = LString.valueOf("length");
	
	private static Comparator<Method> methodSorter = new Comparator<Method>() {
		Collator c = Collator.getInstance(Locale.ROOT);
		
		public int compare(Method m1, Method m2) {
			return c.compare(m1.getName(), m2.getName());
		}
	};
	
	private Class<?> clazz;
	private boolean isArray;
	private LTable metaTable;
	
	private Constructor<?> constrs[];
	private Class<?> constrParamTypes[][];

	private Map<String, Field> fields;
	private Map<String, MethodInfo[]> methods;
	
	public ClassInfo(Class<?> c) {
		clazz = c;
		isArray = c.isArray();		
	}
	
	//Functions
	public Object newInstance(LValue luaArgs[]) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<?> constrs[] = getConstructors();
		Class<?> constrParamTypes[][] = getConstructorParamTypes();

		int index = findParamMatch(luaArgs, constrParamTypes);		
		if (index < 0 || index >= constrs.length) {
			throw new LuaErrorException(String.format("No suitable constructor found for: %s\n", clazz.getName()));
		}
		
		Constructor<?> constr = constrs[index];
		Class<?> javaParams[] = constrParamTypes[index];
	
		Object[] javaArgs = CoerceLuaToJava.coerceArgs(luaArgs, javaParams);
		return constr.newInstance(javaArgs);		
	}
	
	//Getters
	public Constructor<?> findConstructor(LValue luaArgs[]) {
		int index = findParamMatch(luaArgs, getConstructorParamTypes());
		
		Constructor<?> constrs[] = getConstructors();
		if (index >= 0 && index < constrs.length) {
			return constrs[index];
		}
		return null;
	}
	
	protected int findParamMatch(LValue luaArgs[], Class<?> paramTypes[][]) {
		int bestScore = Integer.MAX_VALUE;
		int bestMatch = -1;
		
		for (int n = 0; n < paramTypes.length; n++) {
			int score = CoerceLuaToJava.scoreParamTypes(luaArgs, paramTypes[n]);
			if (score == 0) {
				return n; //Perfect match, return at once
			} else if (score < bestScore) {
				bestScore = score;
				bestMatch = n;
			}
		}
		
		return bestMatch;
	}

	protected int findParamMatch(LValue luaArgs[], MethodInfo methods[]) {
		int bestScore = Integer.MAX_VALUE;
		int bestMatch = -1;
		
		for (int n = 0; n < methods.length; n++) {
			int score = CoerceLuaToJava.scoreParamTypes(luaArgs, methods[n].params);
			if (score == 0) {
				return n; //Perfect match, return at once
			} else if (score < bestScore) {
				bestScore = score;
				bestMatch = n;
			}
		}
		
		return bestMatch;
	}
	
	protected Constructor<?>[] getConstructors() {
		if (constrs == null) {
			constrs = clazz.getConstructors();
		}
		return constrs;
	}
	
	protected Class<?>[][] getConstructorParamTypes() {
		if (constrParamTypes == null) {
			Constructor<?> constrs[] = getConstructors();
			constrParamTypes = new Class<?>[constrs.length][];
			for (int n = 0; n < constrs.length; n++) {
				constrParamTypes[n] = constrs[n].getParameterTypes();
			}
		}
		return constrParamTypes;
	}
	
	public LTable getMetaTable() {
		if (metaTable == null) {
			metaTable = new MetaTable();
		}		
		return metaTable;
	}
	
	protected Field getField(String name) {
		if (fields == null) {
			fields = new HashMap<String, Field>();
			for (Field f : clazz.getFields()) {
				fields.put(f.getName(), f);
			}
		}
		return fields.get(name);
	}

	protected MethodInfo[] getMethods(String name) {
		if (methods == null) {
			Method marr[] = clazz.getMethods();
			Arrays.sort(marr, methodSorter);
			
			methods = new HashMap<String, MethodInfo[]>();
			
			String curName = null;
			List<MethodInfo> list = new ArrayList<MethodInfo>();			
			for (Method m : marr) {
				if (m.getName() != curName) {
					if (curName != null) {
						methods.put(curName, list.toArray(new MethodInfo[list.size()]));
					}
					curName = m.getName();
					list.clear();
				}
				list.add(new MethodInfo(m));
			}
			
			if (curName != null) {
				methods.put(curName, list.toArray(new MethodInfo[list.size()]));
			}
		}
		return methods.get(name);
	}
	
	protected boolean hasMethod(String name) {
		return getMethods(name) != null;
	}
	
	//Setters
	
	//Inner Classes
	private static final class MethodInfo {
		
		public final Method method;
		public final Class<?> params[];
		
		public MethodInfo(Method m) {
			method = m;
			params = m.getParameterTypes();
		}
		
	}
	
	private final class MetaTable extends LTable {

		private boolean seal;
		
		public MetaTable() {
			put(LValue.TM_INDEX, new MetaFunction(true));
			put(LValue.TM_NEWINDEX, new MetaFunction(false));
			seal = true;
		}
		
		public void put(LValue key, LValue val) {
			checkSeal();
			super.put(key, val);
		}
		public void put(int key, LValue val) {
			checkSeal();
			super.put(key, val);
		}
		public void put(String key, LValue val) {
			checkSeal();
			super.put(key, val);
		}
		public void put(String key, int val) {
			checkSeal();
			super.put(key, val);
		}
		public void hashSet(LValue key, Object value) {
			checkSeal();
			super.hashSet(key, value);
		}
		
		protected void checkSeal() {
			if (seal) {
				throw new LuaErrorException("Can't write to a shared Java class metatable");
			}
		}
	}
	
	private final class MetaFunction extends LFunction {
		
		private boolean isGet;
		
		public MetaFunction(boolean get) {
			isGet = get;
		}
		
		public boolean luaStackCall(LuaState vm) {
			Object instance = vm.touserdata(2);
			LValue key = vm.topointer(3);
			LValue val = (isGet ? null : vm.topointer(4));
			
			//Array indexing
			if (isArray && key instanceof LInteger) {
				vm.resettop();
				int index = key.toJavaInt() - 1;
				
				if (isGet) {
					if (index >= 0 && index < Array.getLength(instance)) {
						vm.pushlvalue(CoerceJavaToLua.coerce(Array.get(instance, index)));
					} else {
						vm.pushnil();
					}
				} else {					
					if (index >= 0 && index < Array.getLength(instance)) {
						Object v = CoerceLuaToJava.coerceArg(val, clazz.getComponentType());
						Array.set(instance, key.toJavaInt() - 1, v);
					} else {
						throw new LuaErrorException(new ArrayIndexOutOfBoundsException(index));
					}
				}
				return false;
			}
			
			final String s = key.toJavaString();
			vm.resettop();
			
			//Fields & Methods
			Field field = getField(s);
			if (isGet) {
				if (field != null) {
					try {
						Object o = field.get(instance);
						vm.pushlvalue(CoerceJavaToLua.coerce(o));
					} catch (Exception e) {
						throw new LuaErrorException(e);
					}
				} else if (isArray && key.equals(LENGTH)) {
					vm.pushinteger(Array.getLength(instance));
				} else {
					MethodInfo m[] = getMethods(s);
					if (m != null) {
						vm.pushlvalue(new LMethod(m));
					} else {
						vm.pushnil();
					}
				}
			} else {
				if (field != null) {
					Object v = CoerceLuaToJava.coerceArg(val, field.getType());
					try {
						field.set(instance, v);
					} catch (Exception e) {
						throw new LuaErrorException(e);
					}
				} else {
					throw new LuaErrorException("Invalid assignment, field does not exist in Java class: " + s);
				}
			}			
			return false;
		}
		
	}
		
	private final class LMethod extends LFunction {

		private MethodInfo methods[];
		
		public LMethod(MethodInfo mis[]) {
			methods = mis;
		}

		protected String getName() {
			return (methods.length > 0 ? methods[0].method.getName() : "nil");			
		}
		
		@Override
		public String toString() {
			return clazz.getName() + "." + getName() + "()";
		}

		@Override
		public boolean luaStackCall(LuaState vm) {
			try {
				Object instance = vm.touserdata(2);

				//Find method
				LValue luaArgs[] = LuajavaLib.getArgs(vm);
				int index = findParamMatch(luaArgs, methods);
				if (index < 0 || index >= methods.length) {
					throw new NoSuchMethodException(String.format("Method %s with the specified parameter types doesn't exist", getName()));
				}
	            
				//Call method
				MethodInfo mi = methods[index];
				Object[] javaArgs = CoerceLuaToJava.coerceArgs(luaArgs, mi.params);
				Object result = mi.method.invoke(instance, javaArgs);
				
				//Return result
				vm.resettop();
				vm.pushlvalue(CoerceJavaToLua.coerce(result));
				return false;
			} catch (InvocationTargetException ite) {
				throw new LuaErrorException(ite.getTargetException());
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
		}
		
	}
	
}
