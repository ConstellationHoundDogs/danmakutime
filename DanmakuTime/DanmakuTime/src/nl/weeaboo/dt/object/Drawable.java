package nl.weeaboo.dt.object;

import nl.weeaboo.dt.DTLog;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.LuaRunState;
import nl.weeaboo.dt.lua.LuaThreadPool;
import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.lua.link.LuaLinkedObject;
import nl.weeaboo.dt.lua.link.LuaMethodLink;
import nl.weeaboo.dt.renderer.BlendMode;
import nl.weeaboo.dt.renderer.IRenderer;
import nl.weeaboo.dt.renderer.ITexture;

import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

public class Drawable implements IDrawable, LuaLinkedObject {

	private boolean destroyed;
	
	protected LuaMethodLink updateLink;
	protected LuaThreadPool threadPool;
	
	protected IField field;
	protected ITexture texture;
	private double x, y;
	private short z;
	private double drawAngle;
	protected boolean clip;
	private boolean visible;
	private int color;
	private BlendMode blendMode;
	private double scaleX, scaleY;
	
	public Drawable() {
		color = 0xFFFFFFFF;
		clip = true;
		visible = true;
		blendMode = BlendMode.NORMAL;
		scaleX = scaleY = 1.0;		
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
		field.add(this);
		
		threadPool = new LuaThreadPool();
		threadPool.add(updateLink = new LuaMethodLink(runState, vm, udata, "update"));
		threadPool.add(new LuaMethodLink(runState, vm, udata, "animate"));
	}
	
	@Override
	public LuaLink addThread(LFunction func, LValue... args) {
		LuaMethodLink mlink = new LuaMethodLink(updateLink.runState, updateLink.rootVM,
				getLuaObject(), func, args);
		threadPool.add(mlink);
		return mlink;
	}
	
	@Override
	public boolean destroy() {
		LValue retval = LNil.NIL;
		if (threadPool != null) {
			try {
				retval = updateLink.call(true, "onDestroy");
			} catch (LuaException e) {
				DTLog.warning(e);
			}
		}
		if (retval.isNil() || retval.toJavaBoolean()) {		
			destroyed = true;
			threadPool.dispose();
		}
		return destroyed;
	}
	
	@Override
	public void update(IInput input) {
		threadPool.update();
	}
	
	@Override
	public final void draw(IRenderer renderer) {
		if (!visible) return;
		
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
		
		renderer.drawRotatedQuad(x, y, getWidth(), getHeight(), z, drawAngle);		
	}
	
	//Getters
	@Override
	public LUserData getLuaObject() {
		return updateLink.self;
	}
	
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
	public double getWidth() {
		return texture != null ? texture.getWidth() * scaleX : 1;
	}
	
	@Override
	public double getHeight() {
		return texture != null ? texture.getHeight() * scaleY : 1;
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
	public double getRed() {
		return ((color>>16) & 0xFF) / 255.0;
	}

	@Override
	public double getGreen() {
		return ((color>>8) & 0xFF) / 255.0;
	}

	@Override
	public double getBlue() {
		return (color & 0xFF) / 255.0;
	}
	
	@Override
	public double getAlpha() {
		return ((color>>24) & 0xFF) / 255.0;
	}

	@Override
	public BlendMode getBlendMode() {
		return blendMode;
	}
	
	@Override
	public double getScaleX() {
		return scaleX;
	}

	@Override
	public double getScaleY() {
		return scaleY;
	}
	
	@Override
	public boolean isVisible() {
		return visible;
	}
	
	//Setters
	@Override
	public void setField(IField f) {
		field = f;
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
	public void setColor(double r, double g, double b, double a) {
		int ri = Math.max(0, Math.min(255, (int)Math.round(r * 255.0)));
		int gi = Math.max(0, Math.min(255, (int)Math.round(g * 255.0)));
		int bi = Math.max(0, Math.min(255, (int)Math.round(b * 255.0)));
		int ai = Math.max(0, Math.min(255, (int)Math.round(a * 255.0)));
		setColor((ai<<24)|(ri<<16)|(gi<<8)|(bi));
	}
	
	@Override
	public void setAlpha(double a) {
		int ai = Math.max(0, Math.min(255, (int)Math.round(a * 255.0)));
		setColor((ai<<24) | (color & 0xFFFFFF));
	}

	@Override
	public void setScaleX(double sx) {
		scaleX = sx;
	}

	@Override
	public void setScaleY(double sy) {
		scaleY = sy;
	}
	
	@Override
	public void setScale(double sx, double sy) {
		setScaleX(sx);
		setScaleY(sy);
	}
	
	@Override
	public void setVisible(boolean v) {
		visible = v;
	}
		
}
