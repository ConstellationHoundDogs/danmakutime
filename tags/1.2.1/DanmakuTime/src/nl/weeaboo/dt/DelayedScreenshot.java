package nl.weeaboo.dt;

import nl.weeaboo.dt.renderer.ITexture;
import nl.weeaboo.dt.renderer.Texture;
import nl.weeaboo.game.gl.GLImage;
import nl.weeaboo.game.gl.Screenshot;

public class DelayedScreenshot extends Screenshot {

	//Request
	protected Game game;

	//Cached
	private ITexture tex;
	
	public DelayedScreenshot(Game game, double x, double y, double w, double h) {
		super((float)x, (float)y, (float)w, (float)h);
		
		this.game = game;
	}
	
	//Functions
	
	//Getters
	public ITexture asTexture() {
		if (tex == null && isAvailable()) {
			GLImage image = game.addGeneratedImage(getARGB(),
				getWidth(), getHeight(), true, false);
			tex = new Texture(image);
		}
		return tex;
	}
	
	//Setters
	
}
