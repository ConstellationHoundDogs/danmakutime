package nl.weeaboo.dt.input;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class InputBuffer {

	private Map<Long, BufEntry> map;
	private int numPlayers;
	private long curFrame;
	private long maxInputLag;
	private double avgDelays[];
	private String delayString;
	
	public InputBuffer(long maxLag) {
		map = new LinkedHashMap<Long, BufEntry>();
		
		maxInputLag = maxLag;
		avgDelays = new double[numPlayers+1];
	}
	
	//Functions
	public void clear() {
		map.clear();
	}
	
	public void addKeys(long frame, int playerId, IInput input) {		
		BufEntry stored = map.get(frame);
		if (stored == null) {			
			map.put(frame, new BufEntry(numPlayers, curFrame, input.clone(), playerId));
		} else {
			stored.add(curFrame, playerId, input);
		}
	}
	
	public IInput get(long frame) {
		BufEntry entry = map.get(frame);
		if (entry != null && entry.hasReceivedAll()) {
			updateDelays(frame, entry);
			return entry.input;
		}
		throw new ArrayIndexOutOfBoundsException("Input not ready, frame=" + frame);
	}
	
	protected void updateDelays(long frame, BufEntry be) {
		long delays[] = be.getDelays(frame);
		for (int n = 0; n < delays.length; n++) {
			avgDelays[n] = 0.9*avgDelays[n] + 0.1*delays[n];
		}
		
		StringBuilder sb = new StringBuilder();
		for (double d : avgDelays) {
			sb.append(String.format(Locale.ROOT, "%.1f ", d));
		}
		delayString = sb.toString();
	}
	
	//Getters
	public String getDelayString() {
		return delayString != null ? delayString : "";
	}
	
	public boolean hasReceived(long frame) {
		BufEntry be = map.get(frame);
		if (be != null) {
			return be.hasReceivedAll();
		}
		return false;
	}
	
	//Setters
	public void setNumPlayers(int p) {
		if (numPlayers != p) {
			numPlayers = p;
			
			avgDelays = new double[numPlayers+1];
			for (BufEntry entry : map.values()) {
				entry.setNumPlayers(p);
			}
		}
	}
	
	public void setLocalFrame(long frame) {
		curFrame = frame;
		
		Iterator<Entry<Long, BufEntry>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<Long, BufEntry> entry = itr.next();
			if (entry.getKey() < frame - maxInputLag * 2) {
				//itr.remove();
			}
		}
	}
	
	//Inner Classes
	private static class BufEntry {
	
		public final IInput input;
		private long received[];		
		
		public BufEntry(int numPlayers, long frame, IInput i, int playerId) {
			if (frame < 0) throw new IllegalArgumentException("frame="+frame);

			input = i;
			received = new long[numPlayers+1]; //Index 0 is for the full keyboard messages
			Arrays.fill(received, -1);
			received[playerId] = frame;
		}
		
		public void add(long frame, int playerId, IInput i) {
			if (frame < 0) throw new IllegalArgumentException("frame="+frame);
			if (received[playerId] > 0) return; //Already received
			
			received[playerId] = frame;

			for (int pressed : i.getKeysPressed()) {
				input.setKeyPressed(pressed);
			}
			for (int held : i.getKeysHeld()) {
				input.setKeyHeld(held);	
			}
		}
		
		public boolean hasReceivedAll() {
			for (int n = 0; n < received.length; n++) {
				if (received[n] < 0) {
					//System.out.println(n);
					return false;
				}
			}
			return true;
		}
		
		public long[] getDelays(long frame) {
			long delays[] = new long[received.length];
			for (int n = 0; n < delays.length; n++) {
				delays[n] = received[n] - frame;
			}
			return delays;
		}
		
		public void setNumPlayers(int p) {
			if (received.length != p) {
				received = Arrays.copyOf(received, p);
			}			
		}
		
	}
	
}
