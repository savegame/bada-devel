package com.mypinguin.game;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ParticlesManager {

	class Particle extends Sprite
	{
		public float life = 0;
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
		
		//T must be instantiated from Particle!
		public <T> void setMaxParticlesCount(int maxCount)
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
			emit(delta);
			
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
		
		public void emit(float delta)
		{
			
		}
		
		public void resetParticle(Particle particle)
		{
			particle.setPosition(getX(), getY());
		}
		
		public boolean updateParticle(float delta, Particle particle)
		{
			return true;
		}
	}
	
	class PEmitter_Snow extends PEmitter
	{
		float delay = 0;
		
		public PEmitter_Snow(PenguinGame game, Sprite sprite) {
			super(game, sprite);
			setMaxParticlesCount(100);
		}
		
		public void emit(float delta) {
			delay += delta*1000;
			if (delay > 100)
			{
				delay = 0;
				generate(5);
			}
		}
		
		public boolean updateParticle(float delta, Particle particle)
		{ 
			particle.translateY(-delta*100);
			particle.setAlpha(particle.life);
			particle.life -= delta;
			if (particle.life <= 0) return false;
			return true;
		}
		
		public void resetParticle(Particle particle)
		{
			particle.life = 1;
			particle.setPosition((float) (getX()-400.0f+Math.random()*800.0f), getY());
		}
	}
	
	//---------------------------------------------
	
	public PenguinGame 	game;
	
	//private ParticleEmitter builtin_snow = null;
	private TextureAtlas particles_atlas = null;
	public PEmitter_Snow snowEmitter = null;

	public ParticlesManager( PenguinGame penguinGame ) {
		if (penguinGame == null) throw new IllegalArgumentException("penguinGame cannot be null.");
		this.game = penguinGame;
		Prepare();
	}
	
	public void Prepare() {
		game.asset.load("particles.atlas", TextureAtlas.class);
		game.asset.finishLoadingAsset("particles.atlas");
		
		particles_atlas = game.asset.get("particles.atlas",TextureAtlas.class);
		
		snowEmitter = new PEmitter_Snow(game, particles_atlas.createSprite("tiny_snowflake"));
	}
	
	public void dispose()
	{
		particles_atlas.dispose();
		game.asset.unload("particles.atlas");
	}
}


