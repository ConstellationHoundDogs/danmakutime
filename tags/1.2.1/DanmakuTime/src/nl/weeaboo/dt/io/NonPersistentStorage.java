package nl.weeaboo.dt.io;

import java.io.IOException;
import java.io.InputStream;

import nl.weeaboo.dt.Game;

public class NonPersistentStorage extends PersistentStorage {
	
	public NonPersistentStorage(Game game) {
		super(game, "persist.temp.xml");
	}
	
	//Functions
	public static NonPersistentStorage fromInputStream(Game game, InputStream in)
		throws IOException
	{
		NonPersistentStorage s = new NonPersistentStorage(game);
		s.load0(in);
		return s;
	}
	
	@Override
	public void load() throws IOException {
		//Do nothing, immutable		
	}
	
	@Override
	public void load(InputStream in) throws IOException {
		//Do nothing, immutable		
	}
	
	private void load0(InputStream in) throws IOException {
		super.load(in);
	}
	
	@Override
	public void save() {
		//Do nothing, immutable		
	}

	//Getters
	
	//Setters
	
}
