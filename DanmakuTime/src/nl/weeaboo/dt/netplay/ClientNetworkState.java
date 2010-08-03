package nl.weeaboo.dt.netplay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.dt.DTLog;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.input.Input;
import nl.weeaboo.dt.input.InputBuffer;
import nl.weeaboo.dt.input.VKey;

public class ClientNetworkState extends NetworkState {

	private InetSocketAddress serverAddr;
	private Collection<Integer> localPlayers;
	private long randomSeed;
	private boolean gameStarted;
	private InputBuffer inputBuffer;
	
	private final int maxLagFrames;
	private Map<Long, NetworkMessage[]> sendBuffer;
	
	public ClientNetworkState(byte resHash[], InputBuffer in, int maxLag) {
		super(resHash);
		
		inputBuffer = in;
		inputBuffer.setNumPlayers(2);
		
		maxLagFrames = maxLag;
		
		sendBuffer = new LinkedHashMap<Long, NetworkMessage[]>();
	}

	//Functions
	public void join(InetAddress targetIP, int targetTCPPort, int localUDPPort) throws IOException {
		gameStarted = false;
		inputBuffer.clear();
		
		serverAddr = new InetSocketAddress(targetIP, targetTCPPort);
		localPlayers = new LinkedHashSet<Integer>();

		network.join(targetIP, targetTCPPort, localUDPPort);
		
		String playerNames[] = new String[] { "Player-" + hashCode() };		
		network.sendTCP(new ConnectMessage(playerNames, localUDPPort));
	}
	
	public boolean isGameStarted() {
		return gameStarted;
	}
	
	public void buffer(long frame, int futureKeysPressed[], int futureKeysHeld[],
			int futureVKeysPressed[], int futureVKeysHeld[])
	{
		Iterator<Entry<Long, NetworkMessage[]>> itr = sendBuffer.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<Long, NetworkMessage[]> entry = itr.next();
			if (entry.getKey() < frame - maxLagFrames*2) {
				itr.remove();
			}
		}
		
		List<NetworkMessage> q = new ArrayList<NetworkMessage>();
		
		//Full message send
		if (localPlayers.contains(Integer.valueOf(1))) {
			q.add(new KeyboardMessage(0, frame, futureKeysPressed, futureKeysHeld));
		}
		
		//VKeys send
		int t = 1;
		for (int pid : localPlayers) {
			VKey p[] = VKeyMessage.filterVKeysByPlayer(t, futureVKeysPressed);
			VKey h[] = VKeyMessage.filterVKeysByPlayer(t, futureVKeysHeld);
			
			q.add(new VKeyMessage(pid, frame, p, h));
			t++;
		}

