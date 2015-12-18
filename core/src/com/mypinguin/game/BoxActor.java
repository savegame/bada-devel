package com.mypinguin.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;

/**
 * Created by savegame on 12.11.15.
 */
public class BoxActor extends BodyActor {
	private TextureRegion  picture;
	BoxActor(PenguinGame penguinGame, TextureRegion reg, FixtureDef fixturedef)
	{
		super(penguinGame, fixturedef);
		picture = reg;
		if(picture != null) {
//			setOrigin(picture.getRegionWidth() / 2, picture.getRegionHeight() / 2);
			setBounds( 0, 0, picture.getRegionWidth(), picture.getRegionHeight() );
		}
	}

	public void initialize(Shape bodyShape) {
		super.initialize(bodyShape);
		body.getFixtureList().get(0).setUserData("box");
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if(picture != null) {
			batch.setColor(new Color(1, 1, 1, parentAlpha));
			batch.draw(picture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(),
							getScaleX(), getScaleY(), getRotation());
		}
	}
}
