package nl.weeaboo.dt.netplay;

import java.nio.ByteBuffer;

public class CompositeNetworkMessage extends NetworkMessage {

	public static final int HEADER_SIZE = NetworkMessage.HEADER_SIZE + 2;
	public static final int MAX_PACKET_SIZE = 512;
	
	public CompositeNetworkMessage(NetworkMessage messages[]) {
		super(Type.COMPOSITE, 2 + len(messages));
		
		if (len(messages) > MAX_PACKET_SIZE) {
			throw new IllegalArgumentException("Too much data for a single packet");
		}
		
		buf.putShort((short)messages.length);
		for (NetworkMessage msg : messages) {
			buf.putShort((short)msg.getDataSize());
			buf.put(msg.getData());
		}
				
		buf.rewind();		
	}
	public CompositeNetworkMessage(NetworkMessage msg) {
		super(msg);
	}

	//Functions
	protected static int len(NetworkMessage messages[]) {
		int sum = 0;
		for (NetworkMessage msg : messages) {
			sum += 2 + msg.getDataSize();
		}
		return sum;
	}
	
	//Getters
	public int getMessagesCount() {
		return buf.getShort(getHeaderSize());
	}
	
	public NetworkMessage[] getMessages() {
		ByteBuffer buf = getPayLoad();
		
		int count = buf.getShort();
		NetworkMessage messages[] = new NetworkMessage[count];
		for (int n = 0; n < count; n++) {
			int size = buf.getShort();
			ByteBuffer buf2 = ByteBuffer.allocate(size);
			buf.get(buf2.array());
			
			messages[n] = NetworkMessage.fromByteBuffer(buf2, getAddress());
		}
		
		return messages;
	}
	
	//Setters
	
}
