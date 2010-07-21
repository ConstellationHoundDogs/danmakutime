package nl.weeaboo.dt.object;

import nl.weeaboo.dt.renderer.IRenderer;

public class TextDrawable extends Drawable implements ITextDrawable {

	private int width;
	private String text;
	private int anchor;
	
	public TextDrawable() {
		anchor = 7;
	}
	
	//Functions
	@Override
	public void drawGeometry(IRenderer r) {
		r.drawText(text, getX(), getY(), getZ(), getDrawAngle(), width, anchor);		
	}
	
	//Getters
	@Override
	public int getWordWrap() {
		return width;
	}
	
	//Setters
	@Override
	public void setText(String txt) {
		this.text = txt;
	}

	@Override
	public void setWidth(int width) {
		this.width = (width <= 0 ? 0 : width);
	}

	@Override
	public void setAnchor(int anchor) {
		this.anchor = (anchor >= 1 && anchor <= 9 ? anchor : 7);
	}
	
}
