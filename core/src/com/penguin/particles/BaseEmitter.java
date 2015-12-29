package com.penguin.particles;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.penguin.particles.Particle;
import com.mypinguin.game.PenguinGame;

public class BaseEmitter<T> extends Actor
{
	protected PenguinGame game = null;
	public Sprite sprite = null;
	Class<T> particleClass;

	public LinkedList<Particle> particlesBuffer;
	public LinkedList<Particle> particlesActive;
	
	public boolean unlimitedGenerationEnabled = true;
	public float particlesToUpdatePerSecond;
	
	private int iterationIndex = 0;
	private Iterator<Particle> iterationHandler;
	private float iterationDelta = 0;
	
	public BaseEmitter(PenguinGame game, Sprite sprite, Class<T> particleClass)
	{
		super();
		this.game = game;
		this.sprite = sprite;
		
		this.particleClass = particleClass;
		
		particlesBuffer = new LinkedList<Particle>();
		particlesActive = new LinkedList<Particle>();
		
		SetParticlesToUpdate(60,30);
	}
	
	public void SetParticlesToUpdate(int particlesPerFrame, int framesPerSecond)
	{
		particlesToUpdatePerSecond = particlesPerFrame*framesPerSecond;
	}
	
	public void WarmUp(float timeInSeconds, float stepInSeconds)
	{
		for (;;)
		{
			act(stepInSeconds);
			timeInSeconds -= stepInSeconds;
			if (timeInSeconds < 0) break;
		}
	}
	
	public void generate(int particlesCount)
	{
		Particle tmpParticle;
		
		while (particlesCount > 0)
		{
			if (particlesBuffer.size() > 0)
			{
				tmpParticle = particlesBuffer.pop();
				particlesActive.add(tmpParticle);
				resetParticle(tmpParticle);
			}
			else
			{
				if (unlimitedGenerationEnabled) {
					tmpParticle = particlesActive.pop();
					particlesActive.add(tmpParticle);
					resetParticle(tmpParticle);
				} else {
					try {
						tmpParticle = (Particle) particleClass.getConstructor(Sprite.class).newInstance(sprite);
						particlesActive.add(tmpParticle);
						resetParticle(tmpParticle);
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
				}
			}
			
			particlesCount--;
		}
	}
	
	//particleClass must be derived from Particle
	public void setMaxParticlesCount(int maxCount)
	{
		if (null == sprite) return;
		
		maxCount -= (particlesActive.size()+particlesBuffer.size());
		
		while (maxCount > 0)
		{
			try {
				particlesBuffer.add((Particle) particleClass.getConstructor(Sprite.class).newInstance(sprite));
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
			
			maxCount--;
		}
	}
	
	public void act(float delta)
	{
		super.act(delta);
		
		//Emit particles
		emit(delta);

		boolean justStarted = (iterationIndex == 0);

		int particlesToUpdate = (int) Math.round(particlesToUpdatePerSecond*delta);
		if (particlesToUpdate == 0) particlesToUpdate = 1; //always update at least 1 particle
		
		if (justStarted) iterationDelta = delta;
		else			 iterationDelta += delta;
		
		Particle tmpParticle;
		iterationHandler = particlesActive.listIterator(iterationIndex);
		
		if (iterationHandler.hasNext())
		for(int i = 0; i < particlesToUpdate; i++)
		{
			tmpParticle = iterationHandler.next();
			
			if (false == updateParticle(iterationDelta, tmpParticle))
			{
				particlesBuffer.add(tmpParticle);
				iterationHandler.remove();
				iterationIndex--;
			}
			
			iterationIndex++;
			if (iterationIndex >= particlesActive.size())
			{
				iterationIndex = 0;
				iterationDelta = delta;
				iterationHandler = particlesActive.iterator();
				
				if (justStarted) break; //не даем обновлять за один цикл больше 100% частиц
				if (!iterationHandler.hasNext()) break;
			}
		}
	}
	
	public void draw(Batch batch, float parentAlpha)
	{
		super.draw(batch, parentAlpha);
		
		Iterator<Particle> it = particlesActive.iterator();
		while (it.hasNext()) {
			it.next().draw(batch);
		}
	}
	
	//virtual methods
	public void emit(float delta) {}
	public void resetParticle(Particle particle) {}
	public boolean updateParticle(float delta, Particle particle) { return true; }
}
