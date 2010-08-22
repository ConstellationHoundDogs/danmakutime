package nl.weeaboo.dt.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class KeyConfig implements IKeyConfig {

	private Keys keys;
	private Map<KeyMapKey, List<KeyMapEntry>> map;
	
	public KeyConfig(Keys ks) {		
		keys = ks;
		map = new TreeMap<KeyMapKey, List<KeyMapEntry>>();
		
		setKey(1, VKey.UP, "UP");
		setKey(1, VKey.DOWN, "DOWN");
		setKey(1, VKey.LEFT, "LEFT");
		setKey(1, VKey.RIGHT, "RIGHT");
		setKey(1, VKey.BUTTON1, "Z");
		setKey(1, VKey.BUTTON2, "X");
		setKey(1, VKey.BUTTON3, "SHIFT");
		
		setKey(2, VKey.UP, "NUMPAD5");
		setKey(2, VKey.DOWN, "NUMPAD2");
		setKey(2, VKey.LEFT, "NUMPAD1");
		setKey(2, VKey.RIGHT, "NUMPAD3");
		setKey(2, VKey.BUTTON1, "NUMPAD4");
		setKey(2, VKey.BUTTON2, "NUMPAD0");
		setKey(2, VKey.BUTTON3, "NUMPAD7");
	}
	
	//Functions
	public static KeyConfig load(Keys keys, InputStream in) throws IOException {
		KeyConfig conf = new KeyConfig(keys);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = reader.readLine()) != null) {
			int index = line.indexOf('=');
			String key = line.substring(0, index).trim();
			String vals[] = line.substring(index+1).split("\\|");
			for (int n = 0; n < vals.length; n++) {
				vals[n] = vals[n].trim();
			}
			
			if (key.matches("p\\d\\..*")) {
				index = key.indexOf('.');
				int playerId = Integer.parseInt(key.substring(1, index));
				VKey vkey = VKey.fromName(key.substring(index+1));
				if (playerId >= 1 && playerId <= VKey.MAX_PLAYERS && vkey != null) {
					conf.clearKey(playerId, vkey);
					for (String val : vals) {
						conf.setKey(playerId, vkey, val);
					}
				}
			}
		}
		
		return conf;
	}
	
	public void save(OutputStream out) throws IOException {
		PrintWriter pout = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
		
		for (Entry<KeyMapKey, List<KeyMapEntry>> mapEntry : map.entrySet()) {
			KeyMapKey key = mapEntry.getKey();
			pout.printf("p%d.%s=", key.getPlayerId(), key.getKey().name());
			
			int t = 0;
			for (KeyMapEntry entry : mapEntry.getValue()) {
				String s = entry.getName();
				if (s == null) s = Integer.toString(entry.getKeyCode());
				
				if (t > 0) pout.print("|");
				pout.print(s);
				
				t++;
			}
			pout.println();
		}
		pout.flush();
	}
	
	protected void addEntry(KeyMapEntry entry) {
		List<KeyMapEntry> entries = map.get(entry.getKey());
		if (entries == null) {
			entries = new ArrayList<KeyMapEntry>(2);
			map.put(entry.getKey(), entries);
		}
		
		if (!entries.contains(entry)) {
			entries.add(entry);
		}
	}
	
	//Getters
	@Override
	public int[] getPhysicalKeyCodes(int player, VKey vkey) {
		KeyMapKey key = new KeyMapKey(player, vkey);
		Collection<KeyMapEntry> entries = map.get(key);
		if (entries == null) {
			return new int[0];
		}
		
		int res[] = new int[entries.size()];
		int t = 0;
		for (KeyMapEntry entry : entries) {
			res[t++] = entry.getKeyCode();
		}		
		return res;
	}

	@Override
	public int[] getVKeysHeld(IInput i) {
		VKey keys[] = VKey.values();		
		int result[] = new int[VKey.MAX_PLAYERS * keys.length];
		
		int t = 0;
		for (int player = 1; player <= VKey.MAX_PLAYERS; player++) {
			for (VKey key : keys) {
				for (int code : getPhysicalKeyCodes(player, key)) {
					if (i.isKeyHeld(code)) { //If physical
						result[t++] = key.toKeyCode(player); //Return virtual
						break;
					}
				}
			}
		}
		return Arrays.copyOf(result, t);
	}

	@Override
	public int[] getVKeysPressed(IInput i) {
		VKey keys[] = VKey.values();		
		int result[] = new int[VKey.MAX_PLAYERS * keys.length];
		
		int t = 0;
		for (int player = 1; player <= VKey.MAX_PLAYERS; player++) {
			for (VKey key : keys) {
				for (int code : getPhysicalKeyCodes(player, key)) {
					if (i.isKeyPressed(code)) { //If physical
						result[t++] = key.toKeyCode(player); //Return virtual
						break;
					}
				}
			}
		}
		return Arrays.copyOf(result, t);
	}
	
	protected int getKeyCode(String name) {
		int result = keys.get(name);
		if (result == 0) {
			try {
				result = Integer.parseInt(name);
			} catch (NumberFormatException nfe) {				
			}
		}
		return result;
	}
	
	//Setters
	protected void clearKey(int playerId, VKey key) {
		map.remove(new KeyMapKey(playerId, key));
	}
	protected void setKey(int playerId, VKey key, String keyName) {
		addEntry(new KeyMapEntry(playerId, key, keyName, getKeyCode(keyName)));
	}
	protected void setKey(int playerId, VKey key, int keyCode) {
		addEntry(new KeyMapEntry(playerId, key, null, keyCode));
	}
	
	//Inner Classes
	private static class KeyMapKey implements Comparable<KeyMapKey> {
		
		private final int playerId;
		private final VKey key;
		
		public KeyMapKey(int pid, VKey k) {
			playerId = pid;
			key = k;
		}
		
		public int getPlayerId() { return playerId; }
		public VKey getKey() { return key; }
		
		public boolean equals(Object obj) {
			if (obj instanceof KeyMapKey) {
				KeyMapKey k = (KeyMapKey)obj;
				return playerId == k.playerId
					&& key == k.key;
			}
			return false;
		}

		public int compareTo(KeyMapKey k) {
			if (playerId < k.playerId) return -1;
			if (playerId > k.playerId) return 1;
			
			return key.compareTo(k.key);
		}
		
	}
	
	private static class KeyMapEntry {
		
		private final KeyMapKey key;
		private final String name;
		private final int keycode;
		
		public KeyMapEntry(int pid, VKey k, String s, int i) {
			key = new KeyMapKey(pid, k);
			name = s;
			keycode = i;
		}
		
		public KeyMapKey getKey() { return key; }
		public String getName() { return name; }
		public int getKeyCode() { return keycode; }
	
		public boolean equals(Object obj) {
			if (obj instanceof KeyMapEntry) {
				KeyMapEntry m = (KeyMapEntry)obj;
				return key.equals(m.key)
					&& (name == m.name || (name != null && name.equals(m.name)))
					&& keycode == m.keycode;					
			}
			return false;
		}
		
	}
	
}
