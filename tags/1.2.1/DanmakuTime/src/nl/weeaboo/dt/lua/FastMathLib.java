package nl.weeaboo.dt.lua;

import java.util.Random;

import nl.weeaboo.common.FastMath;

import org.luaj.lib.PackageLib;
import org.luaj.vm.LDouble;
import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LNumber;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

public class FastMathLib extends LFunction {

	public static final String[] NAMES = {
		"math",
		"random",
		"atan2",
		"acos", "asin", "atan", "cos", "sin", "tan", "sqrt"
	};

	private static final int INSTALL = 0;

	// irregular functions
	public static final int RANDOM = 1;
	public static final int LAST_IRREGULAR = RANDOM;

	// 2 argument, return double
	public static final int ATAN2 = 2;
	public static final int LAST_DOUBLE_ARG = ATAN2;

	/* Math operations - single argument, one function */
	public static final int ACOS = 3;
	public static final int ASIN = 4;
	public static final int ATAN = 5;
	public static final int COS = 6;
	public static final int SIN = 7;
	public static final int TAN = 8;
	public static final int SQRT = 9;

	private final Random random;
	private final int id;

	private FastMathLib(Random r, int id) {
		this.random = r;
		this.id = id;
	}

	public static void install(LTable globals, Random random) {
		LTable math = (LTable) globals.get(LString.valueOf("math"));
		if (math == null) {
			throw new RuntimeException("FastMath requires regular math lib to be available");
		}
		
		for (int i = 1; i < NAMES.length; i++) {
			math.put(NAMES[i], new FastMathLib(random, i));
		}
		math.put("huge", new LDouble(Double.MAX_VALUE));
		math.put("pi", new LDouble(Math.PI));
		globals.put("math", math);
		PackageLib.setIsLoaded("math", math);
	}

	public String toString() {
		return NAMES[id] + "()";
	}

	public int invoke(LuaState vm) {
		if (id > LAST_DOUBLE_ARG) {
			vm.pushlvalue(mathop(id, vm.checknumber(1)));
			return 1;
		} else if (id > LAST_IRREGULAR) {
			vm.pushlvalue(mathop(id, vm.checknumber(1), vm.checknumber(2)));
			return 1;
		} else {
			switch (id) {
			case INSTALL:
				install(vm._G, random);
				return 0;
			case RANDOM: {
				switch (vm.gettop()) {
				case 0:
					vm.pushnumber(random.nextDouble());
					return 1;
				case 1: {
					int m = vm.checkint(1);
					vm.argcheck(1 <= m, 1, "interval is empty");
					vm.pushinteger(1 + random.nextInt(m));
					return 1;
				}
				default: {
					int m = vm.checkint(1);
					int n = vm.checkint(2);
					vm.argcheck(m <= n, 2, "interval is empty");
					vm.pushinteger(m + random.nextInt(n + 1 - m));
					return 1;
				}
				}
			}
			default:
				LuaState.vmerror("bad math id");
				return 0;
			}
		}
	}
	
	public static LValue mathop(int id, LNumber arg0) {
		double x = arg0.toJavaBoxedDouble();
		double z;
		
		switch (id) {
		case ACOS: z = FastMath.fastArcCos((float)x); break;
		case ASIN: z = FastMath.fastArcCos((float)x); break;
		case ATAN: z = Math.atan(x) * FastMath.fastAngleScale; break;
		case SIN:  z = FastMath.fastSin((float)x); break;
		case COS:  z = FastMath.fastCos((float)x); break;
		case TAN:  z = Math.tan(x * FastMath.fastAngleScale); break;
		case SQRT: z = Math.sqrt(x); break;
		default: return LNil.NIL;
		}
		
		return LDouble.numberOf(z); 
	}
	protected static LValue mathop(int id, LNumber arg0, LNumber arg1) {
		double x = arg0.toJavaBoxedDouble();
		double y = arg1.toJavaBoxedDouble();				
		double z;
		
		switch (id) {
		case ATAN2: z = FastMath.fastArcTan2((float)x, (float)y); break;
		default: return LNil.NIL;
		}

		return LDouble.numberOf(z); 
	}

}
