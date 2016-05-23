package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by savegame on 19.05.16.
 */
public class Particle_BoxPart extends Particle {

	float horizontal_speed = 0.5f;
	float screenScale;
	float angle = 0f; //угол полета
	private float velocity = 5f;
//	boolean solid;
	//float translation_x, translation_y;

	public Particle_BoxPart(Sprite sprite) {
		super(sprite);
		//solid = false;
		//translation_x = 0;
		//translation_y = 0;
	}

	public float getVelocity() {
		return velocity;
	}

	public void setVelocity(float vel) {
		velocity = vel;
	}
}
