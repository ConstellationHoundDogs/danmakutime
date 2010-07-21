package nl.weeaboo.dt.collision;

public interface IColField {

	// === Functions ===========================================================
	public void add(IColNode c);
	public void remove(IColNode c);
	public void processCollisions();
	
	// === Getters =============================================================
	
	// === Setters =============================================================
	public void setColMatrix(IColMatrix m);
	
}
