package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.penguin.core.PenguinGame;

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
		Particle_BoxPart part = (Particle_BoxPart)particle;
		part.setPosition(getX() + (game.rand.nextFloat()-0.5f)* game.units*0.5f,
						getY() + (game.rand.nextFloat()-0.5f)*game.units*0.5f);
		part.life = 0.5f + game.rand.nextFloat()*0.3f;
		part.alphaTime = 0.15f;
		part.setVelocity( game.units*6 + game.rand.nextFloat()*game.units*0.5f );
		part.setScale(game.rand.nextFloat()*1.5f);
		part.setRotation( angleIncrement*anglePart + game.rand.nextFloat()*angleIncrement*0.33f );
		anglePart++;
	}

	public boolean updateParticle(float delta, Particle particle) {

		Particle_BoxPart part = (Particle_BoxPart)particle;
		part.life -= delta;
		if( part.life <= 0 ) return false;
		if( part.life <= part.alphaTime ) { //пора исчезать
			float alpha = part.life / part.alphaTime ;
			part.setAlpha( alpha );
		}
		float x = (part.getVelocity()* MathUtils.cosDeg(part.getRotation()))*delta;
		float y = (part.getVelocity()* MathUtils.sinDeg(part.getRotation()))*delta;
		part.translate(x,y);
		return true;
	}

	public void generate(int particlesCount) {
		super.generate(particlesCount);
		angleIncrement = 360f / particlesCount;
	}
}
