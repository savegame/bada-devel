package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.penguin.physics.BodyActor;

/**
 * Created by savegame on 23.05.16.
 */
public class Particle_BodyActor extends Particle {
	public BodyActor actor = null;

	public Particle_BodyActor(Sprite sprite) {
		super(sprite);
	}

	public void draw (Batch batch) {
		//блокируем какую либо отрисовку
		if( actor != null ) {
			setPosition(actor.getX(), actor.getY());
			actor.draw(batch, 1.0f );
		}
	}

	public void draw (Batch batch, float alphaModulation) {
		//блокируем отрисовку
		this.draw(batch);
	}
}
