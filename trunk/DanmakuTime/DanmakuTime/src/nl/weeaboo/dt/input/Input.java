package nl.weeaboo.dt.input;

import nl.weeaboo.dt.DTLog;
import nl.weeaboo.game.input.UserInput;

public class Input implements IInput {

	private UserInput input;
	
	public Input() {
		this(new UserInput());
	}
	public Input(UserInput in) {
		input = in;
	}
	
	//Functions
	public IInput clone() {
		try {
			Input i = (Input) super.clone();
			i.input = input.copy();
			return i;
		} catch (CloneNotSupportedException e) {
			DTLog.error(e);
			return null;
		}
	}
	
	@Override
	public boolean consumeKey(int keycode) {
		return input.consumeKey(keycode);
	}

	//Getters
	@Override
	public boolean isKeyHeld(int keycode) {
		return input.isKeyHeld(keycode);
	}

	@Override
	public boolean isKeyPressed(int keycode) {
		return input.isKeyPressed(keycode);
	}
	
	@Override
	public int[] getKeysPressed() {
		return input.getKeysPressed();
	}
	
	@Override
	public int[] getKeysHeld() {
		return input.getKeysHeld();
	}

	@Override
	public void clear() {
		input.clearAll();
	}
	
	//Setters
	@Override
	public void setKeyPressed(int keycode) {
		if (!isKeyPressed(keycode)) {
			input.onKeyPressed(keycode);
		}
	}
	
	@Override
	public void setKeyHeld(int keycode) {
		if (!isKeyHeld(keycode)) {
			input.onKeyPressed(keycode);
			input.consumeKey(keycode);
		}
	}
	@Override
	public void setKeyReleased(int keycode) {
		input.onKeyReleased(keycode);
	}
	
}
