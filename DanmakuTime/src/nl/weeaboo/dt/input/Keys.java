package nl.weeaboo.dt.input;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.dt.DTLog;

public class Keys implements Iterable<Entry<String, Integer>> {

	private Map<String, Integer> keys;
	
	public Keys(Class<?> c) {
		//Add keyboard keys
		keys = getKeyCodeConstants(KeyEvent.class);
		
		//Add joypad keys
		for (int j = 1; j <= JoyKey.MAX_JOYPADS; j++) {
			for (JoyKey key : JoyKey.values()) {
				keys.put(key.toKeyName(j), (int)key.toKeyCode(j));
			}
		}
	}
	
	//Functions
	protected static Map<String, Integer> getKeyCodeConstants(Class<?> c) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (Field field : c.getFields()) {
			String fname = field.getName();
			if (fname.startsWith("VK_")) {
				try {
					map.put(fname.substring(3), field.getInt(null));
				} catch (IllegalArgumentException e) {
					DTLog.warning(e);
					break;
				} catch (IllegalAccessException e) {
					DTLog.warning(e);
					break;
				}
			}
		}
		return map;
	}
	
	//Getters
	@Override
	public Iterator<Entry<String, Integer>> iterator() {
		return keys.entrySet().iterator();
	}
	
	public int get(String name) {
		Integer result = keys.get(name);
		return (result != null ? result : 0);
	}
	
	//Setters
	
}
