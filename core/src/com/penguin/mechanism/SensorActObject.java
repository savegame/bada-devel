package com.penguin.mechanism;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.penguin.core.PenguinGame;

/**
 * Created by savegame on 06.01.16.
 */
public class SensorActObject extends  ActionObject {

	public SensorActObject(PenguinGame penguinGame) {
		super(penguinGame);
		this.game = game;
	}

	public void init(PolygonShape poly) {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = poly;
	}
}
