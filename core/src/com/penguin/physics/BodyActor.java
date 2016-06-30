package com.penguin.physics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mypinguin.game.PenguinGame;
import com.penguin.mechanism.Activable;

/**
 * Created by savegame on 10.11.15.
 */
public class BodyActor extends Actor implements Activable {

	protected FixtureDef  fixturedef = null;//настройки геометрии физического тела
	protected BodyDef     bodydef = null; //настройки физщического тела по умолчанию
	protected Body        body    = null; //физическое тело
	protected PenguinGame game   = null;  //главный игровой класс
	protected boolean     isinit  = false;//объект проинициаоизирован
	protected boolean     active = true;  //объект активный или нет

	public BodyActor(PenguinGame penguinGame) {
		game = penguinGame;

		this.bodydef = new BodyDef();
		this.bodydef.type = BodyDef.BodyType.DynamicBody;

		//fixturedef = _fixturedef;
	}

	public boolean destroyBody() {
			body.setActive(false);
			game.world.destroyBody(body);
			return  true;
	}
	public void setFixtureDef(FixtureDef fixtureDef) {
		this.fixturedef = fixtureDef;
	}

	public void setBodyType( BodyDef.BodyType bodyType ) {
		if(!this.isinit)
			this.bodydef.type = bodyType;
	}

	public boolean isInit() {
		return this.isinit;
	}

	public Vector2 getVelocity() {
		return this.body.getLinearVelocity();
	}
	
	public float getMass() {
		return this.body.getMass();
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

	/**
	 * При контакте вызываеться данная функция
	 * @param fixtureA фикстура принадлежащая bodyActor
	 * @param fixtureB фикстура принадлежащая другому объекту
	 * @param contact объект контакта, на всякий случай для всего объема данных
	 */
	public void beginContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {

	}

	/**
	 * При окончании контакта вызываеться данная функция
	 * @param fixtureA фикстура принадлежащая bodyActor
	 * @param fixtureB фикстура принадлежащая другому объекту
	 * @param contact объект контакта, на всякий случай для всего объема данных
	 */
	public void endContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {

	}

	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	public void postSolve(Contact contact, ContactImpulse impulse) {

	}

	@Override
	public void activate() {
		active = true;
	}

	@Override
	public void deactivate() {
		active = false;
	}

	@Override
	public boolean isActive() {
		return active;
	}
}
