package nl.weeaboo.dt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.jnlp.PersistenceService;
import javax.swing.JApplet;

import nl.weeaboo.common.SystemUtil;
import nl.weeaboo.common.io.FileUtil;
import nl.weeaboo.common.jnlp.JnlpUtil;
import nl.weeaboo.dt.lua.LuaException;
import nl.weeaboo.dt.lua.link.LuaLink;

@SuppressWarnings("serial")
public class AppletLauncher extends JApplet {
	
	private Game game;
	
	public AppletLauncher() {
	}
	
	//Functions
	
	@Override
	public void init() {
		stop();
	}

	@Override
	public void start() {		
		String gameId = getParameter("gameId");
		if (gameId == null) {			
			DTLog.showError("No gameId param passed to the applet -- using default Game ID.");
			gameId = "APPLET";
		}
		
		String argsString = getParameter("args");
		String args[] = new String[0];
		if (argsString != null) {
			args = SystemUtil.parseCommandLine(argsString);
		}
		
		PersistenceService ps = JnlpUtil.getPersistenceService();
		if (ps != null) {			
			try {
				if (ps.getNames(getCodeBase()).length == 0) {
					// If there are no files saved yet:
					// Reserve space for future files, the only way to request
					// space is to create a file that's too large for the
					// current storage max.
					// There should really be a proper API for this.

					JnlpUtil.deleteFile(ps, getCodeBase(), "padding");
					ps.create(JnlpUtil.makeURL(getCodeBase(), "padding"), 20<<20);
					JnlpUtil.deleteFile(ps, getCodeBase(), "padding");
				}
			} catch (IOException e) {
				DTLog.warning("Error reserving JNLP persistent storage :: " + e);				
			}
		}
		
		final Launcher launcher = new Launcher();
		try {
			launcher.processArgs(args);
			launcher.setRootURI(getCodeBase().toURI());

			final String gid = gameId;
			Runnable r = new Runnable() {
				public void run() {
					game = launcher.startApplet(gid, AppletLauncher.this);
				}
			};
			
			Thread t = new Thread(r);
			t.start();
		} catch (URISyntaxException e) {
			DTLog.showError(e);
		}
		
	}

	@Override
	public void stop() {
		if (game != null) {
			game.dispose();
			game = null;
		}
	}

	@Override
	public void destroy() {	
		stop();
	}
	
	//Getters
	public String[] getScriptNames() {
		synchronized (game) {
			return game.getFolderContents("script").toArray(new String[0]);
		}
	}
	public String getScript(String name) {
		InputStream in = null;
		try {
			synchronized (game) {
				in = game.getInputStream(name);
				return FileUtil.read(in);
			}
		} catch (IOException ioe) {
			DTLog.warning(ioe);
			return ioe.toString();
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) { }
		}
	}
	
	//Setters
	public void setScript(String script) {
		synchronized (game) {
			LuaLink mainThread = game.restart("main");
			try {
				game.addRuntimeScript("_applet_.lua", script);
				mainThread.call(false, "appletMain");
			} catch (LuaException e) {
				DTLog.error(e);
			} catch (IOException e) {
				DTLog.error(e);
			}
		}		
	}
	
}
