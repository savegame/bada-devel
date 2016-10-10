package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.penguin.core.PenguinGame;
import com.penguin.physics.WaterActor;

/**
 * Created by savegame on 19.05.16.
 */
public class Particle_BoxPart extends Particle {
	private float m_angleVelocity = 30f; //< скорость вращения
	private float m_velocity = 10f;
	private float m_alphaTime = 1f; //< время исчезновения частицы в секундах
	private Vector2 m_direction = new Vector2();
	//	private Vector2 m_vVelocity = new Vector2();
	private boolean m_isSolid = true;
	private boolean m_isNeedRayCast = true;
	private boolean m_isUnderWater = false;
	private float   m_bouncy = 0.6f; //торможение при столкновении
	private boolean m_isActive = true;

	public boolean isActive() {
		return m_isActive;
	}

	public void setActive(boolean active) {
		m_isActive = active;
	}

	public void setNoNeedRayCast() {
		m_isNeedRayCast = false;
	}

	public void setBouncy(float bouncy) {
		m_bouncy = bouncy;
	}

	public float getBouncy() {
		return m_bouncy;
	}

	public void setUnderWater(boolean underWater) {
		m_isUnderWater = underWater;
	}

	public boolean isUnderWater() {
		return m_isUnderWater;
	}

	public void setSolid(boolean isSolid)
	{
		m_isSolid = isSolid;
	}

	private class RayCallback implements RayCastCallback {
//		public boolean sensor = false;
//		public Vector2 m_shift;
		protected Particle_BoxPart m_partile;
		protected PenguinGame m_game;

		RayCallback(Particle_BoxPart particle, PenguinGame game) {
//			sensor = sens;
			m_partile = particle;
//			m_shift = shift;
			m_game = game;
		}
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal,
																	float fraction) {
			if( fixture.getBody().getUserData() instanceof WaterActor ) {
				Vector2 l = new Vector2( point.x - m_partile.getX()/m_game.units, point.y - m_partile.getY()/m_game.units );
				float length = l.len2() ;
				if( length < 0.04f ) {
					m_partile.setDirection( 0.1f,
									m_partile.getDirection() );
					m_partile.setAngleVelocity( m_game.rand.nextFloat()*120.0f - 60.0f );
					m_partile.setUnderWater(true);
					m_partile.setBouncy( m_partile.getBouncy() * 0.1f );
					return 1;
				}
			}
			else if( !fixture.isSensor() ) {
				Vector2 l = new Vector2( point.x - m_partile.getX()/m_game.units, point.y - m_partile.getY()/m_game.units );
				float length = l.len2() ;
				if( length < 0.04f ) {
					Vector2 newDirection = new Vector2(m_partile.getDirection());
					newDirection = newDirection.add( normal.scl( normal.dot(newDirection)*-2.0f ));
					m_partile.setDirection( 1.0f,
									newDirection.scl(0.9f) );
				//m_partile.setAngleVelocity( m_partile.getAngleVelocity() * -0.9f );
//				m_partile.setNoNeedRayCast();
					return 1;
				}
			}
			else if( fixture.isSensor() )
				return 0;
			return -1;
		}
	}

	public Particle_BoxPart(Sprite sprite) {
		super(sprite);
	}

	public void setDirection( float velocity, Vector2 direction )
	{
		m_direction = direction;
		setVelocity(velocity);
	}

	public Vector2 getDirection() {
		return new Vector2(m_direction);
	}

	public float getAlphaTime() {
		return m_alphaTime;
	}

	public void setAlphaTime(float seconds) {
		m_alphaTime = seconds;
	}

	public void setAngleVelocity( float degreese )
	{
		m_angleVelocity = degreese;
	}

	public float getAngleVelocity() {
		return m_angleVelocity;
	}

	public float getVelocity() {
		return m_velocity;
	}

	public void setVelocity(float velocity) {
		m_velocity = velocity;
		m_direction.scl( m_velocity );
	}

	public boolean update(float delta, PenguinGame game)
	{
		if(!isActive())
			return false;
		Vector2 gravity = new Vector2(game.world.getGravity());
		gravity.scl(game.units * delta);

		life -= delta;
		if( life <= 0 ) return false;
		if( life <= getAlphaTime()) { //пора исчезать
			float alpha = life / getAlphaTime();
			setAlpha( alpha );
		}

		if( !isUnderWater() ) {
			m_direction.add( gravity );
		}
		else
			m_direction.add( gravity.scl(0.1f) );

		float x = getDirection().x * delta;
		float y = getDirection().y * delta;

		if(m_isSolid) {
			if( m_direction.len2() <= 1.0f ) {
				setActive(false);
				return false;
			}
			Vector2 posVec = new Vector2(getX(), getY());

			game.world.rayCast(new RayCallback(this, game),
							posVec.scl(1.0f/game.units),
							getDirection().add(getX(), getY()).scl(1.0f/game.units) );
		}
		translate(x,y);
		setRotation( getRotation() + getAngleVelocity()*delta );
		return true;
	}
}
