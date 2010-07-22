package nl.weeaboo.dt.collision;

public interface IColMatrix extends Cloneable {

	// === Functions ===========================================================	
	public IColMatrix clone();
	
	public int newColType();
	public void setColliding(int t0, int t1);
	public void setColliding(int t0, int t1, boolean c);
	public void setColliding2(int t0, int t1);
	public void setColliding2(int t0, int t1, boolean c);
	
	// === Getters =============================================================
	public boolean isColliding(int t0, int t1);
	public int getSize();
	
	// === Setters =============================================================
	
}
