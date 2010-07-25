package nl.weeaboo.dt.lua.platform;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.luaj.lib.MathLib;
import org.luaj.vm.LDouble;
import org.luaj.vm.LNumber;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public abstract class LuaPlatform extends Platform {

	//Functions
	@Override
    protected void installOptionalLibs(LuaState vm) {
        vm.installStandardLibs();
        LuajavaLib.install(vm._G);
    }	

	@Override
    public Reader createReader(InputStream inputStream) {
        return new InputStreamReader(inputStream);
    }    
	
	@Override
    public abstract InputStream openFile(String fileName);
    
    @Override
	public LNumber mathop(int id, LNumber la, LNumber lb) {
		double a = la.toJavaDouble();
		double b = lb.toJavaDouble();
		double z = 0;
		
		switch (id) {
		case MathLib.ATAN2: z = Math.atan2(a, b); break;
		case MathLib.FMOD: z = a - (b * ((int)(a/b))); break;
		case MathLib.POW: z = Math.pow(a, b); break;
		default: return unsupportedMathOp();
		}
		
		return LDouble.numberOf(z);
	}
	
    @Override
    public LNumber mathPow(LNumber base, LNumber exp) {
		return LDouble.numberOf(Math.pow(base.toJavaDouble(), exp.toJavaDouble()));
	}

    @Override
	public LNumber mathop(int id, LNumber lx) {
		double x = lx.toJavaDouble();
		double z = 0;
		
		switch (id) {
		default: return unsupportedMathOp();
		case MathLib.ABS: z = Math.abs(x); break;
		case MathLib.ACOS: z = Math.acos(x); break;
		case MathLib.ASIN: z = Math.asin(x); break;
		case MathLib.ATAN: z = Math.atan(x); break;
		case MathLib.COS: z = Math.cos(x); break;
		case MathLib.COSH: z = Math.cosh(x); break;
		case MathLib.DEG: z = Math.toDegrees(x); break;
		case MathLib.EXP: z = Math.exp(x); break;
		case MathLib.LOG: z = Math.log(x); break;
		case MathLib.LOG10: z = Math.log10(x); break;
		case MathLib.RAD: z = Math.toRadians(x); break;
		case MathLib.SIN: z = Math.sin(x); break;
		case MathLib.SINH: z = Math.sinh(x); break;
		case MathLib.SQRT: z = Math.sqrt(x); break;
		case MathLib.TAN: z = Math.tan(x); break;
		case MathLib.TANH: z = Math.tanh(x); break;
		}
		
		return LDouble.numberOf(z);
	}
	
	//Getters
	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
    
	@Override
    public String getProperty(String propertyName) {
        return null; //System.getProperty(propertyName);
    }

	//Setters
	
}
