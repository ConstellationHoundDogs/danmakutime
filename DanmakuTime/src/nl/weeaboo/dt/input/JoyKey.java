package nl.weeaboo.dt.input;

public enum JoyKey {
	UP, DOWN, LEFT, RIGHT,
	BUTTON1, BUTTON2, BUTTON3, BUTTON4,
	BUTTON5, BUTTON6, BUTTON7, BUTTON8;
	
	public static int MAX_JOYPADS = 8;
	
	public static JoyKey fromOrdinal(int id) {
		if (id >= 0 && id < JoyKey.values().length) {
			return values()[id];
		}
		return null;
	}
	
	public static JoyKey fromName(String nm) {
		for (JoyKey value : values()) {
			if (value.name().equalsIgnoreCase(nm)) {
				return value;
			}
		}
		return null;
	}
	
	public short toKeyCode(int joypadIndex) {
		if (joypadIndex <= 0 || joypadIndex > MAX_JOYPADS) throw new IllegalArgumentException("Joypad index outside of valid range: " + joypadIndex);
		
		return (short)(20000 + 100 * joypadIndex + ordinal());		
	}

	public String toKeyName(int joypadIndex) {
		if (joypadIndex <= 0 || joypadIndex > MAX_JOYPADS) throw new IllegalArgumentException("Joypad index outside of valid range: " + joypadIndex);

		return String.format("JOY%d_%s", joypadIndex, name());
	}
	
}
