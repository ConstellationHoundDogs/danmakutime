package nl.weeaboo.dt.netplay;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nl.weeaboo.dt.DTLog;

public class Network {

	private ServerSocketChannel tcpServerChannel;
	private List<SocketChannel> tcpChannels;
	private DatagramChannel udpChannel;
	private List<InetSocketAddress> targets;
	private Selector selector;
	private ReceiveTask receiveTask;
	
	private int bufferSize = 64 << 10;
	
	public Network() {
		targets = new ArrayList<InetSocketAddress>(4);
	}
	
	//Functions
	public void stop() {
		receiveTask.stop();
	}
	
	public void host(int tcpPort) throws IOException {
		selector = Selector.open();
		
		tcpServerChannel = ServerSocketChannel.open();
		tcpServerChannel.configureBlocking(false);
		
		ServerSocket socket = tcpServerChannel.socket();
		socket.bind(new InetSocketAddress(tcpPort));
		
		System.out.println("HOSTING: " + socket.getInetAddress().getHostAddress()+":"+tcpPort);
		
		tcpChannels = new ArrayList<SocketChannel>();

		//initUDP(udpPort);	
		
		Thread t = new Thread(receiveTask = new ReceiveTask(selector, bufferSize));
		t.setDaemon(true);
		t.start();
	}
	
	public void join(InetAddress targetAddress, int targetTCPPort,
			int localUDPPort) throws IOException
	{
		selector = Selector.open();

		tcpServerChannel = null;
		
		SocketChannel tcpChannel = SocketChannel.open();
		
		System.out.println("JOINING: " + targetAddress+":"+targetTCPPort);

		Socket tcpSocket = tcpChannel.socket();
		tcpSocket.setReceiveBufferSize(bufferSize);
		tcpSocket.connect(new InetSocketAddress(targetAddress, targetTCPPort));
		
		tcpChannels = new ArrayList<SocketChannel>();
		tcpChannels.add(tcpChannel);

		tcpChannel.configureBlocking(false);
		tcpChannel.register(selector, SelectionKey.OP_READ);
		
		initUDP(localUDPPort);	
		
		Thread t = new Thread(receiveTask = new ReceiveTask(selector, bufferSize));
		t.setDaemon(true);
		t.start();
	}
	
	protected void initUDP(int udpPort) throws IOException {
		udpChannel = DatagramChannel.open();
		udpChannel.configureBlocking(false);
		
		DatagramSocket udpSocket = udpChannel.socket();
		udpSocket.setReceiveBufferSize(bufferSize);
		udpSocket.bind(new InetSocketAddress(udpPort));		

		System.out.println("UDP Socket: " + udpSocket.getLocalAddress()+":"+udpSocket.getLocalPort());
		
		udpChannel.register(selector, SelectionKey.OP_READ);
		
		targets = new ArrayList<InetSocketAddress>();
	}

	public void addUDPTarget(InetAddress targetIP, int targetPort) {
		targets.add(new InetSocketAddress(targetIP, targetPort));
	}
	
	public void sendTCP(NetworkMessage msg) {
		ByteBuffer msgData = msg.getData();
		int oldpos = msgData.position();
		
		ByteBuffer buf = ByteBuffer.allocate(4 + msgData.limit());
		buf.putInt(msgData.remaining());
		buf.put(msgData);
		buf.rewind();
		
		msgData.position(oldpos);
		
		Iterator<SocketChannel> itr = tcpChannels.iterator();
		while (itr.hasNext()) {
			try {
				SocketChannel ch = itr.next();
				
				buf.rewind();
				while (buf.remaining() > 0) {
					ch.write(buf);
				}
			} catch (IOException ioe) {
				error(ioe.toString());
			}
		}
	}
	public void sendUDP(NetworkMessage msg) {
		ByteBuffer buf = msg.getData();
		if (buf.remaining() > NetworkMessage.MAX_UDP_PACKET_SIZE) {
			throw new IllegalArgumentException("Too much data for a single UDP packet: " + buf.remaining());
		}
		
		Iterator<InetSocketAddress> itr = targets.iterator();
		while (itr.hasNext()) {
			InetSocketAddress addr = itr.next();
			
			int oldpos = buf.position();
			try {
				udpChannel.send(buf, addr);
			} catch (IOException ioe) {
				error(ioe.toString());
			} finally {
				buf.position(oldpos);
			}
		}
	}
	
