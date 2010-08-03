package nl.weeaboo.dt.object;

import java.awt.Font;

public enum FontStyle {
	PLAIN(Font.PLAIN), BOLD(Font.BOLD), ITALIC(Font.ITALIC), BOLDITALIC(Font.BOLD|Font.ITALIC);
	
	private int id;
	
	private FontStyle(int id) {
		this.id = id;
	}
	
	public int intValue() { return id; }

	public static FontStyle fromInt(int id) {
		for (FontStyle s : values()) {
			if (s.intValue() == id) {
				return s;
			}
		}
		return null;
	}
}
