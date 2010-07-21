package nl.weeaboo.dt;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import nl.weeaboo.game.GameBase;
import nl.weeaboo.game.GameFrameBase;

public class GameFrame extends GameFrameBase {

	public GameFrame(String title, int w, int h, int fps,
			boolean startFullscreen, boolean trueFullscreen, boolean debugGL)
	{
		super(title, w, h, fps, startFullscreen, trueFullscreen, debugGL);
		
		setFullScreenSize(new Dimension(w, h));
	}
	
	//Functions
	public void start(GameBase gb, Container c) {	
		InputStream in = null;
		try {
			in = gb.getResourceManager().getInputStream("icon.png");
			BufferedImage icon = ImageIO.read(in);
			if (icon != null) setIcon(icon, true);
		} catch (IOException ioe) {
			DTLog.warning(ioe);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				DTLog.warning(e);
			}
		}		

		super.start(gb, c);
	}
	
	//Getters
	
	//Setters
	
}
