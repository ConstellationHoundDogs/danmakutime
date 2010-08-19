package nl.weeaboo.dt.netplay;

public final class ConnectResponseMessage extends NetworkMessage {

	public ConnectResponseMessage(int playerIds[]) {
		super(Type.CONNECT_RESPONSE, 4 + playerIds.length * 4);
		
		buf.putInt(playerIds.length);
		for (int id : playerIds) {
			buf.putInt(id);
		}
	
		buf.rewind();
	}
	protected ConnectResponseMessage(NetworkMessage msg) {
		super(msg);
	}

	//Functions
	
	//Getters
	@Override
	public boolean isValid() {
		return super.isValid() && getPlayerCount() >= 0;
	}
	
	public int getPlayerCount() {
		return buf.getInt(getHeaderSize() + 0);
	}
	public int getPlayerId(int index) {
		return buf.getInt(getHeaderSize() + 4 + 4 * index);
	}

	//Setters
	
}
