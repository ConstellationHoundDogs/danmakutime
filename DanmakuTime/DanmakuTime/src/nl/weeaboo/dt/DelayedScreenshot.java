package nl.weeaboo.dt;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.nio.IntBuffer;

import nl.weeaboo.dt.renderer.ITexture;
import nl.weeaboo.dt.renderer.Texture;
import nl.weeaboo.game.gl.GLImage;
import nl.weeaboo.game.gl.GLTexUtil;

public class DelayedScreenshot {

	//Request
	protected Game game;
	protected Rectangle2D rect;
	
	//Screenshot
	private int argb[];
	private int width;
	private int height;

	//Cached
	private ITexture tex;
	
	public DelayedScreenshot(Game game, double x, double y, double w, double h) {
		this.game = game;
		this.rect = new Rectangle2D.Double(x, y, w, h);
	}
	
	//Functions
	public static int[] copyRect(int argb[], int w, int h, Rectangle rect) {
		int dst[] = new int[rect.width * rect.height];
		GLTexUtil.copyImageIntoData(argb, 0, w, dst, 0, rect.width,
				rect.x, rect.y, rect.width, rect.height);		
		return dst;
	}
	
	//Getters
	public boolean isAvailable() {
		return argb != null;
	}
	
	public Rectangle2D getCaptureRect() {
		return rect.getBounds2D();
	}
	
	public int[] getARGB() {
		return argb;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public ITexture asTexture() {
		if (tex == null && isAvailable()) {
			GLImage image = game.addGeneratedImage(IntBuffer.wrap(getARGB()),
				getWidth(), getHeight(), true, false);
			tex = new Texture(image);
		}
		return tex;
	}
	
	//Setters
	public void set(int argb[], int w, int h) {
		this.argb = argb;
		this.width = w;
		this.height = h;
	}
	
}
