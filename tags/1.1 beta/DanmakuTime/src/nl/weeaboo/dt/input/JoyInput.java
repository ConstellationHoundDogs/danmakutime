package nl.weeaboo.dt.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.Identifier.Axis;

public class JoyInput {

	private final int index;
	private final Controller controller;
	
	private float deadZone = 0.1f;
	private Map<Integer, JoyKey> mapping;
	private TreeSet<JoyKey> oldPressed;
	private TreeSet<JoyKey> newPressed;
	
	public JoyInput(int ix, Controller con) {
		index = ix;
		controller = con;
		
		mapping = new HashMap<Integer, JoyKey>();	
		oldPressed = new TreeSet<JoyKey>();
		newPressed = new TreeSet<JoyKey>();
		
		int analogs = 0;
		int buttonNumber = 0;
		for (int n = 0; n < controller.getComponents().length; n++) {
			Component c = controller.getComponents()[n];
			if (c.getIdentifier() instanceof Axis) {
				Axis ident = (Axis)c.getIdentifier();

				if (ident == Axis.X) {
					mapping.put(-n, JoyKey.LEFT);
					mapping.put( n, JoyKey.RIGHT);
				} else if (ident == Axis.Y) {
					mapping.put(-n, JoyKey.UP);
					mapping.put( n, JoyKey.DOWN);
				}
				analogs++;
			} else if (c.getIdentifier() instanceof Component.Identifier.Button) {
				mapping.put(n, JoyKey.fromOrdinal(JoyKey.BUTTON1.ordinal()+buttonNumber));
				buttonNumber++;
			}
		}
	}
		
	//Functions
	public static List<Controller> getControllers() {
		try {
			Logger jinputLogger = Logger.getLogger("net.java.games.input.DefaultControllerEnvironment");
			jinputLogger.setLevel(Level.WARNING);
		
			List<Controller> list = new ArrayList<Controller>();
			
			ControllerEnvironment controllerEnv = ControllerEnvironment.getDefaultEnvironment();		
	        for (Controller c : controllerEnv.getControllers()) {
	        	if (c.getType() == Controller.Type.STICK
	        			|| c.getType() == Controller.Type.GAMEPAD)
	        	{
	        		list.add(c);
	        	}
	        }
	        
	        return list;        
		} catch (SecurityException se) {
			return Collections.emptyList();
		}
	}
		
	public void update(IInput input) {
		if (controller == null) return;
		
		controller.poll();

		newPressed.clear();		
		for (int n = 0; n < controller.getComponents().length; n++) {
			Component c = controller.getComponents()[n];
			Identifier ident = c.getIdentifier();					
			float p = c.getPollData();
			
			if (ident == Axis.POV) {
				if (p == Component.POV.LEFT || p == Component.POV.UP_LEFT || p == Component.POV.DOWN_LEFT) {
					newPressed.add(JoyKey.LEFT);
				}
				if (p == Component.POV.RIGHT || p == Component.POV.UP_RIGHT || p == Component.POV.DOWN_RIGHT) {
					newPressed.add(JoyKey.RIGHT);
				}
				if (p == Component.POV.UP || p == Component.POV.UP_LEFT || p == Component.POV.UP_RIGHT) {
					newPressed.add(JoyKey.UP);
				}
				if (p == Component.POV.DOWN || p == Component.POV.DOWN_LEFT || p == Component.POV.DOWN_RIGHT) {
					newPressed.add(JoyKey.DOWN);
				}
			} else {			
				if (Math.abs(p) > deadZone){
					p = (p > 0 ? 1f : -1f);
				}
				
				JoyKey key;
				
				key = mapping.get(-n);
				if (key != null && p == -1f) {
					newPressed.add(key);
				}
				
				key = mapping.get(n);
				if (key != null && p ==  1f) {
					newPressed.add(key);
				}
			}
		}
		
		//Detect key press changes
		for (JoyKey key : oldPressed) {
			if (!newPressed.contains(key)) {
				input.setKeyReleased(key.toKeyCode(index));
			}
		}
		for (JoyKey key : newPressed) {
			if (!oldPressed.contains(key)) {
				input.setKeyPressed(key.toKeyCode(index));
			} else {
				input.setKeyHeld(key.toKeyCode(index));
			}
		}

		//Store current state as old state
		TreeSet<JoyKey> temp = oldPressed;
		oldPressed = newPressed;
		newPressed = temp;
	}
	
	//Getters
	
	//Setters
		
}
