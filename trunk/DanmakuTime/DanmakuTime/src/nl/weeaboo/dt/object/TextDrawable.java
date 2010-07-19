package nl.weeaboo.dt.object;

import nl.weeaboo.dt.renderer.IRenderer;

public class TextDrawable extends Drawable implements ITextDrawable {

	private int width;
	private String text;
	
	public TextDrawable() {		
	}
	
	//Functions
	public void draw(IRenderer r) {
		boolean oldClip = r.isClipEnabled();		
		r.setClipEnabled(isClip());
		r.setTexture(texture);
		r.drawText(text, x, y, getZ(), getDrawAngle(), width);		
		r.setClipEnabled(oldClip);
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
	public void setWordWrap(int width) {
		this.width = (width <= 0 ? 0 : width);
	}
	
}
