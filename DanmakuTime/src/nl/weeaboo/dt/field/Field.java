package nl.weeaboo.dt.field;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;

import nl.weeaboo.dt.FastList;
import nl.weeaboo.dt.collision.ColField;
import nl.weeaboo.dt.collision.IColField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.object.IDrawable;
import nl.weeaboo.dt.renderer.IRenderer;

public class Field implements IField {

	private ColField colField;
	private Rectangle bounds;
	private int padding;
	
	private Collection<IDrawable> standbyList;	
	private Collection<IDrawable> garbage;

	private Collection<IDrawable> drawables;
	private IDrawable luaObjArr[];
	
	public Field(int x, int y, int w, int h, int pad) {
		colField = new ColField(x-pad, y-pad, w+pad*2, h+pad*2);
		bounds = new Rectangle(x, y, w, h);
		padding = pad;
		
		standbyList = new ArrayList<IDrawable>();
		garbage = new ArrayList<IDrawable>();

		drawables = new FastList<IDrawable>(IDrawable.class);
		//drawables = new LinkedHashSet<IDrawable>();
		//drawables = new ArrayList<IDrawable>();
	}
	
	//Functions
	@Override
	public void add(IDrawable d) {
		d.setField(this);
		standbyList.add(d);
	}

	@Override
	public void clear() {
		flushStandbyList();
		garbage.addAll(drawables);
		garbageCollect();
	}
	
	protected void flushStandbyList() {
		drawables.addAll(standbyList);
		luaObjArr = null; //Invalidate cache
		standbyList.clear();
	}
	
	@Override
	public void update(IInput input) {
		flushStandbyList();
		
		for (IDrawable d : drawables) {
			if (d == null) continue;
			
			if (!d.isDestroyed()) {
				d.update(input);
			}
			if (d.isDestroyed()) {
				garbage.add(d);
			}
		}
		
		garbageCollect();
		
		colField.processCollisions();
	}
	
	protected void garbageCollect() {
		drawables.removeAll(garbage);
		luaObjArr = null; //Invalidate cache
		
		IDrawable arr[] = garbage.toArray(new IDrawable[0]);
		garbage.clear();
		
		for (IDrawable d : arr) {
			if (d != null) d.setField(null);
		}
	}
	
	@Override
	public void draw(IRenderer renderer) {
		Rectangle oldClip = renderer.getClipRect();
		renderer.setClipRect(bounds.x, bounds.y, bounds.width, bounds.height);

		int dx = bounds.x;
		int dy = bounds.y;
		renderer.translate(dx, dy);		

		for (IDrawable d : drawables) {
			if (d == null) continue;

			if (!d.isDestroyed()) {
				d.draw(renderer);
			}
		}
		renderer.translate(-dx, -dy);		

		renderer.setClipRect(oldClip.x, oldClip.y, oldClip.width, oldClip.height);
	}
	
	//Getters	
	@Override
	public IDrawable[] getAllObjects() {
		if (luaObjArr == null) {
			luaObjArr = drawables.toArray(new IDrawable[drawables.size()]);
		}
		return luaObjArr;
	}
	
	@Override
	public int getObjectCount() {
		return drawables.size();
	}
	
	@Override
	public int getX() {
		return bounds.x;
	}
	
	@Override
	public int getY() {
		return bounds.y;
	}
	
	@Override
	public int getWidth() {
		return bounds.width;
	}
	
	@Override
	public int getHeight() {
		return bounds.height;
	}
	
	@Override
	public int getPadding() {
		return padding;
	}

	@Override
	public IColField getColField() {
		return colField;
	}
	
	//Setters
	
}
