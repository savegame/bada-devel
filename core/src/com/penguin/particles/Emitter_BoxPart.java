package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mypinguin.game.PenguinGame;

/**
 * Created by savegame on 19.05.16.
 */
public class Emitter_BoxPart extends BaseEmitter<Particle_BoxPart> {

	private float emissionTimer = 0;

	public Emitter_BoxPart(PenguinGame game, Sprite sprite, Class<Particle_BoxPart> particleClass) {
		super(game, sprite, particleClass);
		unlimitedGenerationEnabled = false;
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

	public void emit(float delta)
	{
		emissionTimer += delta*1000;
	}

	public void resetParticle(Particle particle) {
		particle.setPosition(getX() + (game.rand.nextFloat()-0.5f)* game.units*0.5f,
						getY() + (game.rand.nextFloat()-0.5f)*game.units*0.5f);
		particle.life = 10.0f;
		particle.setScale(game.rand.nextFloat()*3);
	}

	public boolean updateParticle(float delta, Particle particle) {

		Particle_BoxPart part = (Particle_BoxPart)particle;
		part.life -= delta;
		if( part.life <= 0 ) return false;

		return true;
	}
}
