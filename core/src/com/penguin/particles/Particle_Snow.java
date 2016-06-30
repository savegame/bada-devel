package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Particle_Snow extends Particle {

	float horizontal_speed;
	float screenScale;
	boolean solid;
	//float translation_x, translation_y;
	
	public Particle_Snow(Sprite sprite) {
		super(sprite);
		solid = false;
		//translation_x = 0;
		//translation_y = 0;
	}

}
