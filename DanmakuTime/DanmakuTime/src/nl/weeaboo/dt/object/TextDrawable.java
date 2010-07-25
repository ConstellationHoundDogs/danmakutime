package nl.weeaboo.dt.object;

import nl.weeaboo.dt.renderer.IRenderer;

public class TextDrawable extends Drawable implements ITextDrawable {

	private int width;
	private String text;
	private int blockAnchor;
	private ITextStyle textStyle;
	
	public TextDrawable() {
		blockAnchor = 5;
		textStyle = new TextStyle();
	}
	
	//Functions
	@Override
	public void drawGeometry(IRenderer r) {
		StyledText stext = new StyledText(text, textStyle);
		
		r.drawText(stext, getX(), getY(), getZ(), getDrawAngle(), width, blockAnchor);		
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
	public void setTextAnchor(int anchor) {
		textStyle.setAnchor(anchor);
	}

	@Override
	public void setBlockAnchor(int anchor) {
		blockAnchor = (anchor >= 1 && anchor <= 9 ? anchor : 7);
	}

	@Override
	public void setTextColor(double r, double g, double b) {
		textStyle.setColor(r, g, b);
	}

	@Override
	public void setTextColor(int rgb) {
		textStyle.setColor(rgb);
	}
	
	@Override
	public void setFontName(String f) {
		textStyle.setFontName(f);
	}

	@Override
	public void setFontSize(double s) {
		textStyle.setFontSize(s);
	}

	@Override
	public void setFontStyle(FontStyle s) {
		textStyle.setFontStyle(s);
	}

	@Override
	public void setFont(String fn, FontStyle fs, double sz) {
		textStyle.setFont(fn, fs, sz);
	}
	
	@Override
	public void setOutlineColor(double r, double g, double b) {
		textStyle.setOutlineColor(r, g, b);
	}

	@Override
	public void setOutlineColor(int rgb) {
		textStyle.setOutlineColor(rgb);
	}

	@Override
	public void setOutlineSize(double s) {
		textStyle.setOutlineSize(s);
	}

	@Override
	public void setUnderlined(boolean u) {
		textStyle.setUnderlined(u);
	}
	
}
