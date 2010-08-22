package nl.weeaboo.dt.io;

import java.nio.ByteBuffer;

public interface IPersistentStorageFactory {

	/**
	 * Loads the persistent storage saved on this machine.
	 */
	public IPersistentStorage createPersistentStorage();
	
	/**
	 * Creates a new persistent storage object from a byte buffer.
	 */
	public IPersistentStorage createPersistentStorage(ByteBuffer buf);
	
	/**
	 * Creates a new persistent storage object from a byte buffer. The resulting
	 * storage object can't be saved to disk.
	 */
	public IPersistentStorage createNonPersistentStorage(ByteBuffer buf);
	
}
