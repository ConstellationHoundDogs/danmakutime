package nl.weeaboo.dt.lua;

import java.awt.event.KeyEvent;
import java.util.Random;

import nl.weeaboo.dt.TinyMap;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.object.Sprite;
import nl.weeaboo.dt.renderer.IRenderer;
import nl.weeaboo.dt.renderer.ITextureStore;

import org.luaj.compiler.LuaC;
import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public class LuaRunState {

	public LuaState vm;
		
	private Random random;
	private LuaThreadPool threadPool;
	private TinyMap<IField> fieldMap;
	private LuaLink current;
	
	public LuaRunState(long seed, LuaThreadPool tp, TinyMap<IField> fm,
			ITextureStore ts)
	{
		random = new Random(seed);
		threadPool = tp;
		fieldMap = fm;
		
		Platform.setInstance(new J2sePlatform());
		LuaC.install();
		
		//Install default available objects
		vm = Platform.newLuaState();
		LuaUtil.installLuaLib(this, vm._G);
		LuaUtil.registerClass(this, vm, Sprite.class);
		LuaUtil.registerKeyCodes(vm, KeyEvent.class);
		LuaUtil.registerThreadLib(this, vm, threadPool);

		//global ITextureStore textureStore
		vm.pushlvalue(LuajavaLib.toUserdata(ts, ts.getClass()));
		vm.setglobal("textureStore");		
	}
	
	//Functions
	public void update(IInput input) {
		
		//Update threads
		threadPool.update();
			
		//Update fields
		for (IField field : fieldMap.getValues()) {
			field.update(input);
		}		
	}
	
	public void draw(IRenderer r) {
		for (IField field : fieldMap.getValues()) {
			field.draw(r);
		}		
	}
	
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
	public int getObjectCount() {
		int count = 0;
		for (IField field : fieldMap.getValues()) {
			count += field.getObjectCount();
		}
		return count;		
	}
	
	//Setters
	public void setCurrentLink(LuaLink cur) {
		current = cur;
	}
	
}
