package nl.weeaboo.dt.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.object.IDrawable;
import nl.weeaboo.dt.renderer.IRenderer;

public class Field implements IField {

	private Collection<IDrawable> drawables;
	private Collection<IDrawable> standbyList;
	
	public Field() {
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
		for (IDrawable d : drawables) {
			if (!d.isDestroyed()) {
				d.draw(renderer);
			}
		}
	}
	
	//Getters
	@Override
	public int getObjectCount() {
		return drawables.size();
	}
	
	//Setters
	
}
