package nl.weeaboo.dt.input;

public enum VKey {
	UP, DOWN, LEFT, RIGHT,
	BUTTON1, BUTTON2, BUTTON3, BUTTON4,
	BUTTON5, BUTTON6, BUTTON7, BUTTON8;
	
	public static int MAX_PLAYERS = 8;
	
	public static VKey fromOrdinal(int id) {
		if (id >= 0 && id < VKey.values().length) {
			return values()[id];
		}
		return null;
	}
	
	public static VKey fromName(String nm) {
		for (VKey value : values()) {
			if (value.name().equalsIgnoreCase(nm)) {
				return value;
			}
		}
		return null;
	}
	
	public static int getPlayerFromKeyCode(int keycode) {
		if (keycode >= 10000 && keycode < 20000) {
			keycode -= 10000;
			
			int playerId = keycode / 100;
			if (playerId >= 1 && playerId <= MAX_PLAYERS) {
				return playerId;
			}
		}
		return -1;
	}
	
	public static VKey getEnumFromKeyCode(int keycode) {
		if (keycode >= 10000 && keycode < 20000) {
			return VKey.fromOrdinal(keycode %= 100);
		}
		return null;
	}
	
	public short toKeyCode(int player) {
		if (player <= 0 || player > MAX_PLAYERS) throw new IllegalArgumentException("Player ID outside of valid range: " + player);

		return (short)(10000 + 100 * player + ordinal());		
	}
	
}
