package nl.weeaboo.dt.input;

import nl.weeaboo.game.input.UserInput;

public class Input implements IInput {

	private UserInput input;
	
	public Input(UserInput in) {
		input = in;
	}
	
	//Functions
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
	
	//Setters
	
}
