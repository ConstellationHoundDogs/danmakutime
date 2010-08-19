package nl.weeaboo.dt.collision;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.weeaboo.common.Benchmark;
import nl.weeaboo.common.StringUtil;

public class ColField implements IColField {

	public enum ColAlgorithm {
		NAIVE, UNIFORM_GRID
	}
	
	protected IColMatrix colMatrix;
	protected Rectangle bounds;
	private List<IColNode> waitList;
	protected List<List<IColNode>> nodes;
	protected ColAlgorithm colAlgorithm = ColAlgorithm.UNIFORM_GRID;
	
	private ColGrid grid;

	//Benchmark stuff
	private boolean benchmark = false;
	private long time = 0;
	private int count = 0;

	public ColField(int x, int y, int w, int h) {
		bounds = new Rectangle(x, y, w, h);
		waitList = new ArrayList<IColNode>();
	}
	
	//Functions	
	@Override
	public void add(IColNode c) {
		waitList.add(c);
	}

	protected void flushWaitList() {
		IColNode waitListArray[] = waitList.toArray(new IColNode[waitList.size()]);
		waitList.clear();
		for (IColNode c : waitListArray) {
			Collection<IColNode> list = nodes.get(c.getType());
			list.add(c);
		}
	}
	
	@Override
	public void remove(IColNode c) {
		waitList.remove(c);
		
		Collection<IColNode> list = nodes.get(c.getType());
		list.remove(c);
	}
	
	@Override
	public void processCollisions() {
		if (colMatrix == null || colMatrix.getSize() == 0) {
			return;
		}
				
		if (benchmark) {
			Benchmark.tick();
		}
		
		flushWaitList();
		
		if (colAlgorithm == ColAlgorithm.NAIVE) {
			//Straightforward comparison of all col nodes
			IColNode arr[][] = getNodesAsArray();		
			for (int t0 = 0; t0 < arr.length; t0++) {
				for (int a = 0; a < arr[t0].length; a++) {
					for (int t1 = t0; t1 < arr.length; t1++) {
						boolean atob = colMatrix.isColliding(t0, t1);
						boolean btoa = colMatrix.isColliding(t1, t0);
						if (!atob && !btoa) {
							continue;
						}
					
						//System.out.println(arr[t0].length + " " + arr[t1].length);
						
						for (int b = 0; b < arr[t1].length; b++) {
							ColUtil.collide(arr[t0][a], arr[t1][b], atob, btoa);
						}
					}
				}			
			}
		} else if (colAlgorithm == ColAlgorithm.UNIFORM_GRID) {
			//Cleverer collision detection scheme using a uniform grid
			grid.setNodes(nodes);
			
			for (Collection<IColNode> list : nodes) {
				for (IColNode node : list) {
					grid.processCollisions(node);
				}
			}		
		} else {
			//No valid algorithm set, don't do any collision detection
		}
		
		if (benchmark) {
			time += Benchmark.tock(false);
			count++;
			
			if (count >= 60) {
				System.out.println(StringUtil.formatTime(time, TimeUnit.NANOSECONDS));
				time = 0;
				count = 0;
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
		
		grid = new ColGrid(colMatrix, bounds.width, bounds.height);
	}

}
