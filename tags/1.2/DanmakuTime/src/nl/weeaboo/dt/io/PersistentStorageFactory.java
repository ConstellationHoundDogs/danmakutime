package nl.weeaboo.dt.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import nl.weeaboo.common.io.ByteBufferInputStream;
import nl.weeaboo.dt.DTLog;
import nl.weeaboo.dt.Game;

public class PersistentStorageFactory implements IPersistentStorageFactory {

	private Game game;
	private String filename = "save/persist.xml";
	
	public PersistentStorageFactory(Game g) {
		game = g;
	}
	
	@Override
	public IPersistentStorage createPersistentStorage() {
		IPersistentStorage storage = new PersistentStorage(game, filename);		
		try {
			storage.load();
		} catch (IOException e) {
			DTLog.warning(e);
		}
		return storage;
	}

	@Override
	public IPersistentStorage createPersistentStorage(ByteBuffer buf) {
		int oldpos = buf.position();
		ByteBufferInputStream bin = new ByteBufferInputStream(buf);
		try {
			return createPersistentStorage(bin);
		} finally {
			bin.close();
			buf.position(oldpos);
		}
	}

	protected IPersistentStorage createPersistentStorage(InputStream in) {
		PersistentStorage ps = new PersistentStorage(game, filename);
		try {
			ps.load(in);
		} catch (IOException e) {
			DTLog.warning(e);
		}
		return ps;
	}
	
	@Override
	public IPersistentStorage createNonPersistentStorage(ByteBuffer buf) {
		int oldpos = buf.position();
		ByteBufferInputStream bin = new ByteBufferInputStream(buf);
		try {
			return createNonPersistentStorage(bin);
		} finally {
			bin.close();
			buf.position(oldpos);
		}
	}
	
	protected IPersistentStorage createNonPersistentStorage(InputStream in) {
		try {
			return NonPersistentStorage.fromInputStream(game, in);
		} catch (IOException e) {
			DTLog.warning(e);
		}
		return new NonPersistentStorage(game);
	}

}
