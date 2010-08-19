package nl.weeaboo.dt.netplay;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import nl.weeaboo.common.io.ByteBufferInputStream;
import nl.weeaboo.dt.GameEnv;

public final class GameStartMessage extends NetworkMessage {

	public GameStartMessage(int playerIds[], InetAddress addrs[], int ports[],
			ByteBuffer genv)
	{
		super(Type.GAME_START, 4 + genv.limit() + (4 + playerIds.length * 12 + len(addrs)));
		
		buf.putInt(genv.limit());
		int oldpos = genv.position();
		buf.put(genv);
		genv.position(oldpos);
		
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
	
	public GameEnv getGameEnv() throws IOException {
		ByteBuffer buf = getPayLoad();
		int length = buf.getInt();
		buf.limit(buf.position() + length);
		return GameEnv.fromInput(new ByteBufferInputStream(buf));
	}
	
	protected int getPlayerCountOffset() {
		return buf.getInt(getHeaderSize() + 0) + 4;
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
