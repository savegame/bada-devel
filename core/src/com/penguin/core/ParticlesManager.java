package com.penguin.core;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.penguin.particles.BaseEmitter;
import com.penguin.particles.Emitter_Snow;

import java.util.ArrayList;
import java.util.Iterator;

public class ParticlesManager {
	//---------------------------------------------
	
	class Layer extends Group
	{
		int index;
	}
	
	public PenguinGame 	game = null;
	public Group root = null;
	public ArrayList<Layer> layers;
	
	private TextureAtlas particles_atlas = null;
	public Emitter_Snow snowEmitter = null;

	public ParticlesManager( PenguinGame penguinGame ) {
		if (penguinGame == null) throw new IllegalArgumentException("penguinGame cannot be null.");
		this.game = penguinGame;
		
		prepare();
	}
	
	public Layer getLayer(int layer)
	{
		Iterator<Layer> it = layers.iterator();
		Layer result;
		
		while (it.hasNext()) {
			result = it.next();
			if (result.index == layer) return result;
		}
		
		result = new Layer();
		result.index = layer;
		layers.add(result);
		
		return result;
	}
	
	public void addEmitter(BaseEmitter emitter, int layer)
	{
		getLayer(layer).addActor(emitter);
	}
	
	public void removeEmitterByName(String name, int layer)
	{
		Layer pLayer = getLayer(layer);
		Actor emitter = pLayer.findActor(name);
		if (emitter != null)
		{
			emitter.clear();
			emitter.remove();
		}
	}

//	public void removeEmitter( BaseEmitter emitter, int layer ) {
//		Layer pLayer = getLayer(layer);
//		//Actor emitter = pLayer.findActor(name);
//		if (emitter != null)
//		{
//			pLayer.removeActor(emitter);
//			emitter.clear();
//			emitter.remove();
//		}
//	}
	
	public void clearLayers(){
		Iterator<Layer> it = layers.iterator();
		Layer layer;
		
		while (it.hasNext())
		{
			layer = it.next();
			layer.clear();
			layer.remove();
			it.remove();
		}
	}
	
	public Sprite getParticleSprite(String name)
	{
		Sprite result = particles_atlas.createSprite(name);
		
		if (result == null)
			result = particles_atlas.createSprite("default");
		
		return result;
	}
	
	public void prepare() {
		game.asset.load("particles.atlas", TextureAtlas.class);
		game.asset.finishLoadingAsset("particles.atlas");
		
		particles_atlas = game.asset.get("particles.atlas",TextureAtlas.class);
		
		layers = new ArrayList<Layer>();
		
		//root = new Group();
		
		//snowEmitter = new Emitter_Snow(game, particles_atlas.createSprite("tiny_snowflake"));
		//root.addActor(snowEmitter);
	}
	
	public void dispose()
	{
		clearLayers();
		particles_atlas.dispose();
		game.asset.unload("particles.atlas");
	}
}


