package nl.weeaboo.dt.renderer;

import nl.weeaboo.game.gl.GLImage;
import nl.weeaboo.game.gl.GLImageStore;

public class TextureStore implements ITextureStore {

	private GLImageStore imageStore;
	
	public TextureStore(GLImageStore is) {
		imageStore = is;
	}
	
	//Functions
	
	//Getters
	@Override
	public ITexture get(String id) {
		GLImage image = imageStore.getImage(id, true);
		return (image != null ? new Texture(image) : null);
	}
	
	//Setters
	
}
