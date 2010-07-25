package nl.weeaboo.dt.lua;

@SuppressWarnings("serial")
public class LuaException extends Exception {

	public LuaException(String message, Throwable cause) {
		super(message, cause);
	}

	public LuaException(String message) {
		super(message);
	}

	public LuaException(Throwable cause) {
		super(cause);
	}
	
}
