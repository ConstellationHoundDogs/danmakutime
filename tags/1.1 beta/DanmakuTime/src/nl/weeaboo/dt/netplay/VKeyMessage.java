package nl.weeaboo.dt.netplay;

import java.util.Arrays;

import nl.weeaboo.dt.input.VKey;

public final class VKeyMessage extends UDPNetworkMessage {

	public VKeyMessage(int playerId, long frame, VKey pressed[], VKey held[]) {
		super(Type.INPUT_VKEYS, playerId, frame, (1 + pressed.length) + (1 + held.length));
		
		if (pressed.length > 0x7F) throw new IllegalArgumentException("pressed array too large (max=127): " + pressed.length);
		if (held.length > 0x7F) throw new IllegalArgumentException("held array too large (max=127): " + held.length);

		if (VKey.values().length > 0x7F) throw new IllegalArgumentException("VKey enum has too many values (max=127): " + VKey.values().length);

		buf.put((byte)pressed.length);
		for (VKey key : pressed) {
			buf.put((byte)key.ordinal());
		}
		
		buf.put((byte)held.length);
		for (VKey key : held) {
			buf.put((byte)key.ordinal());
		}
		
		buf.rewind();
	}
	public VKeyMessage(NetworkMessage msg) {
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
		return super.isValid() && getType() == Type.INPUT_VKEYS
			&& getPressedCount() >= 0 && getHeldCount() >= 0;
	}
	
	protected int getPressedCountOffset() {
		return getHeaderSize();
	}
	public int getPressedCount() {
		return buf.get(getHeaderSize());
	}
	public int getPressed(int index) {
		return buf.get(getHeaderSize() + 1 + index);
	}	
	
	protected int getHeldCountOffset() {
		return getPressedCountOffset() + 1 + getPressedCount();		
	}
	public int getHeldCount() {
		return buf.get(getHeldCountOffset());
	}
	public int getHeld(int index) {
		return buf.get(getHeldCountOffset() + 1 + index);
	}	
	
	//Setters
	
}
