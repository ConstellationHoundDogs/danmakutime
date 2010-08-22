package nl.weeaboo.dt.netplay;

import java.io.UnsupportedEncodingException;

import nl.weeaboo.dt.DTLog;

public final class ConnectMessage extends NetworkMessage {

	public ConnectMessage(String playerNames[], int localUDPPort) {
		super(Type.CONNECT, 4 + len(playerNames) + 4);
		
		buf.putInt(localUDPPort);
		
		buf.putInt(playerNames.length);
		for (String playerName : playerNames) {
			try {
				byte bytes[] = playerName.getBytes("UTF-8");
				buf.putInt(bytes.length);
				buf.put(bytes);
			} catch (UnsupportedEncodingException e) {
				DTLog.warning(e);
			}
		}
				
		buf.rewind();
	}
	protected ConnectMessage(NetworkMessage msg) {
		super(msg);
	}

	//Functions
	private static int len(String playerNames[]) {
		int bytes = 0;
		for (String playerName : playerNames) {
			try {
				bytes += 4;
				bytes += playerName.getBytes("UTF-8").length;
			} catch (UnsupportedEncodingException e) {
				DTLog.warning(e);
			}
		}
		return bytes;
	}
	
	//Getters
	@Override
	public boolean isValid() {
		return super.isValid() && getPlayerCount() > 0;
	}
	
	public int getUDPPort() {
		return buf.getInt(getHeaderSize() + 0);
	}
	public int getPlayerCount() {
		return buf.getInt(getHeaderSize() + 4);
	}
	public String[] getPlayerNames() {
		int count = getPlayerCount();
		int oldpos = buf.position();
		buf.position(getHeaderSize() + 8);
		
		String playerNames[] = new String[count];
		for (int n = 0; n < count; n++) {
			int len = buf.getInt();
			byte bytes[] = new byte[len];
			buf.get(bytes);
			try {
				playerNames[n] = new String(bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				DTLog.warning(e);
			}
		}
		
		buf.position(oldpos);
		return playerNames;
	}
	
	//Setters
	
}
