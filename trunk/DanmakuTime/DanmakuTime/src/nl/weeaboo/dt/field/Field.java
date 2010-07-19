package nl.weeaboo.dt.field;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.object.IDrawable;
import nl.weeaboo.dt.renderer.IRenderer;

public class Field implements IField {

	private Rectangle bounds;
	private Collection<IDrawable> drawables;
	private Collection<IDrawable> standbyList;
	
	public Field(int x, int y, int w, int h) {
		bounds = new Rectangle(x, y, w, h);
		
		drawables = new LinkedHashSet<IDrawable>();
		standbyList = new ArrayList<IDrawable>();
	}
	
	//Functions
	@Override
	public void add(IDrawable d) {
		standbyList.add(d);
	}

	@Override
	public void update(IInput input) {
		drawables.addAll(standbyList);
		standbyList.clear();
		
		Iterator<IDrawable> itr = drawables.iterator();
		while (itr.hasNext()) {
			IDrawable d = itr.next();
			if (!d.isDestroyed()) {
				d.update(input);
			}
			if (d.isDestroyed()) {
				itr.remove();
			}
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
			if (!d.isDestroyed()) {
				d.draw(renderer);
			}
		}
		renderer.translate(-dx, -dy);		

		renderer.setClipRect(oldClip.x, oldClip.y, oldClip.width, oldClip.height);
	}
	
	//Getters
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
	
	//Setters
	
}
