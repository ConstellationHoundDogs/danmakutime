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
		return input.isKeyDown(keycode);
	}

	@Override
	public boolean isKeyPressed(int keycode) {
		return input.isKeyPressed(keycode);
	}

	@Override
	public void clear() {
		input.clearAll();
	}
	
	//Setters
	
}
