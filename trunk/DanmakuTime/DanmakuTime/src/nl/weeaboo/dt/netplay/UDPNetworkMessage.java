package nl.weeaboo.dt.netplay;


public class UDPNetworkMessage extends NetworkMessage {

	private static final int headerSize = 5;
	
	public UDPNetworkMessage(Type t, int playerId, long frame, int size) {
		super(t, headerSize + size);
		
		if (playerId > 0x7F) throw new IllegalArgumentException("playerId too large (max=127): " + playerId);
		if (frame > 0x7FFFFFFF) throw new IllegalArgumentException("frame too large (max=2147483647): " + frame);
		if (size > 0x7FFF) throw new IllegalArgumentException("size too large (max=32767): " + size);
		
		buf.put((byte)playerId);
		buf.putInt((int)frame);
	}
	protected UDPNetworkMessage(NetworkMessage msg) {
		super(msg);
	}
	
	//Functions
	
	//Getters
	@Override
	public boolean isValid() {
		return super.isValid() && getPlayerId() >= 0 && getFrame() > 0;
	}
	
	@Override
	public int getHeaderSize() {
		return super.getHeaderSize() + headerSize;
	}
	
	public int getPlayerId() {
		return buf.get(super.getHeaderSize() + 0);
	}
	
	public long getFrame() {
		return buf.getInt(super.getHeaderSize() + 1);
	}
			
	//Setters
	
}
