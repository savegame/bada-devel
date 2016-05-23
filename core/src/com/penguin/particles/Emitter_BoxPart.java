package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.mypinguin.game.PenguinGame;

/**
 * Created by savegame on 19.05.16.
 */
public class Emitter_BoxPart extends BaseEmitter<Particle_BoxPart> {

	private float emissionTimer = 0;
	private float angleIncrement = 360f / 10f; //часть угла
	private int anglePart = 0; //текущая часть



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
		particle.setRotation( angleIncrement*anglePart + game.rand.nextFloat()*angleIncrement*0.33f );
		anglePart++;
	}

	public boolean updateParticle(float delta, Particle particle) {

		Particle_BoxPart part = (Particle_BoxPart)particle;
		part.life -= delta;
		if( part.life <= 0 ) return false;
		float x = part.getX() + part.getVelocity()* MathUtils.sinDeg(part.getRotation());
		float y = part.getY() + part.getVelocity()* MathUtils.cosDeg(part.getRotation());
		part.setPosition(x,y);
		return true;
	}

	public void generate(int particlesCount) {
		super.generate(particlesCount);
		angleIncrement = 360f / particlesCount;
	}
}
