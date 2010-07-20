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
import nl.weeaboo.dt.renderer.BlendMode;
import nl.weeaboo.dt.renderer.IRenderer;
import nl.weeaboo.dt.renderer.ITexture;

public class Drawable implements IDrawable, LuaLinkedObject {

	private boolean destroyed;
	
	protected LuaObjectLink luaLink;
	protected LuaObjectLink luaAnimateLink;

	protected IField field;
	protected ITexture texture;
	protected double x, y;
	private short z;
	private double drawAngle;
	protected boolean clip;
	private int color;
	private BlendMode blendMode;
	
	public Drawable() {
		color = 0xFFFFFFFF;
		blendMode = BlendMode.NORMAL;
	}
	
	//Functions
	@Override
	public void init(LuaRunState runState, LuaState vm, LUserData udata)
		throws LuaException
	{
		IField field = null;
		if (vm.gettop() >= 1) {
			if (vm.isnumber(1)) {
				field = runState.getField(vm.tointeger(1));
			} else if (vm.isuserdata(1)) {
				Object obj = vm.touserdata(1);
				if (obj instanceof IField) {
					field = (IField)obj;
				}
			}
		}
		if (field == null) {
			field = runState.getField(1);
			if (field == null) {
				field = runState.getField(0);
			}
		}		
		setField(field);
		
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
		if (luaLink != null) {
			try {
				retval = luaLink.call(true, "onDestroy");
			} catch (LuaException e) {
				Log.warning(e);
			}
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
	public final void draw(IRenderer renderer) {
		boolean oldClip = renderer.isClipEnabled();
		int oldColor = renderer.getColor();
		BlendMode oldBlendMode = renderer.getBlendMode();
		renderer.setClipEnabled(clip);
		renderer.setColor(color);
		renderer.setBlendMode(blendMode);
		renderer.setTexture(texture);

		drawGeometry(renderer);
		
		renderer.setClipEnabled(oldClip);
		renderer.setColor(oldColor);
		renderer.setBlendMode(oldBlendMode);
	}
	
	public void drawGeometry(IRenderer renderer) {
		if (texture == null) return;
		
		int tw = texture.getWidth();
		int th = texture.getHeight();
		
		renderer.drawRotatedQuad(x, y, tw, th, z, drawAngle);		
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
	
	@Override
	public int getColor() {
		return color;
	}

	@Override
	public double getAlpha() {
		int ai = (color>>24) & 0xFF;
		return ai / 255.0;
	}

	@Override
	public BlendMode getBlendMode() {
		return blendMode;
	}
	
	//Setters
	@Override
	public void setField(IField f) {
		if (field == f) return;
		
		if (field != null) {
			throw new IllegalArgumentException("Can't set field more than once");
		}
		
		field = f;
		field.add(this);
	}
	
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

	@Override
	public void setBlendMode(BlendMode b) {
		blendMode = b;
	}
	
	@Override
	public void setColor(int argb) {
		color = argb;
	}

	@Override
	public void setAlpha(double a) {
		int ai = Math.max(0, Math.min(255, (int)Math.round(a * 255.0)));
		setColor((ai<<24) | (color & 0xFFFFFF));
	}
		
}
