package com.mypinguin.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.WorldManifold;

/**
 * Created by savegame on 12.11.15.
 */
public class BoxActor extends BodyActor {
	private TextureRegion  picture;
	private boolean picked = false; //значит что предмет находиться в руках 
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

	/**
	 * Находиться ли данный предмет в руках персонажа
	 * @return
	 */
	public boolean isPicked() {
		return picked;
	}
	
	/** 
	 * @param pick объект захвачен или нет
	 */
	public void setPicked(boolean pick) {
		picked = pick;
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
	
	
	public void preSolve(Contact contact, Manifold oldManifold) {
	  WorldManifold manifold = contact.getWorldManifold();
	  for(int j = 0; j < manifold.getNumberOfContactPoints(); j++) {
			Object objA = contact.getFixtureA().getBody().getUserData();
			Object objB = contact.getFixtureB().getBody().getUserData();
			if( objA instanceof BodyActor && this.isPicked() && objB == this) {
					contact.setEnabled(false);
			}
			else if( objB instanceof BodyActor && this.isPicked() && objA == this ) {
				contact.setEnabled(false);
			}
			else 
				contact.setEnabled(true);
	  }	  
	}

	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
}
