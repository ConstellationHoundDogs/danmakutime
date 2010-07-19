package nl.weeaboo.dt.lua;

import java.util.Random;

import nl.weeaboo.dt.TinyMap;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.object.Sprite;
import nl.weeaboo.dt.renderer.ITextureStore;

import org.luaj.compiler.LuaC;
import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public class LuaRunState {

	public LuaState vm;
		
	private Random random;
	private TinyMap<IField> fieldMap;
	private LuaLink current;
	
	public LuaRunState(long seed, TinyMap<IField> fm, ITextureStore ts) {
		random = new Random(seed);
		fieldMap = fm;
		
		Platform.setInstance(new J2sePlatform());
		LuaC.install();
		
		vm = Platform.newLuaState();
		LuaUtil.installDefaultFunctions(this, vm._G);
		LuaUtil.registerClass(this, vm, Sprite.class);
		
		vm.pushlvalue(LuajavaLib.toUserdata(ts, ts.getClass()));
		vm.setglobal("textureStore");
	}
	
	//Functions
	
	//Getters
	public LuaLink getCurrentLink() {
		return current;
	}
	public Random getRandom() {
		return random;
	}
	public IField getField(int key) {
		return fieldMap.get(key);
	}
	
	//Setters
	public void setCurrentLink(LuaLink cur) {
		current = cur;
	}
	
}
