package nl.weeaboo.dt.lua;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nl.weeaboo.dt.DTLog;
import nl.weeaboo.dt.lua.link.LuaLink;

public class LuaThreadPool {

	private boolean disposed;
	private List<LuaLink> threads;
	private List<LuaLink> standbyList;
	
	public LuaThreadPool() {
		threads = new LinkedList<LuaLink>();
		standbyList = new ArrayList<LuaLink>();
	}
	
	//Functions
	public void dispose() {
		disposed = true;
	}
	
	public void add(LuaLink t) {
		standbyList.add(t);
	}
	
	public void update() {
		threads.addAll(standbyList);
		standbyList.clear();
		
		Iterator<LuaLink> itr = threads.iterator();
		while (itr.hasNext()) {
			if (isDisposed()) return;
			
			LuaLink link = itr.next();
			if (!link.isFinished()) {
				try {
					link.update();
				} catch (LuaException e) {
					DTLog.warning(e);
				}
			}
			if (link.isFinished()) {
				itr.remove();
			}
		}
	}
	
	//Getters
	public boolean isDisposed() {
		return disposed;
	}
	
	//Setters
	
}
