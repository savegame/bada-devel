package com.mypinguin.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.utils.Array;

/*
 * Created by savegame on 04.11.15.
 */
public class PlayerActor extends BodyActor {
	//Box2D
	private Fixture physicsFixture;
	private Fixture sensorFixture;
	private Fixture getRFixture;
	private Fixture getLFixture;
	private Fixture getItem = null;
	private Body    getBody = null;
	private Joint   getJoint = null;

	private Body          legsBody; //ноги - колесо
	private Fixture       legsFixture;
	private RevoluteJoint legsJoint;

	enum AnimType {
		run,
		stay,
		stop,
		jump,
		falling
	}
	//Animations
	private float     stateTime;
	private Animation animRun;    //бег
	private Animation animStay;   //стоять на месте
	private Animation animStop;   //остановка
	private Animation animJump;   //прыжок
	private Animation animFalling;//падение
	private Animation currentAnim;//текущая анимация
	//All other propertios
	private boolean   grounded    = false; //находится на земле
	private float     moveSpeed   = 400f;  //скорость передвижения
	private float     flySpeed    = 400f;  //скорость передвижения в воздухе
	private float     moveImpulse = 46f;  //ускорение хотьбы
	private float     flyImpulse  = 16f;  //ускорение в воздухе
	private float     jumpImpulse = 1512f; //импульс прыжка
	private float     bodyHeight  = 110f; //высота в пикселах
	private float     bodyDencity = 4f;
	private float     bodyPickDencity = 12f;
	//Debug
	private TextureRegion staticRight = null; //
	private TextureRegion staticFront = null; //
	private TextureRegion staticCurrent = null; //
	private TextureRegion staticPick = null;

	public enum StaticTextureType {
		front,//вид спереди
		side,//вид сбоку
		side_pick //вид сбоку держащий
	}

	enum MoveDirection {
		Left,
		Right,
		None
	}

	MoveDirection m_dir = MoveDirection.None;

	public PlayerActor( PenguinGame game, FixtureDef fixtureDef) {
		super(game, fixtureDef);
		this.setName("PlayerActor");
	}

	public PlayerActor( PenguinGame game, FixtureDef fixtureDef, TextureRegion staticFront ) {
		super(game, fixtureDef);
		setTexRegion(staticFront, StaticTextureType.front);
		this.setName("PlayerActor");
	}

	public void initialize(Shape bodyShape) {
//			fixtureDef.filter.categoryBits = Env.game.getCategoryBitsManager().getCategoryBits("level");
		//bodydef.position.set( getX() / game.units, getY() / game.units );
		float height4 = bodyHeight*0.25f;
		bodydef.angle = 0f;
		body = game.world.createBody(bodydef);

		CircleShape circle = new CircleShape();
		circle.setRadius(32f / game.units);
		circle.setPosition(new Vector2(0, height4 / game.units));
		body.createFixture(circle, 0);
		circle.dispose();

		PolygonShape poly = new PolygonShape();
		poly.setAsBox(30f/game.units, height4/game.units);
		physicsFixture = body.createFixture(poly, bodyDencity);
		physicsFixture.setFriction(0.0f);
		poly.dispose();

		poly = new PolygonShape();
		poly.setAsBox(24f / game.units, bodyHeight*0.3f / game.units, new Vector2(56f / game.units, 0f), 0f);
		getRFixture = body.createFixture(poly,0);
		getRFixture.setSensor(true);
		poly.dispose();

		poly = new PolygonShape();
		poly.setAsBox(24f / game.units, bodyHeight*0.3f / game.units, new Vector2(-56f / game.units, 0f), 0f);
		getLFixture = body.createFixture(poly,0);
		getLFixture.setSensor(true);
		poly.dispose();

		poly = new PolygonShape();
		poly.setAsBox(20f / game.units, 20f / game.units, new Vector2(0f, -(bodyHeight * 0.5f-2) / game.units), 0f);
		sensorFixture = body.createFixture(poly, 0);
		sensorFixture.setSensor(true);
		poly.dispose();

		{
			legsBody = game.world.createBody(bodydef);

			RevoluteJointDef jdef = new RevoluteJointDef();
			jdef.bodyA = body;
			jdef.bodyB = legsBody;
			jdef.collideConnected = false;
			jdef.localAnchorA.x = 0;
			jdef.localAnchorA.y = -height4/game.units;
			jdef.localAnchorB.x = 0;
			jdef.localAnchorB.y = 0;
			jdef.enableLimit = false;

			circle = new CircleShape();
			circle.setRadius(32.1f / game.units);

			legsFixture = legsBody.createFixture(circle, 1);
			legsJoint = (RevoluteJoint)game.world.createJoint(jdef);
			circle.dispose();
			legsBody.setType(BodyDef.BodyType.DynamicBody);
			legsFixture.setDensity(0.2f);
		}

		body.setBullet(true);
		body.setFixedRotation(true);
		body.setTransform(getX() / game.units, getY() / game.units, 0f);
		if( legsBody != null )
			legsBody.setTransform(getX() / game.units, (getY() - height4)/ game.units, 0f);
		fixturedef.shape = null;
	}