		//Add to send buffer
		sendBuffer.put(frame, q.toArray(new NetworkMessage[q.size()]));
	}
	
	public void send() {		
		//Transmit entire history

		int totalData = 0;
		int totalPackets = 0;
		
		int sizeLimit = CompositeNetworkMessage.MAX_PACKET_SIZE;
		List<NetworkMessage> buffer = new ArrayList<NetworkMessage>();
		
		//Put sendlist in a 1-dimensional list
		List<NetworkMessage> sendList = new ArrayList<NetworkMessage>();
		for (Entry<Long, NetworkMessage[]> entry : sendBuffer.entrySet()) {
			for (NetworkMessage msg : entry.getValue()) {
				sendList.add(msg);
			}
		}
		
		//Group messages into composites, and send those
		int headerSize = CompositeNetworkMessage.HEADER_SIZE;
		int size = headerSize;
		for (NetworkMessage msg : sendList) {
			int dataL = 4 + msg.getDataSize();
			if (size + dataL >= sizeLimit) {
				NetworkMessage m = new CompositeNetworkMessage(buffer.toArray(new NetworkMessage[buffer.size()]));
				totalData += m.getDataSize();
				totalPackets++;
				network.sendUDP(m);
				buffer.clear();
				size = headerSize;
			}
			buffer.add(msg);
			size += dataL;
		}
		if (buffer.size() > 0) {
			NetworkMessage m = new CompositeNetworkMessage(buffer.toArray(new NetworkMessage[buffer.size()]));
			totalData += m.getData().limit();
			totalPackets++;
			network.sendUDP(m);
			buffer.clear();
			size = headerSize;
		}
		
		//System.out.printf("UDP [%d frames] [%d packets] => %d bytes\n", maxLagFrames*2, totalPackets, totalData);
	}
	
	protected void receiveMessage(NetworkMessage msg) {
		switch (msg.getType()) {
		case CONNECT_RESPONSE: recvConnectResponseMessage(msg); break;
		case GAME_START: recvGameStartMessage(msg); break;
		case INPUT_KEYBOARD: recvInputKeyboard(msg); break;
		case INPUT_VKEYS: recvInputVKeys(msg); break;
		default: super.receiveMessage(msg);
		}
	}

	protected void recvConnectResponseMessage(NetworkMessage msg) {
		if (!localPlayers.isEmpty()) {
			//throw new IllegalArgumentException("Received a duplicate accept message!");
			return; //Probably someone else's
		}
		
		ConnectResponseMessage cm = new ConnectResponseMessage(msg);
		assertMessageValid(cm);

		int size = cm.getPlayerCount();
		for (int n = 0; n < size; n++) {
			localPlayers.add(Integer.valueOf(cm.getPlayerId(n)));
		}
	}

	protected void recvGameStartMessage(NetworkMessage msg) {
		GameStartMessage gm = new GameStartMessage(msg);
		assertMessageValid(gm);

		randomSeed = gm.getRandomSeed();
		
		ByteBuffer pd = gm.getPersistentData();
		storage = ByteBuffer.allocate(pd.remaining());
		storage.put(pd);
		storage.rewind();
		
		ByteBuffer rh = gm.getResourcesHash();
		if (resHash.compareTo(rh) != 0) {
			resHash.rewind();
			DTLog.showError("Resources hash is different for client and host -- aborting connection attempt.");
			stop();
			return;
		}
		resHash.rewind();
		
		int numPlayers = gm.getPlayerCount();
		ByteBuffer pbuf = gm.getPlayerData();
		for (int n = 0; n < numPlayers; n++) {
			@SuppressWarnings("unused")
			int pid = pbuf.getInt();
			byte ipBytes[] = new byte[pbuf.getInt()];
			pbuf.get(ipBytes);
			int port = pbuf.getInt();
			
			try {
				InetAddress addr = InetAddress.getByAddress(ipBytes);
				network.addUDPTarget(addr, port);
				System.out.println("Client :: New UDP Target: " + addr.getHostAddress() + ":" + port);
			} catch (UnknownHostException e) {
				DTLog.error(e);
			}
		}
		
		gameStarted = true;
	}
	
	protected void recvInputKeyboard(NetworkMessage msg) {
		KeyboardMessage km = new KeyboardMessage(msg);
		assertMessageValid(km);
		
		IInput input = new Input();
		
		int pressedL = km.getPressedCount();
		for (int n = 0; n < pressedL; n++) {
			input.setKeyPressed(km.getPressed(n));
		}
		
		int heldL = km.getHeldCount();
		for (int n = 0; n < heldL; n++) {					
			input.setKeyHeld(km.getHeld(n));
		}

		inputBuffer.addKeys(km.getFrame(), 0, input);
	}
	
	protected void recvInputVKeys(NetworkMessage msg) {
		VKeyMessage km = new VKeyMessage(msg);
		assertMessageValid(km);
		
		int msgPlayer = km.getPlayerId();
		
		IInput input = new Input();

		int pressedL = km.getPressedCount();
		for (int n = 0; n < pressedL; n++) {
			VKey key = VKey.fromOrdinal(km.getPressed(n));
			if (key != null) {
				input.setKeyPressed(key.toKeyCode(msgPlayer));
			}
		}	
		
		int heldL = km.getHeldCount();
		for (int n = 0; n < heldL; n++) {					
			VKey key = VKey.fromOrdinal(km.getHeld(n));
			if (key != null) {
				input.setKeyHeld(key.toKeyCode(msgPlayer));
			}
		}
		
		inputBuffer.addKeys(km.getFrame(), km.getPlayerId(), input);
	}
	
	//Getters
	public InetSocketAddress getServerAddress() {
		return serverAddr;
	}
	public long getRandomSeed() {
		if (!isGameStarted()) {
			throw new IllegalStateException("Random seed not available before init");
		}
		return randomSeed;
	}
	
	//Setters
	
}
