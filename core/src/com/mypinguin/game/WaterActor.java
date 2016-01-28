package com.mypinguin.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.penguin.physics.WaterController;

/**
 * Created by savegame on 14.12.15.
 */
public class WaterActor extends BodyActor {
	protected Fixture  waterSensor;
	private WaterController waterControl;
	protected Vector2 shift;

	public WaterActor(PenguinGame penguinGame, FixtureDef _fixturedef) {
		super(penguinGame, _fixturedef);
	}

	@Override
	public void initialize(Shape bodyShape) {
		if(  bodyShape instanceof PolygonShape == false )
			return;
		fixturedef.shape = bodyShape;
		fixturedef.isSensor = true;
		fixturedef.density = 2f;
//			fixtureDef.filter.categoryBits = Env.game.getCategoryBitsManager().getCategoryBits("level");
		bodydef.position.set( getX() / game.units, getY() / game.units );
		bodydef.angle = getRotation()/180f;
		body = game.world.createBody(bodydef);
		waterSensor = body.createFixture(fixturedef);
		body.setUserData(this);
		body.setType(BodyDef.BodyType.KinematicBody);
		fixturedef.shape = null;
		isinit = true;

		waterControl = new WaterController(game.world, waterSensor);
	}


	public void beginContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {
		if( fixtureB.getBody().getType() == BodyDef.BodyType.DynamicBody ) {
			waterControl.addBody(fixtureB);
		}
	}

	public void endContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {
		if( fixtureB.getBody().getType() == BodyDef.BodyType.DynamicBody ) {
			waterControl.removeBody(fixtureB);
		}
	}

	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	public void postSolve(Contact contact, ContactImpulse impulse) {

	}

	@Override
	public void act(float delta) {
		waterControl.step();
	}

	public void draw (Batch batch, float parentAlpha) {
		//super.draw(batch, parentAlpha);
		if(game.isDebug)
		{
			Vector2 pos = new Vector2(getX() + getWidth() / 2, getY() + getHeight() / 2);
			game.font.draw(batch, this.getName(), pos.x, pos.y);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
