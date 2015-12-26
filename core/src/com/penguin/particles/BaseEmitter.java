package com.penguin.particles;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.penguin.particles.Particle;
import com.mypinguin.game.PenguinGame;

public class BaseEmitter extends Actor
{
	protected PenguinGame game = null;
	public Sprite sprite = null;

	public Particle[] particles;
	public int[] cachedIndexes;
	public ArrayList<Integer> usedIndexes;
	public int cachedIndexesCount = 0;
	public int usedIndexesCount = 0;
	
	public boolean unlimitedGenerationEnabled = true;
	
	public BaseEmitter(PenguinGame game, Sprite sprite)
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
	
	//particleClass must be derived from Particle
	public <T> void setMaxParticlesCount(int maxCount, Class<T> particleClass)
	{
		if (null == sprite) return;
		
		particles = new Particle[maxCount];
		cachedIndexes = new int[maxCount];
		usedIndexes = new ArrayList<Integer>(maxCount);
		
		while (maxCount > 0) {
			maxCount--;

			try {
				particles[maxCount] = (Particle) particleClass.getConstructor(Sprite.class).newInstance(sprite);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
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
	
	//virtual methods
	public void emit(float delta) {}
	public void resetParticle(Particle particle) {}
	public boolean updateParticle(float delta, Particle particle) { return true; }
}
