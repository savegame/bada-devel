package com.mypinguin.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by savegame on 10.11.15.
 */
public class BodyActor extends Actor {
	protected FixtureDef  fixturedef = null;
	protected BodyDef     bodydef = null;
	protected Body        body    = null;
	protected PenguinGame game   = null;
	protected boolean     isinit  = false;

	BodyActor(PenguinGame penguinGame, FixtureDef _fixturedef) {
		game = penguinGame;

		bodydef = new BodyDef();
		bodydef.type = BodyDef.BodyType.DynamicBody;

		fixturedef = _fixturedef;
	}

	public void setBodyType( BodyDef.BodyType bodyType ) {
		if(!isinit)
			bodydef.type = bodyType;
	}

	public boolean isInit() {
		return isinit;
	}

	public void initialize(Shape bodyShape) {
		fixturedef.shape = bodyShape;
//			fixtureDef.filter.categoryBits = Env.game.getCategoryBitsManager().getCategoryBits("level");
		bodydef.position.set( getX() / game.units, getY() / game.units );
		bodydef.angle = getRotation()/180f;
		body = game.world.createBody(bodydef);
		body.createFixture(fixturedef);
		body.setUserData(this);
		fixturedef.shape = null;

		isinit = true;
	}

//	/**
//	 * Действия выпоняемые объектом
//	 * @param delta  время прошедшее с последнего
//	 *               вызова функции
//	 */
//	@Override
//	public void act(float delta) {
//
//	}

	@Override
	public void draw (Batch batch, float parentAlpha) {

		setPosition( body.getPosition().x*game.units, body.getPosition().y*game.units);
		setRotation( body.getAngle() * MathUtils.radDeg );
	}
	/**
	 * Освобождение ресурсов занятых объектом
	 */
	public void dispose() {
		if(game.world != null){
			if(body != null)
				game.world.destroyBody(body);
		}
	}

	public void beginContact(Fixture fixtureB) {

	}

	public void endContact(Fixture fixtureB) {

	}

	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
}
