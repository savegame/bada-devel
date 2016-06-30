package com.penguin.physics;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.mypinguin.game.PenguinGame;

import java.util.ArrayList;

/**
 * Created by savegame on 10.12.15.
 */
public class PlatformActor extends com.penguin.physics.BodyActor {
	enum SmoothPointType {
		all, //смазывать все точки
		ends //смазывать только крайние
	}

	public class Part {
		TextureRegion region; //текстура
		Vector2       shift; // смещение
	}

	protected ArrayList<Vector2> path = new ArrayList<Vector2>();
	protected float moveSpeed = game.units*3;
	protected Animation.PlayMode  mode = Animation.PlayMode.LOOP_PINGPONG;
	protected int nextPoint = 0;
	protected int incPoint = 1;
	protected Vector2 firstPoint = new Vector2();
	protected float nearLength = 2f;
	protected boolean smoothPoint = true; //использовать замедление перед точками
	protected float smoothDist = game.units/2;
	protected int smoothSteps = 20;
	protected SmoothPointType smoothType = SmoothPointType.ends;
	protected float currentSpeed;
	protected ArrayList<Part> parts = new ArrayList<Part>();

	public PlatformActor(PenguinGame penguinGame, FixtureDef _fixturedef) {
		super(penguinGame);
		setFixtureDef(_fixturedef);
		bodydef.type = BodyDef.BodyType.KinematicBody;
		mode = Animation.PlayMode.LOOP_PINGPONG;
	}

	public void setBodyType( BodyDef.BodyType bodyType ) {
//			bodydef.type = BodyDef.BodyType.KinematicBody;
		bodydef.type = BodyDef.BodyType.KinematicBody;
	}

	public void addTextureRegion(TextureRegion texa, float shiftX, float shiftY) {
		Part part = new Part();
		part.region = texa;
		part.shift = new Vector2(shiftX, shiftY);
		parts.add(part);
	}
	public void setMoveSpeed(float speed) {
		moveSpeed = speed;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}

	public void setMoveMode(Animation.PlayMode _mode) {
		this.mode = _mode;
	}

	public Animation.PlayMode getMoveMode() {
		return mode;
	}

	public boolean isInit() {
		return isinit;
	}

	public void setSmoothPoint( boolean smooth )
	{
		smoothPoint = smooth;
	}

	public void setSmoothDistance(float dist ) {
		if(dist < 5f)
		smoothDist = dist;
	}

	public void initialize(Shape bodyShape) {
		fixturedef.shape = bodyShape;
//			fixtureDef.filter.categoryBits = Env.game.getCategoryBitsManager().getCategoryBits("level");
		bodydef.position.set( getX() / game.units, getY() / game.units );
		firstPoint = bodydef.position;
		bodydef.angle = getRotation()/180f;
		bodydef.type = BodyDef.BodyType.KinematicBody;
		body = game.world.createBody(bodydef);
		body.createFixture(fixturedef);
		body.setUserData(this);
		fixturedef.shape = null;

		this.isinit = true;
	}

	public void setPath(Vector2 vec[]) {
		path.clear();
		for(int i = 0; i < vec.length; i ++ ) {
			path.add( new Vector2(vec[i]) );
		}
	}

	/**
	 * Действия выпоняемые объектом
	 * @param delta  время прошедшее с последнего
	 *               вызова функции
	 */
	@Override
	public void act(float delta) {
		if( !isActive() ) return;

		Vector2 bodyPos = body.getPosition();
		bodyPos.x *= game.units;
		bodyPos.y *= game.units;
		Vector2 nextPointPos;
		Vector2 len = new Vector2();

		if( path.isEmpty() ) {
//			nextPointPos = new Vector2(firstPoint.x, firstPoint.y * game.units * 5);
//			if( bodyPos.y * game.units == nextPointPos.y )
//				nextPointPos = firstPoint;
			deactivate();
		}
		else {
			switch ( mode ) {
				case NORMAL:
				case REVERSED:
				case LOOP:
				case LOOP_REVERSED:
				case LOOP_RANDOM:
				case LOOP_PINGPONG:
					if( nextPoint < path.size() )
					{
						nextPointPos = new Vector2( path.get(nextPoint) );
						len.x = nextPointPos.x - bodyPos.x;
						len.y = nextPointPos.y - bodyPos.y;
						if( len.len2() <= nearLength*nearLength ) {
							nextPoint+=incPoint;
							if(nextPoint >= path.size() ) {
								nextPoint -= 2;
								incPoint = -1;
							}
							else if( nextPoint < 0 )
							{
								nextPoint += 2;
								incPoint = 1;
							}
							nextPointPos = path.get(nextPoint);
							len.x = nextPointPos.x - bodyPos.x;
							len.y = nextPointPos.y - bodyPos.y;
						}
					}
					break;
			}
			float speed = 0;
			if( smoothPoint ) {
				float distance = len.len();
				switch (smoothType) {
					case all:
					case ends:
						if( ( 	(nextPoint == 0  && incPoint == -1 )
								|| 	(nextPoint == path.size() - 1 && incPoint == 1 )
							) && distance <= smoothDist) {
							int p = (int)(distance * smoothSteps / smoothDist);
							speed = moveSpeed / (smoothSteps * game.units) * ( (p == 0)?1:p );
//							speed = moveSpeed * distance/ (smoothDist*game.units);
						}
						else if( (nextPoint == 1 && incPoint == 1) || (nextPoint == path.size() - 2 && incPoint == -1) ) {
							Vector2 distV = new Vector2( path.get(nextPoint - incPoint).x - bodyPos.x, path.get(nextPoint - incPoint).y - bodyPos.y );
							float distance0 = distV.len();
							if( distance0 < smoothDist ) {
								int p = (int)(distance0 * smoothSteps / smoothDist);
								speed = moveSpeed / (smoothSteps * game.units) * ( (p == 0)?1:p );
//								speed = moveSpeed * distance0/ (smoothDist*game.units);
							}
							else
								speed = moveSpeed/game.units;
						}
						else
							speed = moveSpeed/game.units;
						break;
				}
			}
			else
				speed = moveSpeed/game.units;
			len = len.nor();
			currentSpeed = speed;
			Vector2 vel = new Vector2(len.x*speed, len.y*speed);
			body.setLinearVelocity(vel);
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if( game.isDebug ) {
			String text = new String();
			//Color ret = game.font.getColor();
			game.font.setColor(1f, 0.3f, 0.25f,1f);
			text = "speed = " + currentSpeed
					+ "\nActive = " + isActive();
			game.font.draw( batch, text, (int)this.getX(), (int)this.getY() + (int)this.getHeight() );
			game.font.setColor(1f,1f,1f,1f);
		}
		float length = 0;
		game.batch.setColor(1,1,1,1);
		for (Part part : parts) {
			game.batch.draw( part.region, this.getX() + length + part.shift.x, this.getY() + part.shift.y );
			length += part.region.getRegionWidth();
		}
	}
	/**
	 * Освобождение ресурсов занятых объектом
	 */
	public void dispose() {
		super.dispose();
	}

	/**
	 * Активируем объект
	 */
	@Override
	public void activate() {
		super.activate();
	}

	/**
	 * Декативирует объект
	 */
	@Override
	public void deactivate() {
		super.deactivate();
	}
}