	/**
	 * функция setAnimation задает анимацию для определнного действия
	 * @param anim      анимация
	 * @param animType  тип анимации
	 */
	public void setAnimation( Animation anim, AnimType animType)
	{
		switch (animType) {
			case run:
				animRun = anim;
				break;
			case jump:
				animJump = anim;
				break;
			case stay:
				animStay = anim;
				break;
			case falling:
				animFalling = anim;
				break;
			case stop:
				animStop = anim;
				break;
		}
	}

	/** Функция устанавливает тестовый регион для отрисовки
	 * игрока
	 * */
	public void setTexRegion(TextureRegion staticRegion, StaticTextureType type){
		if( staticRegion == null )
			return;
		switch( type ){
			case front:
				staticFront = staticRegion;
				staticCurrent = staticRegion;
				break;
			case side:
				staticRight = staticRegion;
				break;
			case side_pick:
				staticPick = staticRegion;
				break;
		}
	}
	
	public void setMoveSpeed(float speed) {
		moveSpeed = speed;
	}
	
	public float getMoveSpeed() {
		return moveSpeed;
	}
	
	public boolean isGrounded() {
		return grounded;
	}
	
	private boolean isPlayerGrounded(float deltaTime) {
//		groundedPlatform = null;
		boolean sensor = false;
		boolean bodyToo = false;
		if( getJoint == null ) {
			getBody = null;
			getItem = null;
		}
		Array<Contact> contactList = game.world.getContactList();
		for(int i = 0; i < contactList.size; i++) {
			Contact contact = contactList.get(i);
			if( !contact.isTouching() ) continue;
			if(contact.getFixtureA() == sensorFixture || contact.getFixtureB() == sensorFixture) {
				Vector2 pos = body.getPosition();
//				WorldManifold manifold = contact.getWorldManifold();
//				for(int j = 0; j < manifold.getNumberOfContactPoints(); j++) {
//					sensor |= (manifold.getPoints()[j].y <  pos.y - 5f/game.units );
//				}
				sensor = true;
			}
			else if(getJoint == null) {
				if (contact.getFixtureA() == getRFixture || contact.getFixtureB() == getRFixture) {
					if (contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals("box")) {
						getBody = contact.getFixtureA().getBody();
						getItem = getRFixture;
					} else if (contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals("box")) {
						getBody = contact.getFixtureB().getBody();
						getItem = getRFixture;
					}
				}
				else if (contact.getFixtureA() == getLFixture || contact.getFixtureB() == getLFixture) {
					if (contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals("box")) {
						getBody = contact.getFixtureA().getBody();
						getItem = getLFixture;
					} else if (contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals("box")) {
						getBody = contact.getFixtureB().getBody();
						getItem = getLFixture;
					}
				}
			}
		}
		return sensor;
	}

	public  void jump() {
		if(false == grounded) return;
		Vector2 pos = body.getPosition();
		body.applyLinearImpulse( new Vector2(0,jumpImpulse/game.units) , pos, true);
		grounded = false;
	}

	public void pick() {
		if( getBody != null && getJoint == null) {
			getBody.setAwake(true);
			PrismaticJointDef jointDef = new PrismaticJointDef();
			float near = 64f;
			float far = 65f;
			if( getItem == getLFixture ){
				jointDef.lowerTranslation = -near/game.units;
				jointDef.upperTranslation = -far/game.units;
			}
			else {
				jointDef.lowerTranslation = near/game.units;
				jointDef.upperTranslation = far/game.units;
			}
			jointDef.localAnchorA.x = 0f;
			jointDef.localAnchorB.x = 32f/game.units;
			jointDef.localAnchorA.y = 0f;
			jointDef.localAnchorB.y = 32f/game.units;
			jointDef.bodyA = body;
			jointDef.bodyB = getBody;
			jointDef.enableLimit = true;
			jointDef.collideConnected = false;
			getJoint = game.world.createJoint(jointDef);
			PrismaticJoint joint = (PrismaticJoint)getJoint;
			physicsFixture.setDensity(bodyPickDencity);
		}
		else 	if( getJoint != null ) {
			game.world.destroyJoint(getJoint);
			if( getItem == getLFixture )
				getBody.applyForceToCenter(-125f, 0, true);
			else
				getBody.applyForceToCenter(125f, 0, true);
			getJoint = null;
			getBody = null;
			getItem = null;
			physicsFixture.setDensity(bodyDencity);
		}
	}

	public boolean canPick() {
		return  getJoint == null && getBody != null;
	}

	public boolean isPicked() {
		return getJoint != null;
	}

	public float getFriction() {
		if( physicsFixture == null )
			return 0f;
		return physicsFixture.getFriction();
	}

