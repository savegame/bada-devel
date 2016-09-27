package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Disposable;
import com.mypinguin.game.PenguinGame;
import com.penguin.physics.BoxActor;

/**
 * Created by savegame on 23.05.16.
 */
public class Emitter_BodyActor<T> extends BaseEmitter implements Disposable {
	private FixtureDef fixtureDef = null;
	private TextureRegion textureRegion = null;
	private Vector2 direction = new Vector2(0,0);
	private Shape shape = null;
	private float emissionTimer = 0; //время прошедшее с последнего emit
	private float emitTime = 3f; // таймаут между emit
	private float destroyForce = 10.0f;
	private boolean breakable = true;
	Class<T> bodyActorClass;

	public Emitter_BodyActor(PenguinGame game, Shape shape, FixtureDef fixtureDef, TextureRegion textureRegion, Class<Particle_BodyActor> particleClass) {
		super(game, game.particles.getParticleSprite("default"), particleClass);
		this.fixtureDef = fixtureDef;
		this.textureRegion = textureRegion;
		this.shape = shape;
		unlimitedGenerationEnabled = false;
	}
	
	public void setBreakable(boolean breakable) {
		this.breakable = breakable;
	}
	
	public void setDestroyForce(float force)
	{
		this.destroyForce = force;
	}
	
	public boolean isBreakable() {
		return this.breakable;
	}
	
	public void setEmitTime( float time ) {
		this.emitTime = time;
	}

	public float getEmitTime() {
		return this.emitTime;
	}

	public void setBodyActorClass(Class<T> bodyActorClass) {
		this.bodyActorClass = bodyActorClass;
	}

	public void emit(float delta)
	{
		emissionTimer += delta;

		if( emissionTimer >= emitTime )
		{
			emissionTimer = 0;
			if(particlesBuffer.isEmpty() == false )
				generate(1);
		}
	}

	public void resetParticle(Particle particle) {
		if( particle instanceof Particle_BodyActor == false )
			return;

		Particle_BodyActor bodyActor = (Particle_BodyActor)particle;
		bodyActor.actor = new BoxActor(game, textureRegion, fixtureDef);
		bodyActor.actor.setPosition(getX(), getY());
		bodyActor.actor.initialize( shape );
		
		((BoxActor)bodyActor.actor).setBreakable(this.breakable);
		((BoxActor)bodyActor.actor).setDestroyForce(this.destroyForce);
		((BoxActor)bodyActor.actor).particle = bodyActor;
	}

	public boolean updateParticle(float delta, Particle particle) {
		if( particle instanceof Particle_BodyActor == false )
			return false;
		Particle_BodyActor part = (Particle_BodyActor)particle;

		return part.actor != null;
	}

	public void dispose() {
		shape.dispose();
	}
}
