package nl.weeaboo.dt.netplay;

import java.util.Arrays;

import nl.weeaboo.dt.input.VKey;

public final class KeyboardMessage extends UDPNetworkMessage {

	public KeyboardMessage(int playerId, long frame, int pressed[], int held[]) {
		super(Type.INPUT_KEYBOARD, playerId, frame, (2 + 2 * pressed.length) + (2 + 2 * held.length));
		
		if (pressed.length > 0x7FFF) throw new IllegalArgumentException("pressed array too large (max=32767): " + pressed.length);
		if (held.length > 0x7FFF) throw new IllegalArgumentException("held array too large (max=32767): " + held.length);
		
		buf.putShort((short)pressed.length);
		for (int key : pressed) {
			if (key > 0x7FFF) throw new IllegalArgumentException("key code too large (max=32767): " + key);

			buf.putShort((short)key);
		}
		
		buf.putShort((short)held.length);
		for (int key : held) {
			if (key > 0x7FFF) throw new IllegalArgumentException("key code too large (max=32767): " + key);

			buf.putShort((short)key);
		}
		
		buf.rewind();
	}
	protected KeyboardMessage(NetworkMessage msg) {
		super(msg);
	}

	//Functions
	public static VKey[] filterVKeysByPlayer(int player, int keys[]) {
		VKey result[] = new VKey[keys.length];
		
		int t = 0;
		for (int key : keys) {
			if (VKey.getPlayerFromKeyCode(key) == player) {
				result[t++] = VKey.getEnumFromKeyCode(key);
			}
		}
		
		return Arrays.copyOf(result, t);
	}
	
	//Getters
	@Override
	public boolean isValid() {
		return super.isValid() && getType() == Type.INPUT_KEYBOARD
			&& getPressedCount() >= 0 && getHeldCount() >= 0;
	}

	protected int getPressedCountOffset() {
		return getHeaderSize();
	}
	public int getPressedCount() {
		return buf.getShort(getHeaderSize());
	}
	public int getPressed(int index) {
		return buf.getShort(getHeaderSize() + 2 + 2 * index);
	}	
	
	protected int getHeldCountOffset() {
		return getPressedCountOffset() + 2 + 2 * getPressedCount();		
	}
	public int getHeldCount() {
		return buf.getShort(getHeldCountOffset());
	}
	public int getHeld(int index) {
		return buf.getShort(getHeldCountOffset() + 2 + 2 * index);
	}	
	
	//Setters
	
}
