package com.penguin.particles;

import com.badlogic.gdx.graphics.Camera;
import com.mypinguin.game.PenguinGame;

public class Emitter_Snow extends BaseEmitter
{
	float delay = 0;
	private Camera camera;
	private float screenWidth, screenHeight, screenLeft, screenRight;
	
	public Emitter_Snow(PenguinGame game, Camera camera) {
		super(game, game.particles.getParticleSprite("tiny_snowflake"));
		
		setMaxParticlesCount(100, Particle.class);
		
		this.camera = camera;
	}
	
	public void emit(float delta) {
		screenWidth = camera.viewportWidth*1.3f;
		screenHeight = camera.viewportHeight*1.3f;
		screenLeft = camera.position.x - screenWidth*0.5f;
		screenRight = screenLeft + screenWidth;
		
		setPosition(camera.position.x, camera.position.y + screenHeight*0.5f);
		
		
		
		delay += delta*1000;
		if (delay > 100)
		{
			delay = 0;
			generate(1);
		}
	}
	
	public boolean updateParticle(float delta, Particle particle)
	{ 
		particle.translateY(-delta*100);
		particle.setAlpha(particle.life/5.0f);
		particle.life -= delta;
		
		if (particle.getX() < screenLeft) particle.setX(particle.getX()+screenWidth);
		if (particle.getX() > screenRight) particle.setX(particle.getX()-screenWidth);
		
		if (particle.life <= 0) return false;
		return true;
	}
	
	public void resetParticle(Particle particle)
	{
		particle.life = 5;
		particle.setPosition((float) (screenLeft+Math.random()*screenWidth), getY());
	}
}
