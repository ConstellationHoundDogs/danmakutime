package nl.weeaboo.dt.object;

import org.luaj.vm.LNil;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

import nl.weeaboo.common.Log;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;
import nl.weeaboo.dt.lua.link.LuaLinkedObject;
import nl.weeaboo.dt.lua.link.LuaObjectLink;
import nl.weeaboo.dt.renderer.IRenderer;
import nl.weeaboo.dt.renderer.ITexture;

public class Drawable implements IDrawable, LuaLinkedObject {

	private boolean destroyed;
	
	protected LuaObjectLink luaLink;
	protected LuaObjectLink luaAnimateLink;

	protected ITexture texture;
	protected double x, y;
	private short z;
	private double drawAngle;
	private boolean clip;
	
	public Drawable() {
	}
	
	//Functions
	@Override
	public void init(LuaRunState runState, LuaState vm, LUserData udata)
		throws LuaException
	{
		IField field = runState.getField(1);
		field.add(this);
		
		luaLink = new LuaObjectLink(runState, vm, udata);
		
		luaAnimateLink = new LuaObjectLink(runState, vm, udata) {
			public void init() {
				inited = true;

				int pushed = pushMethod("animate");
				finished = (pushed <= 0);
			}
		};
	}
	
	@Override
	public void destroy() {
		LValue retval = LNil.NIL;
		try {
			retval = luaLink.call(true, "onDestroy");
		} catch (LuaException e) {
			Log.warning(e);
		}
		
		if (retval.isNil() || retval.toJavaBoolean()) {		
			destroyed = true;
		}
	}
	
	@Override
	public LValue call(String methodName, Object... args) throws LuaException {
		return luaLink.call(false, methodName, args);
	}
	
	@Override
	public void update(IInput input) {

		//Update Lua links
		if (luaLink != null && !luaLink.isFinished()) {
			try {
				luaLink.update();
			} catch (LuaException e) {
				Log.warning(e);
				luaLink = null;
			}
		}
		
		if (luaAnimateLink != null && !luaAnimateLink.isFinished()) {
			try {
				luaAnimateLink.update();
			} catch (LuaException e) {
				Log.warning(e);
				luaAnimateLink = null;
			}
		}
	}
	
	@Override
	public void draw(IRenderer renderer) {
		if (texture == null) return;
		
		int tw = texture.getWidth();
		int th = texture.getHeight();

		boolean oldClip = renderer.isClipEnabled();		
		renderer.setClipEnabled(clip);
		renderer.setTexture(texture);
		renderer.drawRotatedQuad(x, y, tw, th, z, drawAngle);		
		renderer.setClipEnabled(oldClip);
	}
	
	//Getters
	@Override
	public boolean isDestroyed() {
		return destroyed;
	}
	
	@Override
	public double getDrawAngle() {
		return drawAngle;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}
	
	@Override
	public ITexture getTexture() {
		return texture;
	}
	
	@Override
	public short getZ() {
		return z;
	}
	
	@Override
	public boolean isClip() {
		return clip;
	}
	
	//Setters
	@Override
	public void setDrawAngle(double a) {
		drawAngle = a;
	}

	@Override
	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void setTexture(ITexture tex) {
		texture = tex;
	}
	
	@Override
	public void setZ(short z) {
		this.z = z;
	}
	
	/**
	 * Ease-of use method for setting Z. WARNING: Z will be truncated to a
	 * short.
	 * 
	 * @param z The new Z-coordinate
	 */
	public void setZ(int z) {
		setZ((short) z);
	}

	@Override
	public void setClip(boolean c) {
		clip = c;
	}
	
}
