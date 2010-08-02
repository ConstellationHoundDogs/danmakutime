package nl.weeaboo.dt.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IPersistentStorage {

	// === Functions ===========================================================
	public void load() throws IOException;
	public void load(InputStream in) throws IOException;
	public void save() throws IOException;
	public void save(OutputStream out) throws IOException;
	public void clear();
	public Object remove(String key);
	
	// === Getters =============================================================
	public Object get(String key);
	
	// === Setters =============================================================
	public void set(String key, Object val);
	
}
