package nl.weeaboo.dt.netplay;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public final class GameStartMessage extends NetworkMessage {

	public GameStartMessage(int playerIds[], InetAddress addrs[], int ports[],
			long randomSeed, ByteBuffer resHash, ByteBuffer persistentData)
	{
		super(Type.GAME_START, 8
				+ (4 + persistentData.remaining())
				+ (4 + resHash.remaining())
				+ (4 + playerIds.length * 12 + len(addrs)));
		
		buf.putLong(randomSeed);

		buf.putInt(persistentData.remaining());
		int oldpos = persistentData.position();
		buf.put(persistentData);
		persistentData.position(oldpos);
		
		buf.putInt(resHash.remaining());
		oldpos = resHash.position();
		buf.put(resHash);
		resHash.position(oldpos);
		
		buf.putInt(playerIds.length);
		for (int n = 0; n < playerIds.length; n++) {
			buf.putInt(playerIds[n]);
			buf.putInt(addrs[n].getAddress().length);
			buf.put(addrs[n].getAddress());
			buf.putInt(ports[n]);
		}
		
		buf.rewind();
	}
	public GameStartMessage(NetworkMessage msg) {
		super(msg);
	}

	//Functions
	private static int len(InetAddress addrs[]) {
		int bytes = 0;
		for (InetAddress addr : addrs) {
			bytes += addr.getAddress().length;
		}
		return bytes;
	}
	
	//Getters
	@Override
	public boolean isValid() {
		return super.isValid() && getPlayerCount() >= 0;
	}
	
	public long getRandomSeed() {
		return buf.getLong(getHeaderSize() + 0);
	}

	protected int getPersistentDataBytesOffset() {
		return 8;
	}
	public int getPersistentDataBytes() {
		return buf.getInt(getHeaderSize() + getPersistentDataBytesOffset());
	}
	public ByteBuffer getPersistentData() {
		ByteBuffer buf = getPayLoad();
		buf.position(buf.position() + getPersistentDataBytesOffset() + 4);
		buf.limit(buf.position() + getPersistentDataBytes());
		return buf;
	}
	
	protected int getResourcesHashOffset() {
		return getPersistentDataBytesOffset() + 4 + getPersistentDataBytes();
	}
	public int getResourcesHashBytes() {
		return buf.getInt(getHeaderSize() + getResourcesHashOffset());
	}
	public ByteBuffer getResourcesHash() {
		ByteBuffer buf = getPayLoad();
		buf.position(buf.position() + getResourcesHashOffset() + 4);
		buf.limit(buf.position() + getResourcesHashBytes());
		return buf;
	}
	
	protected int getPlayerCountOffset() {
		return getResourcesHashOffset() + 4 + getResourcesHashBytes();
	}
	public int getPlayerCount() {
		return buf.getInt(getHeaderSize() + getPlayerCountOffset());
	}
	public ByteBuffer getPlayerData() {
		ByteBuffer buf = getPayLoad();
		buf.position(buf.position() + getPlayerCountOffset() + 4);
		return buf;
	}
	
	//Setters
	
}
