package com.mypinguin.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.Particle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class ParticlesManager {

	class Particle extends Sprite
	{
		public Particle(Sprite sprite)
		{
			super(sprite);
		}
	}
	
	class PEmitter extends Actor
	{
		protected PenguinGame game   = null;
		public Sprite sprite = null;
	
		public Particle[] particles;
		public int[] cachedIndexes;
		public ArrayList<Integer> usedIndexes;
		public int cachedIndexesCount = 0;
		public int usedIndexesCount = 0;
		
		public boolean unlimitedGenerationEnabled = true;
		
		public PEmitter(PenguinGame game, Sprite sprite)
		{
			super();
			this.game = game;
			this.sprite = sprite;
		}
		
		public void generate(int particlesCount)
		{
			Particle[] particles = this.particles;
			ArrayList<Integer> usedIndexes = this.usedIndexes;
			Integer index;
			
			while (particlesCount > 0) {
				index = popCachedIndex();
				if (index != -1) {
					usedIndexes.add(index);
					resetParticle(particles[index]);
				}
				else
				{
					if (unlimitedGenerationEnabled) {
						index = usedIndexes.remove(0);
						usedIndexes.add(index);
						resetParticle(particles[index]);
					}
				}
				particlesCount--;
			}
		}
		
		public int popCachedIndex()
		{
			if (cachedIndexesCount == 0) return -1;
				
			int result = cachedIndexes[cachedIndexesCount-1];
			
			cachedIndexes[cachedIndexesCount-1] = -1;
			cachedIndexesCount--;
			usedIndexesCount++;
			
			return result;
		}
		
		public void pushCachedIndex(int index)
		{
			if (cachedIndexesCount == cachedIndexes.length) return;
			
			cachedIndexes[cachedIndexesCount] = index;
			cachedIndexesCount++;
			usedIndexesCount--;
		}
		
		public void setMaxParticlesCount(int maxCount)
		{
			if (null == sprite) return;
			
			particles = new Particle[maxCount];
			cachedIndexes = new int[maxCount];
			usedIndexes = new ArrayList<Integer>(maxCount);
			
			while (maxCount > 0) {
				maxCount--;
				particles[maxCount] = new Particle(sprite);
				pushCachedIndex(maxCount);
			}
			
			usedIndexesCount = 0;
		}
		
		public void act(float delta)
		{
			super.act(delta);
			
			Particle[] particles = this.particles;
			ArrayList<Integer> usedIndexes = this.usedIndexes;
			
			//Emit particles
			emit();
			
			//Update active particles
			Iterator<Integer> it = usedIndexes.iterator();
			Integer index;
			
			while (it.hasNext())
			{
				index = it.next();
				if (false == updateParticle(delta,particles[index])) {
					it.remove();
					pushCachedIndex(index);
				}
			}
		}
		
		public void draw(Batch batch, float parentAlpha)
		{
			super.draw(batch, parentAlpha);
			
			Particle[] particles = this.particles;
			ArrayList<Integer> usedIndexes = this.usedIndexes;
			
			Iterator<Integer> it = usedIndexes.iterator();
			Integer index;
			
			while (it.hasNext()) {
				index = it.next();
				particles[index].draw(batch);
			}
		}
		
		public void emit()
		{
			
		}
		
		public void resetParticle(Particle particle)
		{
			particle.setPosition(getX(), getY());
		}
		
		public boolean updateParticle(float delta, Particle particle)
		{
			return false;
		}
	}
	
	
	public PenguinGame 	game;
	
	//private ParticleEmitter builtin_snow = null;
	private TextureAtlas particles_atlas = null;

	public ParticlesManager( PenguinGame penguinGame ) {
		if (penguinGame == null) throw new IllegalArgumentException("penguinGame cannot be null.");
		this.game = penguinGame;
		Prepare();
	}
	
	public void Prepare() {
		game.asset.load("particles.atlas", TextureAtlas.class);
		game.asset.finishLoadingAsset("particles.atlas");
		
		particles_atlas = game.asset.get("particles.atlas",TextureAtlas.class);
	}
	
	public void BuiltIn_Render(Stage stage, SpriteBatch batch, float delta)
	{
		Boolean batch_isDrawing = batch.isDrawing();
		
		if (batch_isDrawing) batch.end();
		
		batch.begin();
		//builtin_snow.draw(batch, delta);
		//builtin_snow.getSprite().setCenter(game.player.getX(), game.player.getY());
		//builtin_snow.getSprite().draw(batch);
		batch.end();
		
		if (batch_isDrawing) batch.begin();
	}

	public void BuiltIn_Snow_Enable()
	{
	}
	
	public void BuiltIn_Snow_Disable()
	{
	}
	
	public void dispose()
	{
		particles_atlas.dispose();
		game.asset.unload("particles.atlas");
	}
}


