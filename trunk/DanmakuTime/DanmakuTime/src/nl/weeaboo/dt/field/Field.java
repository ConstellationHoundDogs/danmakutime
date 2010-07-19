package nl.weeaboo.dt.field;

import java.util.ArrayList;
import java.util.Collection;
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
	public boolean remove(IDrawable d) {
		return standbyList.remove(d) || drawables.remove(d);
	}

	@Override
	public void update(IInput input) {
		drawables.addAll(standbyList);
		standbyList.clear();
		
		for (IDrawable d : drawables) {
			d.update(input);
		}
	}
	
	@Override
	public void draw(IRenderer renderer) {
		for (IDrawable d : drawables) {
			d.draw(renderer);
		}
	}
	
	//Getters
	@Override
	public int getObjectCount() {
		return drawables.size();
	}
	
	//Setters
	
}
