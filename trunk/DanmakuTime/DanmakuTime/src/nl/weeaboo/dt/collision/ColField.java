package nl.weeaboo.dt.collision;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ColField implements IColField {

	protected IColMatrix colMatrix;
	protected Rectangle bounds;
	protected List<List<IColNode>> nodes;
	
	public ColField(int x, int y, int w, int h) {
		bounds = new Rectangle(x, y, w, h);
	}
	
	//Functions	
	@Override
	public void add(IColNode c) {
		Collection<IColNode> list = nodes.get(c.getType());
		list.add(c);
	}

	@Override
	public void remove(IColNode c) {
		Collection<IColNode> list = nodes.get(c.getType());
		list.remove(c);
	}
	
	@Override
	public void processCollisions() {
		if (colMatrix == null || colMatrix.getSize() == 0) {
			return;
		}
		
		IColNode arr[][] = getNodesAsArray();
		
		for (int t0 = 0; t0 < arr.length; t0++) {
			for (int a = 0; a < arr[t0].length; a++) {
				for (int t1 = t0; t1 < arr.length; t1++) {
					if (!colMatrix.isColliding(t0, t1) && !colMatrix.isColliding(t1, t0)) {
						continue;
					}
				
					//System.out.println(arr[t0].length + " " + arr[t1].length);
					
					for (int b = 0; b < arr[t1].length; b++) {
						collide(arr[t0][a], arr[t1][b]);
					}
				}
			}			
		}		
	}
		
	protected void collide(IColNode a, IColNode b) {
		if (a == b) return;
		
		if (a.intersects(b)) {
			int t0 = a.getType();
			int t1 = b.getType();
						
			if (colMatrix.isColliding(t0, t1)) {
				a.onCollide(b);
			}
			if (colMatrix.isColliding(t1, t0)) {
				b.onCollide(a);
			}			
		}
	}
	
	//Getters
	protected IColNode[][] getNodesAsArray() {
		if (nodes == null) return new IColNode[0][0];
		
		IColNode arr[][] = new IColNode[nodes.size()][];
		for (int n = 0; n < nodes.size(); n++) {
			Collection<IColNode> list = nodes.get(n);
			arr[n] = list.toArray(new IColNode[list.size()]);
		}
		return arr;
	}
	
	//Setters
	@Override
	public void setColMatrix(IColMatrix m) {
		IColNode arr[][] = getNodesAsArray();
		
		colMatrix = m.clone();
		
		nodes = new ArrayList<List<IColNode>>(colMatrix.getSize());
		for (int n = 0; n < arr.length; n++) {
			List<IColNode> list = new ArrayList<IColNode>(arr[n].length);
			for (IColNode node : arr[n]) {
				list.add(node);
			}
			nodes.add(list);
		}
		for (int n = arr.length; n < colMatrix.getSize(); n++) {
			nodes.add(new ArrayList<IColNode>());
		}
	}

}
