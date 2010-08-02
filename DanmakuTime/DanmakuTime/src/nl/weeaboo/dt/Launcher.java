package nl.weeaboo.dt;

import java.awt.Container;
import java.io.File;
import java.net.InetAddress;
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
	
	private HostConfig hostConfig;
	private JoinConfig joinConfig;
	
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
				+ "\n\t\t-host <externalAddress> <port>\t\t\tHost a networked game"
				+ "\n\t\t-join <address> <port> <localUDPPort>\t\t\tJoin a networked game"
				);
	}
	
	public static void main(String args[]) {
		Launcher launcher = new Launcher();
		
		try {
			launcher.processArgs(args);
		} catch (RuntimeException re) {
			System.err.println(re.toString());
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
				} else if (args[n].startsWith("-host")) {
					hostConfig = new HostConfig(args[++n], Integer.parseInt(args[++n]));
				} else if (args[n].startsWith("-join")) {
					joinConfig = new JoinConfig(args[++n], Integer.parseInt(args[++n]),
							Integer.parseInt(args[++n]));
				} else if (!args[n].startsWith("-")) {
					setRootURI(new File(args[n]).toURI());
				} else {
					throw new IllegalArgumentException("Unrecognized commandline option");
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
			if (hostConfig != null) {
				game.hostNetGame(hostConfig.tcpPort);
				InetAddress targetAddr = InetAddress.getLocalHost();
				if (!hostConfig.externalIP.equalsIgnoreCase("localhost")) {
					targetAddr = InetAddress.getByName(hostConfig.externalIP);
				}
				game.joinNetGame(targetAddr, hostConfig.tcpPort, hostConfig.tcpPort);
			} else if (joinConfig != null) {
				InetAddress targetAddr = InetAddress.getLocalHost();
				if (!joinConfig.targetIP.equalsIgnoreCase("localhost")) {
					targetAddr = InetAddress.getByName(joinConfig.targetIP);
				}
				game.joinNetGame(targetAddr, joinConfig.targetTCPPort,
						joinConfig.localUDPPort);
			}			
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
		
	//Inner Classes
	private static class HostConfig {
		public final String externalIP;
		public final int tcpPort;
		
		public HostConfig(String externalIP, int tcpPort) {
			this.externalIP = externalIP;
			this.tcpPort = tcpPort;
		}
	}

	private static class JoinConfig {
		public final String targetIP;
		public final int targetTCPPort;
		public final int localUDPPort;
		
		public JoinConfig(String targetIP, int targetTCPPort, int localUDPPort) {
			this.targetIP = targetIP;
			this.targetTCPPort = targetTCPPort;
			this.localUDPPort = localUDPPort;
		}
	}
	
}
