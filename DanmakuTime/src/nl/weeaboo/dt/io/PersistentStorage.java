package nl.weeaboo.dt.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.weeaboo.common.PreferenceStore;
import nl.weeaboo.common.collections.Typed;
import nl.weeaboo.dt.DTLog;
import nl.weeaboo.dt.Game;

public class PersistentStorage implements IPersistentStorage {
	
	private Game game;
	private String filename;
	private PreferenceStore prefStore;
	
	public PersistentStorage(Game game, String filename) {
		this.game = game;
		this.filename = filename;
		
		prefStore = new PreferenceStore();
	}
	
	//Functions
	@Override
	public void load() throws IOException {
		InputStream in = null;
		try {
			if (game.getFileExists(filename)) {
				in = game.getInputStream(filename);
				load(in); //Replace state with saved state
			} else {
				clear(); //No saved file? Reset to default state
			}
		} finally {
			if (in != null) in.close();
		}		
	}
	
	@Override
	public void load(InputStream in) throws IOException {
		clear();
		prefStore.load(in);
	}
	
	@Override
	public void save() {
		OutputStream out = null;
		try {
			out = game.getOutputStream(filename);
			save(out);
		} catch (IOException ioe) {
			DTLog.warning(ioe);
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException ioe) { }
		}		
	}

	@Override
	public void save(OutputStream out) throws IOException {
		prefStore.save(out);
	}
	
	@Override
	public void clear() {
		prefStore.clear();
	}
	
	@Override
	public Object remove(String key) {
		Typed t = prefStore.removeProperty(key);
		return (t != null ? t.getValue() : null);
	}
	
	//Getters	
	@Override
	public Object get(String key) {
		return prefStore.getProperty(key);
	}
	
	//Setters
	@Override
	public void set(String key, Object val) {
		if (val == null) {
			remove(key);
		} else {
			prefStore.setProperty(key, val);
		}
	}
	
}
