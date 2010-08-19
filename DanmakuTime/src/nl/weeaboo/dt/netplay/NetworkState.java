package nl.weeaboo.dt.netplay;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import nl.weeaboo.dt.netplay.NetworkMessage.Type;

public class NetworkState {

	protected final Network network;
	protected final ByteBuffer resHash;
	protected ByteBuffer storage; //Persistent storage file of the game's host
	
	public NetworkState(byte hash[]) {
		resHash = ByteBuffer.allocate(hash.length);
		resHash.put(hash);
		resHash.rewind();
		
		network = new Network();
	}
	
	//Functions
	public void stop() {
		network.stop();
	}
	
	public void receive() throws IOException {
		if (!network.isReceiving()) {
			throw new EOFException("Receiver thread has been terminated");
		}
		
		NetworkMessage messages[] = network.receive();
		for (NetworkMessage msg : messages) {
			receiveMessage(msg);
		}
	}
	
	protected void receiveMessage(NetworkMessage msg) {
		if (msg.getType() == Type.COMPOSITE) {
			CompositeNetworkMessage c = new CompositeNetworkMessage(msg);
			for (NetworkMessage inner : c.getMessages()) {
				receiveMessage(inner);
			}
			return;
		}
		System.err.println("Unknown message type: " + msg.getType());
	}
		
	protected void assertMessageValid(NetworkMessage msg) {
		if (!msg.isValid()) {
			throw new RuntimeException(String.format("Message invalid (%s)", msg.getType().toString()));
		}
	}
	
	//Getters
	public ByteBuffer getPersistentStorage() {
		assert(storage != null);
		
		return storage;
	}
	
	//Setters
	
}
