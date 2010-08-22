package nl.weeaboo.dt.lua;

import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import nl.weeaboo.dt.DTLog;
import nl.weeaboo.dt.audio.ISoundEngine;
import nl.weeaboo.dt.collision.CircleColNode;
import nl.weeaboo.dt.collision.ColMatrix;
import nl.weeaboo.dt.collision.LineSegColNode;
import nl.weeaboo.dt.collision.RectColNode;
import nl.weeaboo.dt.field.Field;
import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.input.Keys;
import nl.weeaboo.dt.io.IPersistentStorage;
import nl.weeaboo.dt.lua.link.LuaLink;
import nl.weeaboo.dt.lua.platform.LuaPlatform;
import nl.weeaboo.dt.lua.platform.LuajavaLib;
import nl.weeaboo.dt.object.Drawable;
import nl.weeaboo.dt.object.FontStyle;
import nl.weeaboo.dt.object.Sprite;
import nl.weeaboo.dt.object.TextDrawable;
import nl.weeaboo.dt.renderer.BlendMode;
import nl.weeaboo.dt.renderer.ITextureStore;
import nl.weeaboo.dt.renderer.Renderer;

import org.luaj.compiler.LuaC;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public class LuaRunState {

	public LuaState vm;
		
	private boolean disposed;
	private Random random;
	private LuaThreadPool threadPool;
	private Map<Integer, IField> fieldMap;
	
	private LuaLink current;
	
	public LuaRunState(long seed, LuaPlatform platform, Keys keys, LuaThreadPool tp,
			Map<Integer, IField> fm, ITextureStore ts, ISoundEngine se,
			IPersistentStorage ps)
	{
		random = new Random(seed);
		threadPool = tp;
		fieldMap = fm;
		
		Platform.setInstance(platform);
		LuaC.install();
		
		//Install default available objects
		vm = Platform.newLuaState();
		LuaUtil.installLuaLib(this, vm._G);
				
		try {
			LuaUtil.registerClass2(this, vm, Drawable.class);
			LuaUtil.registerClass2(this, vm, TextDrawable.class);
			LuaUtil.registerClass2(this, vm, Sprite.class);
			
			LuaUtil.registerClass(this, vm, ColMatrix.class);
			LuaUtil.registerClass(this, vm, CircleColNode.class);
			LuaUtil.registerClass(this, vm, LineSegColNode.class);
			LuaUtil.registerClass(this, vm, RectColNode.class);
		} catch (LuaException e) {
			DTLog.showError(e);
		}
		
		LuaUtil.registerEnum(vm, BlendMode.class);
		LuaUtil.registerEnum(vm, FontStyle.class);
		LuaUtil.registerKeyCodes(vm, keys);
		
		LuaUtil.registerThreadLib(this, vm);
		LuaUtil.registerFieldLib(this, vm);

		//global ITextureStore texStore
		vm.pushlvalue(LuajavaLib.toUserdata(ts, ts.getClass()));
		vm.setglobal("texStore");

		//global ISoundEngine soundEngine
		vm.pushlvalue(LuajavaLib.toUserdata(se, se.getClass()));
		vm.setglobal("soundEngine");

		//global IPersistentStorage storage
		vm.pushlvalue(LuajavaLib.toUserdata(ps, ps.getClass()));
		vm.setglobal("storage");
	}
	
	//Functions
	public void dispose() {
		if (!disposed) {
			disposed = true;
			threadPool.dispose();
		}
	}
	
	public IField createField(int x, int y, int w, int h, int pad) {
		int id = fieldMap.size();
		while (fieldMap.containsKey(id)) {
			id++;
		}
		return createField(id, x, y, w, h, pad);
	}
	public IField createField(int id, int x, int y, int w, int h, int pad) {
		Field field = new Field(x, y, w, h, pad);
		IField old = fieldMap.put(id, field);
		if (old != null) {
			old.clear();
		}
		return field;
	}
	
	public void addThread(LuaLink t) {
		threadPool.add(t);
	}
	
	public void update(IInput input, boolean paused) {
		if (!paused) {
			//Update threads
			threadPool.update();
		}
			
		//Update fields
		int pauseFieldId = 999;
		for (Entry<Integer, IField> entry : fieldMap.entrySet()) {
			if (isDisposed()) return;
			
			if (entry.getKey().intValue() == pauseFieldId || !paused) {
				entry.getValue().update(input);
			}
		}
	}
	
	public void draw(Renderer r) {
		for (IField field : fieldMap.values()) {
			field.draw(r);
		}
	}
	
	public long reseed() {
		long seed = random.nextLong();
		random.setSeed(seed);
		return seed;
	}
	
	//Getters
	public boolean isDisposed() {
		return disposed;
	}
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
		for (IField field : fieldMap.values()) {
			count += field.getObjectCount();
		}
		return count;		
	}
	
	//Setters
	public void setCurrentLink(LuaLink cur) {
		current = cur;
	}
	
}