	public void waitForTCPConnection() throws IOException {
		SocketChannel ch = tcpServerChannel.accept();
		if (ch != null) {
			ch.configureBlocking(false);
			ch.register(selector, SelectionKey.OP_READ);
			tcpChannels.add(ch);
		}
	}
	
	protected void info(String format, Object... args) {
		synchronized (Network.class) {
			System.out.printf("[%d] ", udpChannel.socket().getPort());
			if (args.length > 0) {
				System.out.printf(format, args);
			} else {
				System.out.print(format);
			}
			System.out.println();
		}
	}
	
	protected void error(String format, Object... args) {
		synchronized (Network.class) {
			System.err.printf("[%d] ", udpChannel.socket().getPort());
			if (args.length > 0) {
				System.err.printf(format, args);
			} else {
				System.err.print(format);
			}
			System.err.println();
		}
	}
	
	//Getters
	public boolean isConnectedTo(InetSocketAddress addr) {
		return isConnectedTo(addr.getAddress(), addr.getPort());
	}
	public boolean isConnectedTo(InetAddress addr, int port) {
		for (InetSocketAddress socketAddr : targets) {
			if (socketAddr.getAddress().equals(addr) && socketAddr.getPort() == port) {
				return true;
			}
		}
		return false;
	}
	
	public NetworkMessage[] receive() {
		return receiveTask.receive();
	}
	
	public boolean isReceiving() {
		return receiveTask != null && !receiveTask.isTerminated();
	}
	
	//Setters

	//Inner Classes
	private static class ReceiveTask implements Runnable {
		
		private volatile boolean stop;
		private volatile boolean terminated;
		
		private Selector selector;
		private ByteBuffer readBuffer;
		private List<NetworkMessage> packets;
		
		public ReceiveTask(Selector selector, int bufferSize) {
			this.selector = selector;
			this.readBuffer = ByteBuffer.allocate(Math.max(1024, bufferSize));
			this.packets = new ArrayList<NetworkMessage>();
		}
		
		public void stop() {
			stop = true;
		}
		
		public boolean isTerminated() {
			return terminated;
		}
		
		public void run() {
			while (!stop) {
				try {
					selector.select(25);
				} catch (IOException ioe) {
					DTLog.error(ioe);
					break;
				}
				
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> itr = keys.iterator();
				try {		
					while (itr.hasNext()) {
						SelectionKey key = itr.next();
						//System.out.println(key.channel() + " " + key.readyOps());
						
						SelectableChannel ch = key.channel();
																
						InetSocketAddress addr;
						if (ch instanceof DatagramChannel) {
							DatagramChannel dch = (DatagramChannel)ch;

							int n;
							for (n = 0; n < 64; n++) { //Read max 64 at a time
								readBuffer.rewind();
								readBuffer.limit(readBuffer.capacity());
								addr = (InetSocketAddress)dch.receive(readBuffer);
								
								if (addr == null) {									
									break;
								}
								
								readBuffer.limit(readBuffer.position());
								readBuffer.rewind();
								NetworkMessage msg = NetworkMessage.fromByteBuffer(readBuffer, addr);
								
								synchronized (packets) {
									packets.add(msg);
								}
							}							
						} else {
							SocketChannel sch = (SocketChannel)ch;
							addr = (InetSocketAddress)sch.socket().getRemoteSocketAddress();

							readBuffer.rewind();
							readBuffer.limit(4);
							while (readBuffer.remaining() > 0) {
								sch.read(readBuffer);
							}
							int size = readBuffer.getInt(0);
							readBuffer.limit(size);
							readBuffer.rewind();
							while (readBuffer.remaining() > 0) {
								sch.read(readBuffer);
							}
							
							itr.remove();

							readBuffer.limit(readBuffer.position());
							readBuffer.rewind();
							NetworkMessage msg = NetworkMessage.fromByteBuffer(readBuffer, addr);
							
							synchronized (packets) {
								packets.add(msg);
							}
						}						
					}
				} catch (IOException ioe) {
					DTLog.error(ioe);
					terminated = true;
					return;
				}
			}
			
			terminated = true;
		}
		
		public NetworkMessage[] receive() {
			synchronized (packets) {
				NetworkMessage arr[] = packets.toArray(new NetworkMessage[packets.size()]);
				packets.clear();
				return arr;
			}
		}
	}
	
}
