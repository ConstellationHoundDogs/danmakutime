package nl.weeaboo.dt.renderer;

import nl.weeaboo.game.gl.GLImage;

public class Texture implements ITexture {

	private GLImage image;
	private int hashCode; //Cached
	
	public Texture(GLImage image) {
		this.image = image;
	}
	
	//Functions
	public int hashCode() {
		if (hashCode != 0) return hashCode;
		
		// Hashcode could potentially be 0, but the only problem that would
		// cause is that it would be recalculated every frame instead of
		// being cached.
		return (hashCode = image.hashCode());
	}
	
	public boolean equals(Object o) {
		if (o instanceof Texture) {
			return image == ((Texture)o).image;
		}
		return false;
	}
	
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