	@Override
	public void act(float delta) {
		stateTime += delta;
		m_dir = MoveDirection.None;
		Array<Action> arr = getActions();
		grounded = isPlayerGrounded(delta);

		if( arr.size == 0 && isGrounded() ){
			Vector2 vel = body.getLinearVelocity();
			if( vel.x > 0.1f || vel.x < -0.1f ){
				physicsFixture.setFriction(100.1f);
				legsFixture.setFriction(100.1f);
				legsJoint.setLimits(legsBody.getAngle(), legsBody.getAngle());
				legsJoint.setMaxMotorTorque(1f);
				legsJoint.enableLimit(true);
			}
		}
		else if( arr.size == 0 ) {
			physicsFixture.setFriction(0.1f);
			legsFixture.setFriction(0.1f);
//			legsJoint.setLimits(legsBody.getAngle(), legsBody.getAngle());
//			legsJoint.enableLimit(true);
		}
		else legsJoint.enableLimit(false);

		float speed = moveSpeed;
		float impulse = moveImpulse*delta;
		if(!isGrounded()) {
			speed = flySpeed;
			impulse = flyImpulse*delta;
		}

		for( Action act : arr ) {
			if( act instanceof MoveByAction ) {
				MoveByAction moveact = (MoveByAction) act;
				Vector2 vel = body.getLinearVelocity();
				Vector2 pos = body.getPosition();
				float velocity = moveact.getAmountX()*speed/game.units;

				if( moveact.getAmountX() < 0 ) {
					if( getItem != getLFixture && getJoint != null){
						PrismaticJoint joint = (PrismaticJoint)getJoint;
						joint.setLimits(-68f/game.units, -64f/game.units);
						getItem = getLFixture;
					}
					physicsFixture.setFriction(0.2f);
					legsFixture.setFriction(0.2f);
					m_dir = MoveDirection.Left;
					if (vel.x > velocity ) {
						body.applyLinearImpulse( impulse*moveact.getAmountX(), 0, pos.x, pos.y, true);
					}
					else if(vel.x < velocity ) {
						body.setLinearVelocity(velocity, body.getLinearVelocity().y);
					}
				}
				else if( moveact.getAmountX() > 0 ) {
					if( getItem != getRFixture && getJoint != null){
						PrismaticJoint joint = (PrismaticJoint)getJoint;
						joint.setLimits( 64f/game.units, 68f/game.units);
						getItem = getRFixture;
					}
					physicsFixture.setFriction(0.2f);
					legsFixture.setFriction(0.2f);
					m_dir = MoveDirection.Right;
					if (vel.x < velocity ) {
						body.applyLinearImpulse( impulse*moveact.getAmountX(), 0, pos.x, pos.y, true);
					}
					else if (vel.x > velocity ) {
						body.setLinearVelocity(velocity, body.getLinearVelocity().y);
					}
				}
			}
		}
		clearActions();
//		body.setTransform( body.getPosition(), 0 );
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		batch.setColor(1, 1, 1, parentAlpha);
		currentAnim = null;
		switch ( m_dir ){
			case Left:
				setScaleX(-1);
				if(isPicked())
					staticCurrent = staticPick;
				else
					staticCurrent = staticRight;
				if(isGrounded())
					currentAnim = animRun;
				break;
			case Right:
				setScaleX(1);
				if(isPicked())
					staticCurrent = staticPick;
				else
					staticCurrent = staticRight;
				if(isGrounded())
					currentAnim = animRun;
				break;
			case None:
				if( isGrounded() ) {
					if( isPicked() ) {
						if(getItem == getRFixture)
							setScaleX(1);
						else
							setScaleX(-1);
						staticCurrent = staticPick;
						currentAnim = animStay;
					}
					else {
						setScaleX(1);
						staticCurrent = staticFront;
						currentAnim = animStay;
					}
				}
				break;
		}

		if(currentAnim != null){
			staticCurrent = currentAnim.getKeyFrame(stateTime);
		}

		if (staticCurrent != null) {
			setSize(staticCurrent.getRegionWidth(), staticCurrent.getRegionHeight());
			setOrigin(staticCurrent.getRegionWidth() / 2, staticCurrent.getRegionHeight() / 2);
			batch.draw(staticCurrent,
							getX() - staticCurrent.getRegionWidth() / 2,
							getY() - staticCurrent.getRegionHeight() / 2,
							getOriginX(), getOriginY(),
							staticCurrent.getRegionWidth(), staticCurrent.getRegionHeight(),
							getScaleX(), getScaleY(),
							getRotation());
		}
		/*if( legsBody != null )
		{
			batch.draw(staticRight,
							getX() - staticCurrent.getRegionWidth() / 2,
							getY() - staticCurrent.getRegionHeight() / 2,
							getOriginX(), getOriginY(),
							staticCurrent.getRegionWidth(), staticCurrent.getRegionHeight(),
							getScaleX(), getScaleY(),
							legsBody.getAngle()* MathUtils.radDeg);
		}*/
	}
}
