package nl.weeaboo.dt;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import nl.weeaboo.dt.io.HashUtil;
import nl.weeaboo.dt.io.IPersistentStorage;
import nl.weeaboo.dt.io.IPersistentStorageFactory;
import nl.weeaboo.dt.lua.LuaRunState;

public final class GameEnv implements Cloneable {

	private long randomSeed;
	private byte persistentData[];
	private byte resourcesHash[];
	
	public GameEnv(long randomSeed, byte persistentData[], byte resourcesHash[]) {
		this.randomSeed = randomSeed;
		this.persistentData = persistentData.clone();
		this.resourcesHash = resourcesHash.clone();
	}
	
	public static GameEnv fromGame(Game game) throws IOException {
		LuaRunState lrs = game.getLuaRunState();
		long randomSeed = System.nanoTime();
		if (lrs != null) lrs.reseed();
		
		IPersistentStorageFactory storageFactory = game.getPersistentStorageFactory();
		IPersistentStorage ps = storageFactory.createPersistentStorage();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ps.save(bout);
		byte persistentData[] = bout.toByteArray();
		
		byte resourcesHash[] = HashUtil.calculateResourcesHash(game);
		
		return new GameEnv(randomSeed, persistentData, resourcesHash);
	}
	
	public static GameEnv fromInput(InputStream in) throws IOException {		
		DataInputStream din = new DataInputStream(in);
		long randomSeed = din.readLong();
		
		int persistentDataLength = din.readInt();
		byte persistentData[] = new byte[persistentDataLength];
		din.read(persistentData);

		int resourcesHashLength = din.readInt();
		byte resourcesHash[] = new byte[resourcesHashLength];
		din.read(resourcesHash);

		return new GameEnv(randomSeed, persistentData, resourcesHash);
	}
	
	public ByteBuffer toByteBuffer() {
		ByteBuffer buf = ByteBuffer.allocate(8
				+ (4 + getPersistentDataLength())
				+ (4 + getResourcesHashLength()));
		
		buf.putLong(getRandomSeed());

		buf.putInt(getPersistentDataLength());
		buf.put(getPersistentData());
		
		buf.putInt(getResourcesHashLength());
		buf.put(getResourcesHash());
		
		buf.rewind();
		return buf;
	}
	
	@Override
	public GameEnv clone() {
		return new GameEnv(randomSeed, persistentData, resourcesHash);
	}
	
	@Override
	public int hashCode() {
		return (int)randomSeed;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GameEnv) {
			GameEnv env = (GameEnv)obj;
			return randomSeed == env.getRandomSeed()
				&& Arrays.equals(persistentData, env.persistentData)
				&& Arrays.equals(resourcesHash, env.resourcesHash);
		}
		return false;
	}
	
	public long getRandomSeed() {
		return randomSeed;
	}
	public int getPersistentDataLength() {
		return persistentData.length;
	}
	public byte[] getPersistentData() {
		return persistentData.clone();
	}
	public int getResourcesHashLength() {
		return resourcesHash.length;
	}
	public byte[] getResourcesHash() {
		return resourcesHash.clone();
	}
	
}
