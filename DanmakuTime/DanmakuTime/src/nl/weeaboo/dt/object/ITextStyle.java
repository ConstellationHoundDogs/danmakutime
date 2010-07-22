package nl.weeaboo.dt.object;


public interface ITextStyle {

	// === Functions ===========================================================
	
	// === Getters =============================================================
	public String getFontName();
	public FontStyle getFontStyle();
	public double getFontSize();
	public int getColor();
	public int getAnchor();
	public boolean isUnderlined();
	public double getOutlineSize();
	public int getOutlineColor();
	
	// === Setters =============================================================
	public void setFontName(String f);
	public void setFontStyle(FontStyle s);
	public void setFontSize(double s);
	public void setColor(double r, double g, double b);
	public void setColor(int rgb);
	public void setAnchor(int a);
	public void setUnderlined(boolean u);
	public void setOutlineSize(double s);
	public void setOutlineColor(double r, double g, double b);
	public void setOutlineColor(int rgb);
	
}
