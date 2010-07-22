package nl.weeaboo.dt.object;

import java.util.Arrays;

import nl.weeaboo.common.StringUtil;

public class StyledText {

	private int text[];
	private ITextStyle styles[];
	
	public StyledText(String t, ITextStyle s) {
		text = (t != null ? StringUtil.toUnicodeArray(t) : new int[0]);
		styles = new ITextStyle[text.length];
		
		Arrays.fill(styles, s);
	}
	
	//Functions
	
	//Getters
	public int[] getCharacters() {
		return text;
	}
	public ITextStyle[] getStyles() {
		return styles;
	}
	
	//Setters
	
}
