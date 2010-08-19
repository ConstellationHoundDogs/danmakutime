package nl.weeaboo.dt;

import java.awt.Dimension;

import nl.weeaboo.game.image.Blur;

public class BlurringScreenshot extends DelayedScreenshot {

	private int magnitude;
	
	public BlurringScreenshot(Game game, double x, double y, double w, double h, int magnitude) {
		super(game, x, y, w, h);
		
		this.magnitude = magnitude;
	}

	//Functions
	
	//Getters
	
	//Setters
	@Override
	public void set(int argb[], int w, int h) {
		Dimension size = new Dimension(w, h);
		argb = Blur.process(argb, size, magnitude, true, true);
		super.set(argb, size.width, size.height);
	}
	
}
