package nl.weeaboo.dt.collision;

import java.util.Arrays;
import java.util.Collection;

import nl.weeaboo.dt.object.IDrawable;

public class ColGrid {

	private IColMatrix colMatrix;
	private GridCell grid[][][];
	private GridCell fallbackCells[];
	private int gridMagnitude;
	private double gridSize;
	private double maxObjectRadius;
	private int cellsW;
	private int cellsH;
	
	public ColGrid(IColMatrix m, int w, int h) {
		colMatrix = m;
		
		gridMagnitude = 5;
		gridSize = 1 << gridMagnitude;
		maxObjectRadius = .5 * gridSize;
		
		cellsW = (int)Math.ceil(w / (double)gridMagnitude);
		cellsH = (int)Math.ceil(h / (double)gridMagnitude);
		grid = new GridCell[0][cellsW][cellsH];
		
		fallbackCells = new GridCell[0];
	}
	
	//Functions
	public void processCollisions(IColNode node) {
		IDrawable owner0 = node.getHost().getOwner();
		int t0 = node.getType();
		double r0 = node.getBoundingCircleRadius();		
		if (owner0 == null || owner0.isDestroyed()) {
			return;
		}
		
		double centerX = node.getCenterX();
		double centerY = node.getCenterY();
		int minX = Math.max(0, toGridCoordX(centerX - r0));
		int maxX = Math.min(cellsW-1, toGridCoordX(centerX + r0));
		int minY = Math.max(0, toGridCoordY(centerY - r0));
		int maxY = Math.min(cellsH-1, toGridCoordY(centerY + r0));

		int cellX = toGridCoordX(centerX);
		int cellY = toGridCoordY(centerY);
		
		int t1;
		if (r0 > maxObjectRadius
				|| cellX < 0 || cellY < 0 || cellX >= cellsW || cellY >= cellsH)
		{
			//If object is large or otherwise not in a grid cell
			
			//Large <-> Large
			for (t1 = t0; t1 < grid.length; t1++) {
				boolean atob = colMatrix.isColliding(t0, t1);
				boolean btoa = colMatrix.isColliding(t1, t0);
				if (!atob && !btoa) continue;

				GridCell cell = fallbackCells[t1];
				if (cell == null) continue;
				
				IColNode elems[] = cell.elems;
				for (int n = 0; n < cell.size; n++) {
					ColUtil.collide(node, elems[n], atob, btoa);

					//Early escape when we are dead
					if (owner0.isDestroyed()) return;
				}
			}
			
			//Large <-> Small
			t1 = 0;			
		} else {
			//Small <-> Small
			t1 = t0;
		}
		
		//Early escape when we are dead
		if (owner0.isDestroyed()) return;

		//Check collisions with grid
		while (t1 < grid.length) {
			boolean atob = colMatrix.isColliding(t0, t1);
			boolean btoa = colMatrix.isColliding(t1, t0);
			if (!atob && !btoa) {
				t1++;
				continue;
			}

			for (int cy = minY; cy <= maxY; cy++) {
				for (int cx = minX; cx <= maxX; cx++) {
					GridCell cell = grid[t1][cx][cy];
					if (cell == null) continue;
					
					IColNode elems[] = cell.elems;
					for (int n = 0; n < cell.size; n++) {
						ColUtil.collide(node, elems[n], atob, btoa);
						
						//Early escape when we are dead
						if (owner0.isDestroyed()) return;
					}
				}
			}
			
			t1++;
		}
	}
	
	private final int toGridCoordX(double x) {
		return ((int)(x + .5)) >> gridMagnitude;
	}
	private final int toGridCoordY(double y) {
		return ((int)(y + .5)) >> gridMagnitude;
	}
	
	//Getters
	
	//Setters
	public void setNodes(Collection<? extends Collection<? extends IColNode>> nodes) {
		if (grid == null || grid.length != nodes.size()) {
			grid = new GridCell[nodes.size()][cellsW][cellsH];
			fallbackCells = new GridCell[grid.length];
		} else {		
			for (int t = 0; t < grid.length; t++) {				
				for (int x = 0; x < cellsW; x++) {
					for (int y = 0; y < cellsH; y++) {
						if (grid[t][x][y] != null) {
							grid[t][x][y].clear();
						}
					}
				}
				if (fallbackCells[t] != null) {
					fallbackCells[t].clear();
				}
			}
		}
		
		int t = 0;
		for (Collection<? extends IColNode> list : nodes) {
			for (IColNode node : list) {
				int cellX = toGridCoordX(node.getCenterX());
				int cellY = toGridCoordY(node.getCenterY());
				
				GridCell cell;
				if (node.getBoundingCircleRadius() > maxObjectRadius
						|| cellX < 0 || cellY < 0 || cellX >= cellsW || cellY >= cellsH)
				{
					cell = fallbackCells[t];
					if (cell == null) fallbackCells[t] = cell = new GridCell();
				} else {
					cell = grid[t][cellX][cellY];
					if (cell == null) grid[t][cellX][cellY] = cell = new GridCell();
				}
				cell.add(node);
			}
			t++;
		}
	}
	
	//Inner Classes
	private static class GridCell {

		IColNode elems[];
		int size;
		
		GridCell() {
			elems = new IColNode[8];
		}
		
		public void add(IColNode node) {
			if (size >= elems.length) {
				elems = Arrays.copyOf(elems, elems.length<<1);
			}
			elems[size++] = node;
		}
		
		public void clear() {
			Arrays.fill(elems, 0, size, null);
			size = 0;
		}
		
	}
	
}
