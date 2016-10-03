package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.penguin.core.PenguinGame;

/**
 * Created by savegame on 19.05.16.
 */
public class Emitter_BoxPart extends BaseEmitter<Particle_BoxPart> {

	private float    m_emissionTimer = 0;
	private float    angleIncrement = 360f / 10f; //часть угла
	private int      anglePart = 0; //текущая часть
	private Vector2  m_direction = new Vector2();
	private float    m_beginAngle = 0.0f;
	public  float    m_rangeAngle = 45.0f; // 30 degrees
	private float    m_rotationIncrement = 25.0f;
	public boolean   m_isSolid = true;
	public float     m_impulse = 6f;
	public float     m_particleLifeTime = 0.5f;
	public float m_particleLifeTimeRand = 0.3f; //random factor for life time

	public Emitter_BoxPart(PenguinGame game, Sprite sprite, Class<Particle_BoxPart> particleClass) {
		super(game, sprite, particleClass);
		unlimitedGenerationEnabled = false;
		angleIncrement = m_rangeAngle *0.1f;
	}

	public void setImpulseDirection( Vector2 direction ) {
		this.m_direction = new Vector2(direction);
	}

	public void act(float delta) {
		super.act(delta);
		if(particlesActive.isEmpty())
		{
			//game.particles.removeEmitter( this );
			this.clear();
			this.remove();
		}
	}

	public void emit(float delta) {
		m_emissionTimer += delta*1000;
	}

	public void resetParticle(Particle particle) {
		Particle_BoxPart part = (Particle_BoxPart)particle;
		part.setPosition(
						getX() + (game.rand.nextFloat()-0.5f)* game.units*0.5f,
						getY() + (game.rand.nextFloat()-0.5f)*game.units*0.5f
		);
		part.life = m_particleLifeTime + game.rand.nextFloat()* m_particleLifeTimeRand;
		part.setAlphaTime( 0.15f );
		float velocity = game.units * (m_impulse + game.rand.nextFloat()*0.5f);
		float randomScaleCoef = game.rand.nextFloat()*0.5f + 0.75f;
		part.setScale(
						getScaleX()*randomScaleCoef,
						getScaleY()*randomScaleCoef
		);
		part.setSolid(m_isSolid);
		//part.setRotation( angleIncrement*anglePart + game.rand.nextFloat()*angleIncrement*0.33f );
		part.setRotation( angleIncrement*(anglePart + game.rand.nextFloat()*0.33f - 0.15f) - m_rangeAngle*0.5f );
		Vector2 newDir = new Vector2(m_direction);
		part.setDirection( velocity, newDir.rotate(part.getRotation()) );
		part.setAngleVelocity( (game.rand.nextFloat()*0.5f + 0.75f)*m_rotationIncrement*(game.rand.nextBoolean()?1:-1) );
//		part.life = m_particleLifeTime;
		anglePart++;
	}

	public boolean updateParticle(float delta, Particle particle) {
		Particle_BoxPart part = (Particle_BoxPart)particle;
		return part.update(delta, game );
	}

	public void generate(int particlesCount) {
		super.generate(particlesCount);
		angleIncrement = m_rangeAngle / particlesCount;
	}
}
