package nl.weeaboo.dt;

import java.awt.Container;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JApplet;

import nl.weeaboo.common.components.GuiUtil;
import nl.weeaboo.common.components.Skin;
import nl.weeaboo.common.io.FileUtil;
import nl.weeaboo.common.jnlp.JnlpUtil;
import nl.weeaboo.game.ResourceManager;

public class Launcher {

	private URI rootURI;
	private boolean debug;
	
	public Launcher() {
		try {
			rootURI = new File("").toURI();
		} catch (SecurityException se) {
			URL url = JnlpUtil.getCodebase();
			if (url != null) {
				try {
					rootURI = url.toURI();
				} catch (URISyntaxException e) {
					DTLog.error(e);
				}
			} else {
				DTLog.error("No codebase");
			}
		}
		
		debug = false;
	}
	
	//Functions
	protected static void printUsage() {
		System.err.println("Usage: java -jar DanmakuTime.jar [options] <sourceFolder>"
				+ "\n\toptions:"
				+ "\n\t\t-debug\t\t\tRun program in debug mode"
				+ "\n\t\t-script <filename>\t\t\tChange the first script that gets loaded"
				);
	}
	
	public static void main(String args[]) {
		Launcher launcher = new Launcher();
		
		try {
			launcher.processArgs(args);
		} catch (RuntimeException re) {
			printUsage();
			return;
		}
		
		//LauncherUtil.generateResourceZip(new File(launcher.getRootURI()));		
		launcher.startFrame();
	}
	
	public void processArgs(String args[]) {
		for (int n = 0; n < args.length; n++) {
			try {
				if (args[n].startsWith("-debug")) {
					setDebug(true);
				} else if (!args[n].startsWith("-")) {
					setRootURI(new File(args[n]).toURI());
				} else {
					throw new IllegalArgumentException("Invalid arg: " + args[n]);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid arg: " + args[n] + " " + e);				
			}
		}
	}
	
	public Game startFrame() {
		if (rootURI == null) {
			throw new IllegalArgumentException("No root URI specified");
		}
		
		String gameId = rootURI.toString();
		gameId = gameId.substring(gameId.lastIndexOf('/')+1);
		return startGame(gameId, null);
	}
	public Game startApplet(String gameId, JApplet applet) {		
		return startGame(gameId, applet);
	}
	
	protected Game startGame(String gameId, Container container) {
		gameId = "DanmakuTime/" + FileUtil.toValidASCIIFilename(gameId);
		
		try {
			GuiUtil.setSkin(Skin.NATIVE);
		} catch (Exception e) {
			DTLog.warning(e);
		}
		
		boolean inited = false;
		try {
			ResourceManager rm = new ResourceManager(rootURI, "res.zip");
			rm.open(gameId, container);
			
			Config config = new Config();
			Game.loadConfig(config, rm);
			config.setProperty("debug", debug);
			
			GameFrame frame = new GameFrame(config.game.getGameName(),
					config.graphics.getWidth(), config.graphics.getHeight(),
					config.graphics.getFPS(), config.graphics.getStartFullscreen(),
					config.getUseTrueFullscreen(), config.isDebug());
			
			Game game = new Game(config, rm, frame);
			game.start(gameId, container);
			
			inited = true;
			return game;
		} catch (Throwable t) {
			DTLog.showError(t);
		} finally {
			if (!inited) {
				DTLog.showError("Fatal error during init");
			}
		}
		
		return null;
	}
	
	//Getters
	public URI getRootURI() { return rootURI; }
	public boolean isDebug() { return debug; }
	
	//Setters	
	public void setRootURI(URI rootURI) { this.rootURI = rootURI; }
	public void setDebug(boolean debug) { this.debug = debug; }
	
}
