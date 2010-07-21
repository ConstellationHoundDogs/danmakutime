package nl.weeaboo.dt.lua;

import java.awt.event.KeyEvent;
import java.util.Random;

import nl.weeaboo.common.Log;
import nl.weeaboo.dt.TinyMap;
import nl.weeaboo.dt.audio.ISoundEngine;
import nl.weeaboo.dt.collision.CircleColNode;
import nl.weeaboo.dt.collision.ColMatrix;
import nl.weeaboo.dt.field.Field;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.object.Drawable;
import nl.weeaboo.dt.object.Sprite;
import nl.weeaboo.dt.object.TextDrawable;
import nl.weeaboo.dt.renderer.BlendMode;
import nl.weeaboo.dt.renderer.ITextureStore;
import nl.weeaboo.dt.renderer.Renderer;

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
			ITextureStore ts, ISoundEngine se)
	{
		random = new Random(seed);
		threadPool = tp;
		fieldMap = fm;
		
		Platform.setInstance(new J2sePlatform());
		LuaC.install();
		
		//Install default available objects
		vm = Platform.newLuaState();
		LuaUtil.installLuaLib(this, vm._G);
		
		LuaUtil.registerClass2(this, vm, Drawable.class);
		LuaUtil.registerClass2(this, vm, TextDrawable.class);
		LuaUtil.registerClass2(this, vm, Sprite.class);
		
		try {
			LuaUtil.registerClass(this, vm, ColMatrix.class);
			LuaUtil.registerClass(this, vm, CircleColNode.class);
		} catch (LuaException e) {
			Log.showError(e);
		}
		
		LuaUtil.registerEnum(vm, BlendMode.class);
		LuaUtil.registerKeyCodes(vm, KeyEvent.class);
		
		LuaUtil.registerThreadLib(this, vm, threadPool);
		LuaUtil.registerFieldLib(this, vm);

		//global ITextureStore textureStore
		vm.pushlvalue(LuajavaLib.toUserdata(ts, ts.getClass()));
		vm.setglobal("textureStore");

		//global ISoundEngine soundEngine
		vm.pushlvalue(LuajavaLib.toUserdata(se, se.getClass()));
		vm.setglobal("soundEngine");
	}
	
	//Functions
	public IField createField(int x, int y, int w, int h, int pad) {
		int id = fieldMap.getSize();
		while (fieldMap.containsKey(id)) {
			id++;
		}
		return createField(id, x, y, w, h, pad);
	}
	public IField createField(int id, int x, int y, int w, int h, int pad) {
		Field field = new Field(x, y, w, h, pad);
		fieldMap.put(id, field);
		return field;
	}
	
	public void updatePaused() {
		for (IField field : fieldMap.getValues()) {
			field.flushStandbyList();
		}
	}
	
	public void update(IInput input) {					
		//Update threads
		threadPool.update();
			
		//Update fields
		for (IField field : fieldMap.getValues()) {
			field.update(input);
		}
	}
	
	public void draw(Renderer r) {
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
