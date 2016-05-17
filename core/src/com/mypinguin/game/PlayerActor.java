package com.mypinguin.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.utils.Array;

import java.util.HashSet;
import java.util.Set;

/*
 * Created by savegame on 04.11.15.
 */
public class PlayerActor extends com.penguin.physics.BodyActor {
	//Box2D
	private Fixture  physicsFixture;
	private Fixture  sensorFixture;
	private Fixture  getRFixture;
	private Fixture  getLFixture;
	private Fixture  getItem = null;
	private Body     getBody = null;
	private Joint    getJoint = null;
	private MassData getMass = null;
	private MassData nullMass = null;

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
	public Set<Fixture> allContacts = new HashSet<Fixture>(); //список геометрий с которыми столкнулся игрок
	private Set<Fixture> groundedFixtures; //список геометрий с которыми столкнулись ноги
	private boolean   grounded    = false; //находится на земле
	private boolean   underwater  = false; //находиться под водой
	private Vector2   realitiveMoveVel = new Vector2(); //относительная скорость (без учета скорости платформы например)
	private float     moveSpeed   = 400f;  //скорость передвижения
	private float     flySpeed    = 400f;  //скорость передвижения в воздухе
	private float     moveImpulse = 86f;  //ускорение хотьбы
	private float     flyImpulse  = 36f;  //ускорение в воздухе
	private float     jumpImpulse = 1512f; //импульс прыжка
	private float     bodyHeight  = 110f; //высота в пикселах
	private float     bodyDencity = 4f;
	private float     bodyPickDencity = 12f;
	private float     vlocityEpsilon = 0.05f; //минимальная скорость, которая приравниваеться к нулю
	private com.penguin.physics.BodyActor platform = null;//платформа на которой находиться игрок (или другой объект)
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
		super(game);
		this.setFixtureDef(fixtureDef);
		this.setName("PlayerActor");
		groundedFixtures = new HashSet<Fixture>();
		nullMass = new MassData();
		nullMass.mass = 0.5f;
	}

	public PlayerActor( PenguinGame game, FixtureDef fixtureDef, TextureRegion staticFront ) {
		super(game);
		this.setFixtureDef(fixtureDef);
		setTexRegion(staticFront, StaticTextureType.front);
		this.setName("PlayerActor");
		groundedFixtures = new HashSet<Fixture>();
		nullMass = new MassData();
		nullMass.mass = 0.5f;
	}

	public void initialize(Shape bodyShape) {
		float height4 = bodyHeight*0.25f;
		bodydef.angle = 0f;
		body = game.world.createBody(bodydef);
		body.setUserData(this);

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
			legsBody.setUserData(this);

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

	public int groundedCount() {
		return groundedFixtures.size();
	}

	public boolean isUnderwater() {
		return underwater;
	}
	
	private class MyRayCallback implements RayCastCallback {
		public boolean sensor = false;
		public Vector2 p1;
		
		MyRayCallback(boolean sens, Vector2 p) {
			sensor = sens;
			p1 = p;
		}
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal,
				float fraction) {
			if( !fixture.isSensor() && !(fixture.getBody().getUserData() instanceof com.penguin.physics.WaterActor) ) {
				Vector2 l = new Vector2( point.x - p1.x, point.y - p1.y + 1 );
				float length = l.len() ;
				if( length < 1.1f ) {
					sensor = true;
					return length;
				}
			}
			return 0;
		}
	}
	
	public boolean isPlayerGrounded() {
		boolean sensor = false;
		Array<Contact> contactList = game.world.getContactList();
		platform = null;
		for(int i = 0; i < contactList.size; i++) {
			Contact contact = contactList.get(i);
			if( !contact.isTouching() ) continue;
			if(contact.getFixtureA() == sensorFixture || contact.getFixtureB() == sensorFixture) {
				Object objA = contact.getFixtureA().getBody().getUserData();
				Object objB = contact.getFixtureB().getBody().getUserData();
				if( objA instanceof com.penguin.physics.PlatformActor || objA instanceof com.penguin.physics.BoxActor) {
					platform = (com.penguin.physics.BodyActor)objA;
				}
				else if( objB instanceof com.penguin.physics.PlatformActor || objB instanceof com.penguin.physics.BoxActor) {
					platform = (com.penguin.physics.BodyActor)objB;
				}
				sensor = true;
			}
			else if(getJoint == null) {
				if (contact.getFixtureA() == getRFixture  || contact.getFixtureA() == getLFixture) {
					if (contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals("box")) {
						getBody = contact.getFixtureB().getBody();
						getItem = contact.getFixtureA();
					}
				}
				else if (contact.getFixtureB() == getRFixture  || contact.getFixtureB() == getLFixture) {
					if (contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals("box")) {
						getBody = contact.getFixtureA().getBody();
						getItem = contact.getFixtureB();
					}
				}
			}
		}
		if(!sensor){
			//raycast
			final Vector2 p1 = new Vector2(this.getX()/game.units, this.getY()/game.units);
			Vector2 p2 = new Vector2(p1.x, p1.y - 2f);
			MyRayCallback mrcb = new MyRayCallback(sensor, p1);
			game.world.rayCast(  mrcb, p1, p2);
			sensor = mrcb.sensor; 
		}
		return sensor;
	}

	public  void jump() {
		if( !isGrounded() && !underwater ) return;
		Vector2 pos = body.getPosition();
		body.applyLinearImpulse( new Vector2(0,jumpImpulse/game.units) , pos, true);
		grounded = false;
	}

	public void pick() {
		if( getBody != null && getJoint == null) {
			getBody.setAwake(true);
			getMass = getBody.getMassData();
			getBody.setMassData(nullMass);
			getBody.setAngularVelocity(0);
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
			if( getBody.getUserData() instanceof com.penguin.physics.BoxActor) {
				((com.penguin.physics.BoxActor)getBody.getUserData()).setPicked(true);
			}
		}
		else 	if( getJoint != null ) {
			if( getBody.getUserData() instanceof com.penguin.physics.BoxActor) {
				((com.penguin.physics.BoxActor)getBody.getUserData()).setPicked(false);
			}
			game.world.destroyJoint(getJoint);
			MassData data = new MassData();
			getBody.setMassData(getMass);
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
		grounded = isPlayerGrounded();
		body.setAwake(true);

		if( arr.size == 0 && isGrounded() ){
			Vector2 vel = body.getLinearVelocity();
			if( vel.x > 0.1f || vel.x < -0.1f ){
				physicsFixture.setFriction(100.1f);
				legsFixture.setFriction(100.1f);
			}
		}
		else if( arr.size == 0 ) {
			physicsFixture.setFriction(0.1f);
			legsFixture.setFriction(0.1f);
		}
		else legsJoint.enableLimit(false);

		float speed = moveSpeed;
		float impulse = moveImpulse*delta;
		if(!isGrounded()) {
			speed = flySpeed;
			impulse = flyImpulse*delta;
		}

		Vector2 platformVel = new Vector2(0,0);
		if( platform != null ) {
			if( platform instanceof com.penguin.physics.BoxActor && ((com.penguin.physics.BoxActor)platform).isPlatformed() )
				platformVel = platform.getVelocity();
			else 
				platformVel = platform.getVelocity();
		}

		if( arr.size == 0 && isGrounded() ) {
			//Vector2 vel = body.getLinearVelocity();
			Vector2 pos = body.getPosition();
			realitiveMoveVel.y = body.getLinearVelocity().y;
			if( realitiveMoveVel.x <  - vlocityEpsilon ) { //move left
				//body.applyLinearImpulse(impulse, 0, pos.x, pos.y, true);
				realitiveMoveVel.x += speed * delta * 0.1;
				if( realitiveMoveVel.x > -vlocityEpsilon )
					realitiveMoveVel.x = 0;
			}
			else if( realitiveMoveVel.x > vlocityEpsilon ) { //move rigth
				//body.applyLinearImpulse(-impulse, 0, pos.x, pos.y, true);
				realitiveMoveVel.x -= speed * delta * 0.1;
				if( realitiveMoveVel.x < vlocityEpsilon )
					realitiveMoveVel.x = 0;
			}
			else if(realitiveMoveVel.x > - vlocityEpsilon && realitiveMoveVel.x < vlocityEpsilon ) {
				realitiveMoveVel.x = 0f;
			}
			body.setLinearVelocity(realitiveMoveVel.x + platformVel.x, realitiveMoveVel.y);
		}
		else if( isGrounded() && platform != null ) {
			for (Action act : arr) {
				if (act instanceof MoveByAction) {
					MoveByAction moveact = (MoveByAction) act;
					Vector2 vel = body.getLinearVelocity();
					Vector2 pos = body.getPosition();
					float velocity = speed / game.units;
					realitiveMoveVel.y = vel.y;
					if (moveact.getAmountX() < 0) {
						if (getItem != getLFixture && getJoint != null) {
							PrismaticJoint joint = (PrismaticJoint) getJoint;
							joint.setLimits(-68f / game.units, -64f / game.units);
							getItem = getLFixture;
						}
						physicsFixture.setFriction(0.2f);
						legsFixture.setFriction(0.2f);
						m_dir = MoveDirection.Left;

						if( realitiveMoveVel.x > 0 )
							//realitiveMoveVel.x -= speed * delta * 0.1;
							realitiveMoveVel.x = 0f;
						if ( realitiveMoveVel.x > -velocity) {
							//body.applyLinearImpulse(impulse * moveact.getAmountX(), 0, pos.x, pos.y, true);
							realitiveMoveVel.x -= speed * delta * 0.045 ;
							if( realitiveMoveVel.x < -velocity) {
								realitiveMoveVel.x = -velocity ;
							}
						}
					}
					else if (moveact.getAmountX() > 0) {
						if (getItem != getRFixture && getJoint != null) {
							PrismaticJoint joint = (PrismaticJoint) getJoint;
							joint.setLimits(64f / game.units, 68f / game.units);
							getItem = getRFixture;
						}
						physicsFixture.setFriction(0.2f);
						legsFixture.setFriction(0.2f);
						m_dir = MoveDirection.Right;

						if( realitiveMoveVel.x < 0 )
							//realitiveMoveVel.x += speed * delta * 0.1;
							realitiveMoveVel.x = 0f;
						if ( realitiveMoveVel.x < velocity) {
							//body.applyLinearImpulse(impulse * moveact.getAmountX(), 0, pos.x, pos.y, true);
							realitiveMoveVel.x += speed * delta * 0.045 ;
							if( realitiveMoveVel.x > velocity) {
								realitiveMoveVel.x = velocity ;
							}
						}
					}

					body.setLinearVelocity(realitiveMoveVel.x + platformVel.x, realitiveMoveVel.y);
				}
			}
			clearActions();
		}
		else {
			for (Action act : arr) {
				if (act instanceof MoveByAction) {
					MoveByAction moveact = (MoveByAction) act;
					Vector2 vel = body.getLinearVelocity();
					Vector2 pos = body.getPosition();
					float velocity = moveact.getAmountX() * speed / game.units;
					realitiveMoveVel.y = vel.y;
					if (moveact.getAmountX() < 0) {
						if (getItem != getLFixture && getJoint != null) {
							PrismaticJoint joint = (PrismaticJoint) getJoint;
							joint.setLimits(-68f / game.units, -64f / game.units);
							getItem = getLFixture;
						}
						physicsFixture.setFriction(0.2f);
						legsFixture.setFriction(0.2f);
						m_dir = MoveDirection.Left;
						if (vel.x > velocity) {
							body.applyLinearImpulse(impulse * moveact.getAmountX(), 0, pos.x, pos.y, true);
						} else if (vel.x < velocity) {
							body.setLinearVelocity(velocity, body.getLinearVelocity().y);
						}
					}
					else if (moveact.getAmountX() > 0) {
						if (getItem != getRFixture && getJoint != null) {
							PrismaticJoint joint = (PrismaticJoint) getJoint;
							joint.setLimits(64f / game.units, 68f / game.units);
							getItem = getRFixture;
						}
						physicsFixture.setFriction(0.2f);
						legsFixture.setFriction(0.2f);
						m_dir = MoveDirection.Right;
						if (vel.x < velocity ) {
							body.applyLinearImpulse(impulse * moveact.getAmountX(), 0, pos.x, pos.y, true);
						} else if (vel.x > velocity ) {
							body.setLinearVelocity(velocity, body.getLinearVelocity().y);
						}
					}
				}
			}
			clearActions();
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		batch.setColor( new Color(1, 1, 1, parentAlpha) );
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
						//setScaleX(1);
						//if(this.getScaleX() < 0)
							staticCurrent = staticRight;
						//staticCurrent = staticFront;
						currentAnim = animStay;
					}
				}
				break;
		}

		if(currentAnim != null){
			staticCurrent = currentAnim.getKeyFrame(stateTime);
		}

		if (staticCurrent != null) {
			float halfWidth = staticCurrent.getRegionWidth() / 2;
			float halfHeight = staticCurrent.getRegionHeight() / 2;
			//Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
//			batch.disableBlending();
			batch.draw(staticCurrent,
							getX() - halfWidth,
							getY() - halfHeight,
							halfWidth, halfHeight,
							staticCurrent.getRegionWidth(), staticCurrent.getRegionHeight(),
							getScaleX(), getScaleY(),
							getRotation());
		}
	}

	public void beginContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {
		allContacts.add(fixtureB);
		if(fixtureA == sensorFixture) {
			if( fixtureB.getBody().getUserData() instanceof com.penguin.physics.WaterActor)
				underwater = true;
			else {
				groundedFixtures.add(fixtureB);
				grounded = true;
			}
			grounded = true;
		}
	}

	public void endContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {
		allContacts.remove(fixtureB);
		if(fixtureA == sensorFixture || fixtureA == legsFixture) {
			if( fixtureB.getBody().getUserData() instanceof com.penguin.physics.WaterActor)
				underwater = false;
			else
				groundedFixtures.remove(fixtureB);
			if( groundedFixtures.isEmpty() ) {
				grounded = false;
//				if( isPlayerGrounded(0) )
//				{
//					int p = 0;
//				}
			}
		}
		else if( getJoint == null && (fixtureA == getRFixture  || fixtureA == getLFixture) )
		{
			getBody = null;
			getItem = null;
		}
	}

	public void preSolve(Contact contact, Manifold oldManifold) {
	  WorldManifold manifold = contact.getWorldManifold();
	  for(int j = 0; j < manifold.getNumberOfContactPoints(); j++) {
		Object objA = contact.getFixtureA().getBody().getUserData();
		Object objB = contact.getFixtureB().getBody().getUserData();
		if( objA instanceof com.penguin.physics.PlatformActor && objB instanceof PlayerActor ) {
			if( contact.getFixtureB().getBody() != legsBody )
				contact.setEnabled(false);
			else if( manifold.getNormal().y < 0 )
				contact.setEnabled(false);
			else
				contact.setEnabled(true);
		}
		else if( objB instanceof com.penguin.physics.PlatformActor && objA instanceof PlayerActor ) {
			if( contact.getFixtureA().getBody() != legsBody )
				contact.setEnabled(false);
			else if( manifold.getNormal().y < 0 )
				contact.setEnabled(false);
			else
				contact.setEnabled(true);
		}
	  }
	}

	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
}
