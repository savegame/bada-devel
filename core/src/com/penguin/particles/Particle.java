package com.penguin.particles;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Particle extends Sprite
{
	public float life = 0; //время жизни частицы

	public Particle(Sprite sprite) {
		super(sprite);
	}
}
