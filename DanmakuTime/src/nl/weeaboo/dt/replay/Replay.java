package nl.weeaboo.dt.replay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import nl.weeaboo.common.SystemUtil;
import nl.weeaboo.dt.GameEnv;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.input.Input;

public class Replay implements IReplay {

	private GameEnv genv;
	private String mainFuncName;
	private Collection<IInput> frames;
	
	private Replay() {
		frames = new ArrayList<IInput>(1024);
	}
	public Replay(GameEnv genv, String mainFuncName) {
		this();
		
		this.genv = genv.clone();
		this.mainFuncName = mainFuncName;		
	}
	
	//Functions
	public static Replay fromInputStream(InputStream in) throws IOException {
		Replay replay = new Replay();
		replay.load(in);
		return replay;
	}
	
	@Override
	public void addFrame(IInput input) {
		frames.add(input);
	}

	@Override
	public Iterator<IInput> iterator() {
		return Arrays.asList(frames.toArray(new IInput[frames.size()])).iterator();
	}
	
	@Override
	public void load(InputStream in0) throws IOException {
		DataInputStream din = new DataInputStream(new InflaterInputStream(in0));
		genv = GameEnv.fromInput(din);		
		mainFuncName = din.readUTF();
		
		//Read frames		
		int numFrames = din.readInt();
		for (int n = 0; n < numFrames; n++) {
			addFrame(loadFrame(din));
		}
	}

	protected IInput loadFrame(DataInputStream din) throws IOException {
		IInput in = new Input();
		
		int heldCount = din.readShort();
		for (int h = 0; h < heldCount; h++) {
			in.setKeyHeld(din.readShort());
		}
		
		int pressedCount = din.readShort();
		for (int p = 0; p < pressedCount; p++) {
			in.setKeyPressed(din.readShort());
		}
		
		return in;
	}
	
	@Override
	public void save(OutputStream out0) throws IOException {
		DeflaterOutputStream out = new DeflaterOutputStream(out0);
		out.write(SystemUtil.bufferToArray(genv.toByteBuffer()));
		
		DataOutputStream dout = new DataOutputStream(out);
		dout.writeUTF(mainFuncName);
		
		//Write frames
		dout.writeInt(frames.size());
		for (IInput input : frames) {
			saveFrame(dout, input);
		}
		
		out.finish();
	}
	
	protected void saveFrame(DataOutputStream dout, IInput in) throws IOException {
		int held[] = in.getKeysHeld();
		int pressed[] = in.getKeysPressed();
		
		if (held.length > 0x7FFF) throw new IllegalArgumentException("held array too large (max=32767): " + held.length);
		if (pressed.length > 0x7FFF) throw new IllegalArgumentException("pressed array too large (max=32767): " + pressed.length);
		
		dout.writeShort(held.length);
		for (int key : held) {
			if (key > 0x7FFF) throw new IllegalArgumentException("key code too large (max=32767): " + key);
			dout.writeShort(key);
		}
		
		dout.writeShort(pressed.length);
		for (int key : pressed) {
			if (key > 0x7FFF) throw new IllegalArgumentException("key code too large (max=32767): " + key);
			dout.writeShort(key);
		}
	}
	
	//Getters
	@Override
	public GameEnv getStartingState() {
		return genv.clone();
	}
	
	@Override
	public String getMainFuncName() {
		return mainFuncName;
	}
	
	//Setters
	
}
