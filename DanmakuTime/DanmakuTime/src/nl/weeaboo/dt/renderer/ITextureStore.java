package nl.weeaboo.dt.renderer;

public interface ITextureStore {

	/**
	 * @return The texture matching <code>id</code>
	 */
	public ITexture get(String id);
	
}
