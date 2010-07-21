package nl.weeaboo.dt.collision;

import nl.weeaboo.common.Log;

public class ColMatrix implements IColMatrix {

	private boolean matrix[][];
	private int size;
	
	public ColMatrix() {
		matrix = new boolean[0][0];
		size = 0;
	}
	
	//Functions
	@Override
	public ColMatrix clone() {
		try {
			ColMatrix c = (ColMatrix)super.clone();
			c.matrix = new boolean[size][size];
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					c.matrix[x][y] = matrix[x][y];
				}
			}
			c.size = size;
			return c;
		} catch (CloneNotSupportedException e) {
			Log.error(e);
		}
		return null;
	}
	
	@Override
	public int newColType() {
		boolean old[][] = matrix;
		
		size++;
		
		matrix = new boolean[size][size];
		for (int x = 0; x < old.length; x++) {
			for (int y = 0; y < old[x].length; y++) {
				matrix[x][y] = old[x][y];
			}
		}
		
		return size-1;
	}
	
	//Getters
	@Override
	public int getSize() {
		return size;
	}

	@Override
	public boolean isColliding(int t0, int t1) {
		return matrix[t0][t1];
	}
	
	//Setters
	@Override
	public void setColliding(int t0, int t1) {
		setColliding(t0, t1, true);
	}

	@Override
	public void setColliding(int t0, int t1, boolean c) {
		matrix[t0][t1] = c;
	}
	
}
