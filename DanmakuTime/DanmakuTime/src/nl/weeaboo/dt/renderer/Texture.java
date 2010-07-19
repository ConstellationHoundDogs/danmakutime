package nl.weeaboo.dt.renderer;

import nl.weeaboo.game.gl.GLImage;

public class Texture implements ITexture {

	private GLImage image;
	
	public Texture(GLImage image) {
		this.image = image;
	}
	
	//Functions

	//Getters
	public GLImage getImage() {
		return image;
	}
	
	@Override
	public int getWidth() {
		return image.getWidth();
	}

	@Override
	public int getHeight() {
		return image.getHeight();
	}
	
	//Setters
	
}
