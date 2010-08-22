package nl.weeaboo.dt.replay;

import nl.weeaboo.dt.GameEnv;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.io.IPersistentStorage;

public interface IReplayPlayback {

	// === Functions ===========================================================
	public void start(IReplay r);
	public IInput nextFrame();
	
	// === Getters =============================================================
	public GameEnv getGameEnv();
	public String getMainFuncName();
	public boolean hasNextFrame();
	public IPersistentStorage getStorage();
	
	// === Setters =============================================================
	
}
