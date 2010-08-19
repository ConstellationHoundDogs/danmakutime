package nl.weeaboo.dt;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;

import nl.weeaboo.common.GraphicsUtil;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.game.gl.GLManager;
import nl.weeaboo.game.gl.GLVideoCapture;

public class GameVideoCapture {

	private Game game;
	private GLVideoCapture videoCapture;

	public GameVideoCapture(Game game) {
		this.game = game;
	}
	
	//Functions
	public void start(String filename) throws IOException {
		stop();
		
		Config config = game.getConfig();
		
		int rw = game.getWidth();
		int rh = game.getHeight();
		int scw = config.capture.getWidth();
		int sch = config.capture.getHeight();
		
		if (scw <= 0) scw = rw;
		if (sch <= 0) sch = rh;
		
		Dimension d = GraphicsUtil.getProportionalScale(rw, rh, scw, sch);
		
		int captureFPS = config.graphics.getFPS();
		//while (captureFPS > 30) captureFPS /= 2;
		
		videoCapture = new GLVideoCapture(game, filename, config.capture.getX264CRF(),
				captureFPS, rw, rh, d.width, d.height, true);
		
		try {
			videoCapture.start();
		} catch (IOException ioe) {
			videoCapture = null;
			throw ioe;
		}
	}
	
	public void stop() {
		if (videoCapture != null) {
			videoCapture.stop();
			videoCapture = null;
		}		
	}
	
	public void update(IInput ii) {
		if (ii.consumeKey(KeyEvent.VK_F8)) {
			//Video capture activation key
			if (isRecordingVideo()) {
				stop();
			} else {
				try {
					start("capture.mkv");
					DTLog.message("Starting video recording");
				} catch (IOException e) {
					DTLog.showError(e);
				}
			}
		}		
	}
	
	public void updateGL(GLManager glm) {
		if (videoCapture != null) {
			try {
				videoCapture.update(glm, game.getRealWidth(), game.getRealHeight());
			} catch (IOException e) {
				DTLog.showError(e);
				videoCapture = null;
			}
		}
	}
	
	//Getters
	public boolean isRecordingVideo() {
		return videoCapture != null;
	}
	
	//Setters
	
}
