package nl.weeaboo.dt.netplay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerNetworkState extends NetworkState {

	private final int maxPlayers;
	private int connectedPlayers;
	private List<ConnectionState> clients;
	private long randomSeed;
	private boolean gameStarted;

	public ServerNetworkState(byte resHash[], int maxPlayers) {
		super(resHash);
		
		this.maxPlayers = maxPlayers;
	}

	//Functions
	public void host(int tcpPort, ByteBuffer persistentStorage) throws IOException {
		storage = persistentStorage;
		
		randomSeed = System.nanoTime();
		gameStarted = false;
		
		connectedPlayers = 0;
		clients = new ArrayList<ConnectionState>();
		
		network.host(tcpPort);
	}
	
	public void update() throws IOException {
		network.waitForTCPConnection();
		receive();
	}
	
	protected void receiveMessage(NetworkMessage msg) {
		switch (msg.getType()) {
		case CONNECT: recvConnect(msg); break;
		default: super.receiveMessage(msg);
		}
	}
	
	protected void recvConnect(NetworkMessage msg) {
		ConnectMessage cm = new ConnectMessage(msg);
		assertMessageValid(cm);
		
		int newPlayerIds[] = generatePlayerIds(cm.getPlayerCount());
				
		InetSocketAddress socketAddr = msg.getAddress();
		if (!network.isConnectedTo(socketAddr)) {
			if (newPlayerIds.length < cm.getPlayerCount()) {
				System.err.printf("Client connection request refused. Requested %d players, I have %d left.\n", cm.getPlayerCount(), newPlayerIds.length);
				network.sendTCP(new ConnectResponseMessage(new int[0]));
				return;
			}

			clients.add(new ConnectionState(newPlayerIds, socketAddr.getAddress(), cm.getUDPPort()));
			
			connectedPlayers += newPlayerIds.length;
			network.sendTCP(new ConnectResponseMessage(newPlayerIds));
						
			if (connectedPlayers >= maxPlayers) {
				gameStart();
			}
		}
	}
	
	protected void gameStart() {
		int playerIds[] = new int[connectedPlayers];
		InetAddress addrs[] = new InetAddress[connectedPlayers];
		int ports[] = new int[connectedPlayers];

		int t = 0;
		for (ConnectionState state : clients) {
			for (int id : state.getPlayerIds()) {
				playerIds[t] = id;
				addrs[t] = state.getAddress();
				ports[t] = state.getUDPPort();
				t++;
			}
		}
		
		gameStarted = true;
		network.sendTCP(new GameStartMessage(playerIds, addrs, ports,
				randomSeed, resHash, storage));		
	}
	
	protected int[] generatePlayerIds(int num) {
		num = Math.min(maxPlayers, num);
		
		int result[] = new int[num];		
		int n = 0;
		while (n < num) {
			int id = generatePlayerId();
			if (id < 0) {
				break;
			}
			result[n++] = id;
		}
		return Arrays.copyOf(result, n);
	}
	
	protected int generatePlayerId() {
		for (int pid = 1; pid <= maxPlayers; pid++) {
			boolean ok = true;
			for (ConnectionState cs : clients) {
				if (cs.hasPlayer(pid)) {
					ok = false;
				}
			}
			
			if (ok) {
				return pid;
			}
		}
		return -1;
	}
	
	//Getters
	public boolean isGameStarted() {
		return gameStarted;		
	}
	
	//Setters
	
	//Inner Classes
	private static class ConnectionState {
		
		private int playerIds[];
		private InetAddress addr;
		private int udpPort;
		
		public ConnectionState(int pids[], InetAddress addr, int udpPort) {
			this.playerIds = pids.clone();
			this.addr = addr;
			this.udpPort = udpPort;
		}
		
		public int[] getPlayerIds() {
			return playerIds;
		}
		public InetAddress getAddress() {
			return addr;
		}
		public int getUDPPort() {
			return udpPort;
		}
		
		public boolean hasPlayer(int pid) {
			for (int n = 0; n < playerIds.length; n++) {
				if (playerIds[n] == pid) {
					return true;
				}
			}
			return false;
		}
		
	}
	
}
