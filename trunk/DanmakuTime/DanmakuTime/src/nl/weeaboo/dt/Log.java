package nl.weeaboo.dt;

import java.awt.Color;
import java.io.OutputStream;
import java.io.PrintStream;

import nl.weeaboo.common.io.SplitOutputStream;

/**
 * Application-wide logger.
 */
public class Log extends nl.weeaboo.game.Log {

	private static Log instance;
	
	private boolean debug;
	private Notifier notifier;
	private OutputStream errFileOut;
	private Color messageColor;
		
	public Log() {
		messageColor = new Color(0xFFFFFF);
	}
	
	//Functions
	public static synchronized Log getInstance() {
		if (instance == null) instance = new Log();
		return instance;
	}
	public static synchronized void setInstance(Log log) {
		if (log == null) {
			throw new IllegalArgumentException("log == null");
		}
		instance = log;
	}

	public synchronized void start(boolean d, Notifier n, OutputStream e) {
		debug = d;
		notifier = n;		
		errFileOut = e;
		
		if (errFileOut != null) {
			err = new PrintStream(new SplitOutputStream(System.err, errFileOut));
		} else {
			err = System.err;
		}
		
		Log.setInstance(this);
	}
	
	protected void finalize() throws Throwable {
		try {
			errFileOut.close();
		} finally {
			super.finalize();
		}		
	}
	
	//Error
	public static void error(String s) {
		getInstance().onError(s);
	}
	public static void error(Throwable t) {
		getInstance().onError(t);
	}
	
	public void onError(String s) {
		Notifier n = notifier;
		if (n != null) n.addMessage(null, s, new Color(0xee4040), 5f);
				
		super.onError(s);
	}
	public void onError(Throwable t) {
		Notifier n = notifier;
		if (n != null) n.addMessage(null, t.toString(), new Color(0xee4040), 5f);

		super.onError(t);
	}

	//Warning
	public static void warning(String s) {
		getInstance().onWarning(s);
	}
	public static void warning(Throwable t) {
		getInstance().onWarning(t);
	}

	public void onWarning(String s) {
		Notifier n = notifier;
		if (n != null) n.addMessage(null, s, new Color(0xfdae04), 5f);

		super.onWarning(s);
	}
	public void onWarning(Throwable t) {
		Notifier n = notifier;
		if (n != null) n.addMessage(null, t.toString(), new Color(0xfdae04), 5f);

		super.onWarning(t);
	}

	//Message
	
	/**
	 * Show a non-error message to the user.
	 */
	public static void message(String s) {
		getInstance().onMessage(s);
	}
	public void onMessage(String s) {
		Notifier n = notifier;
		if (n != null) n.addMessage(null, s, messageColor, 1f);		
	}
	
	//Verbose
	
	/**
	 * Debug messages are disabled when <code>{@link Game#debug} == false</code>
	 */
	public static void debug(String s) {
		getInstance().onVerbose(s);
	}
	public void onVerbose(String s) {
		if (debug) {
			Notifier n = notifier;
			if (n != null) n.addMessage(null, s, new Color(0x59d34f), 1f);
	
			super.onVerbose(s);
		}
	}
	
	//Show Error
	public static void showError(String s) {
		getInstance().onShowError(s);
	}
	public static void showError(Throwable t) {
		getInstance().onShowError(t);
	}
	
	//Getters
	public Color getMessageColor() {
		return messageColor;
	}
	
}
