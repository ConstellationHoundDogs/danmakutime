package nl.weeaboo.dt.replay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.weeaboo.dt.GameEnv;
import nl.weeaboo.dt.input.IInput;

public interface IReplay extends Iterable<IInput> {

	// === Functions ===========================================================
	public void addFrame(IInput input);
	public void load(InputStream in) throws IOException;
	public void save(OutputStream out) throws IOException;
	
	// === Getters =============================================================
	public GameEnv getStartingState();
	public String getMainFuncName();
	
	// === Setters =============================================================
	
}
