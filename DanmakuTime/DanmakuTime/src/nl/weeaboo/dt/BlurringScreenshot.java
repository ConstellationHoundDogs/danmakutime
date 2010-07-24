package nl.weeaboo.dt;

import java.awt.Dimension;

import nl.weeaboo.dt.renderer.Blur;

public class BlurringScreenshot extends DelayedScreenshot {

	public BlurringScreenshot(Game game, double x, double y, double w, double h) {
		super(game, x, y, w, h);
	}

	//Functions
	
	//Getters
	
	//Setters
	@Override
	public void set(int argb[], int w, int h) {
		Dimension size = new Dimension(w, h);
		argb = Blur.process(argb, size, 4, true, true);
		super.set(argb, size.width, size.height);
	}
	
}
