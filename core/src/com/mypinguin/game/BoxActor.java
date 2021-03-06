package com.mypinguin.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
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
	private boolean platformed = false; //ЗНАЧИТ ПРЕДМЕТ НАХОДИТЬСЯ НА подвижной платформе
	
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
	
	/**
	* Находиться ли предмет на подвижной платформе
	* @return true если предмет на подвижной платформе
	*/
	public boolean isPlatformed() {
		return platformed;
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if(picture != null) {
			batch.setColor(new Color(1, 1, 1, parentAlpha));
			batch.draw(picture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(),
							getScaleX(), getScaleY(), getRotation());
		}
		if( game.isDebug ) {
			String str;
			str = "Picked: " + isPicked()
					+"\nPlatformed: " + isPlatformed();
			game.font.draw(batch, str, body.getWorldCenter().x*game.units, body.getWorldCenter().y*game.units);
		}
	}
	
	/**
	 * При контакте вызываеться данная функция
	 * @param fixtureA фикстура принадлежащая bodyActor
	 * @param fixtureB фикстура принадлежащая другому объекту
	 * @param contact объект контакта, на всякий случай для всего объема данных
	 */
	public void beginContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {
		Object objB = fixtureB.getBody().getUserData();
		if( objB instanceof PlatformActor ) {
			platformed = true;
		}
		else if( objB instanceof BoxActor ) {
			platformed = ((BoxActor)objB).isPlatformed();
		}
	}

	/**
	 * При окончании контакта вызываеться данная функция
	 * @param fixtureA фикстура принадлежащая bodyActor
	 * @param fixtureB фикстура принадлежащая другому объекту
	 * @param contact объект контакта, на всякий случай для всего объема данных
	 */
	public void endContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {
		Object objB = fixtureB.getBody().getUserData();
		if( objB instanceof PlatformActor ) {
			platformed = false;
		}
	}
	
	public void preSolve(Contact contact, Manifold oldManifold) {
	  WorldManifold manifold = contact.getWorldManifold();
	  for(int j = 0; j < manifold.getNumberOfContactPoints(); j++) {
			Object objA = contact.getFixtureA().getBody().getUserData();
			Object objB = contact.getFixtureB().getBody().getUserData();
			if( objA instanceof BodyActor && objB == this) {
					if( this.isPicked() ) 
						contact.setEnabled(false);
//					else if( objA instanceof PlatformActor && manifold.getNormal().y >= 0 ) {
//						
//					}
			}
			else if( objB instanceof BodyActor && objA == this ) {
				if( this.isPicked() ) 
					contact.setEnabled(false);
			}
			else 
				contact.setEnabled(true);
	  }	  
	}

	public void postSolve(Contact contact, ContactImpulse impulse) {
		if( impulse.getNormalImpulses().length >= 2 ) {
			Vector2 vec = new Vector2( impulse.getNormalImpulses()[0], impulse.getNormalImpulses()[1] );
			if( vec.len() > 10f ) {
				//body.getFixtureList().get(0).setSensor(true);
				body.setLinearVelocity(0f, 0f);
				//body.setActive(false);
			}
		}
	}
}
