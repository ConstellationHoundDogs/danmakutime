package nl.weeaboo.dt.object;

import java.awt.Font;

import nl.weeaboo.game.text.MutableTextStyle;

public class TextStyle implements ITextStyle {

	private MutableTextStyle style;
	
	public TextStyle() {
		style = new MutableTextStyle(null, Font.PLAIN, 12f);
	}
	
	//Functions
	public MutableTextStyle getInnerStyle() {
		return style;
	}
	
	//Getters
	@Override
	public int getAnchor() {
		return style.getAnchor();
	}

	@Override
	public int getColor() {
		return style.getColor();
	}

	@Override
	public String getFontName() {
		return style.getFontName();
	}

	@Override
	public double getFontSize() {
		return style.getFontSize();
	}

	@Override
	public FontStyle getFontStyle() {
		return FontStyle.fromInt(style.getFontStyle());
	}

	@Override
	public int getOutlineColor() {
		return style.getOutlineColor();
	}

	@Override
	public double getOutlineSize() {
		return style.getOutlineSize();
	}

	@Override
	public boolean isUnderlined() {
		return style.isUnderlined();
	}
	
	//Setters
	@Override
	public void setAnchor(int a) {
		style.setAnchor(a);
	}

	@Override
	public void setColor(double r, double g, double b) {
		int ri = Math.max(0, Math.min(255, (int)Math.round(r * 255.0)));
		int gi = Math.max(0, Math.min(255, (int)Math.round(g * 255.0)));
		int bi = Math.max(0, Math.min(255, (int)Math.round(b * 255.0)));
		style.setColor(ri, gi, bi);
	}

	@Override
	public void setColor(int rgb) {
		style.setColor(0xFF000000 | rgb);
	}

	@Override
	public void setFontName(String f) {
		style.setFontName(f);
	}

	@Override
	public void setFontSize(double s) {
		style.setFontSize((float)s);
	}

	@Override
	public void setFontStyle(FontStyle s) {
		style.setFontStyle(s.intValue());
	}

	@Override
	public void setOutlineColor(double r, double g, double b) {
		int ri = Math.max(0, Math.min(255, (int)Math.round(r * 255.0)));
		int gi = Math.max(0, Math.min(255, (int)Math.round(g * 255.0)));
		int bi = Math.max(0, Math.min(255, (int)Math.round(b * 255.0)));
		style.setOutlineColor(ri, gi, bi);
	}

	@Override
	public void setOutlineColor(int rgb) {
		style.setOutlineColor(0xFF000000 | rgb);
	}

	@Override
	public void setOutlineSize(double s) {
		style.setOutlineSize((float)s);
	}

	@Override
	public void setUnderlined(boolean u) {
		style.setUnderlined(u);
	}
	
}
