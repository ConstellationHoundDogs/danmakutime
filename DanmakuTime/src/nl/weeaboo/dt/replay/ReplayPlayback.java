package nl.weeaboo.dt.replay;

import java.nio.ByteBuffer;
import java.util.Iterator;

import nl.weeaboo.dt.GameEnv;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.io.IPersistentStorage;
import nl.weeaboo.dt.io.IPersistentStorageFactory;

public class ReplayPlayback implements IReplayPlayback {

	private IPersistentStorageFactory storageFactory;
	
	private IReplay replay;
	private Iterator<IInput> itr;
	private IPersistentStorage storage;
	
	public ReplayPlayback(IPersistentStorageFactory sf) {
		storageFactory = sf;
	}
	
	//Functions
	@Override
	public void start(IReplay r) {
		replay = r;
		itr = replay.iterator();

		ByteBuffer buf = ByteBuffer.wrap(getGameEnv().getPersistentData());
		storage = storageFactory.createNonPersistentStorage(buf);
	}

	@Override
	public IInput nextFrame() {
		return itr.next();
	}

	//Getters
	@Override
	public boolean hasNextFrame() {
		return itr != null && itr.hasNext();
	}

	@Override
	public GameEnv getGameEnv() {
		return replay.getStartingState();
	}

	@Override
	public String getMainFuncName() {
		return replay.getMainFuncName();
	}

	@Override
	public IPersistentStorage getStorage() {
		return storage;
	}
	
	//Setters
	
}
