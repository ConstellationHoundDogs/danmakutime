package nl.weeaboo.dt.netplay;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class NetworkMessage {
	
	public enum Type {
		COMPOSITE,
		CONNECT, CONNECT_RESPONSE, GAME_START,
		INPUT_KEYBOARD, INPUT_VKEYS;
		
		public static Type fromOrdinal(int id) {
			if (id >= 0 || id < Type.values().length) {
				return values()[id];			
			}
			return null;
		}
	}
	
	public static final int MAX_UDP_PACKET_SIZE = 512;
	protected static final int HEADER_SIZE = 1;

	protected ByteBuffer buf;
	private transient InetSocketAddress addr;
	
	public NetworkMessage(Type t, int size) {
		if (t.ordinal() > 127) {
			throw new IllegalArgumentException("Enum ordinal too large (max=127).");
		}
		
		buf = ByteBuffer.allocate(HEADER_SIZE + size);
		buf.put((byte)t.ordinal());
	}
	protected NetworkMessage(NetworkMessage msg) {
		this(msg.getData(), msg.getAddress());
	}
	private NetworkMessage(ByteBuffer b, InetSocketAddress addr) {
		int oldpos = b.position();
		buf = ByteBuffer.allocate(b.remaining());
		buf.put(b);
		buf.position(oldpos);
		
		this.addr = addr;
	}
	
	//Functions
	public static NetworkMessage fromByteBuffer(ByteBuffer buf, InetSocketAddress addr) {
		return new NetworkMessage(buf, addr);
	}
		
	//Getters
	public int getHeaderSize() {
		return HEADER_SIZE;
	}
	
	public ByteBuffer getData() {
		return buf.asReadOnlyBuffer();
	}
	
	public int getDataSize() {
		return buf.remaining();
	}
	
	public ByteBuffer getPayLoad() {
		ByteBuffer buf = getData();
		buf.position(buf.position() + getHeaderSize());
		return buf;
	}
	
	public Type getType() {
		return Type.fromOrdinal(buf.get(0));		
	}
	
	public boolean isValid() {
		return getType() != null;
	}
	
	public InetSocketAddress getAddress() {
		return addr;
	}
	
	//Setters
	
}
