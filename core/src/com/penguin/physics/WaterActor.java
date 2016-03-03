package com.penguin.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer.Random;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.mypinguin.game.PenguinGame;

/**
 * Created by savegame on 14.12.15.
 */
public class WaterActor extends com.penguin.physics.BodyActor {
	protected Fixture  waterSensor;
	private WaterController waterControl;
	protected Vector2 shift;
	protected Mesh waterMesh = null;
//	protected Renderable water;
	protected float vertices[];//массив вершин
	protected int   vertexlen;  //кол-во ячеек занимаемых одной вершиной в массиве
	protected int   columnCount;//кол-во количество колонок (вершин для волны - 1)
	protected RandomXS128 rand = new RandomXS128();
	
	protected float wave_sin_cur[], wave_sin_old[];//синус анимация поверхности
	protected float wave_old[], wave_cur[];        //симуляция колебаний
	protected float wave_height = 32.0f;
	Shader shader;

	public WaterActor(PenguinGame penguinGame, FixtureDef _fixturedef) {
		super(penguinGame);
		this.setFixtureDef(_fixturedef);
	}

	@Override
	public void initialize(Shape bodyShape) {
		if(  bodyShape instanceof PolygonShape == false )
			return;
		PolygonShape polyShape = (PolygonShape)bodyShape;
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

		columnCount = (int)(getWidth()/game.units)*2;
		
		int vertexcount = (columnCount + 1)*2;
		vertexlen = 5;
		
		float width = getWidth()/(float)columnCount;
		float height = width*2;
		int cols2 = columnCount+1;
		
		vertices = new float[vertexcount*vertexlen];
		wave_sin_cur = new float[cols2];
		wave_sin_old = new float[cols2];
		wave_old = new float[cols2];
		wave_cur = new float[cols2];
		short indicies[] = new short[columnCount*6];
		
		for(int col = 0; col < cols2; col++)
		{
			int index = col*vertexlen;	
			vertices[index    ] = getX() + width*col;//x
			wave_cur[col] = wave_old[col] = 0.0f;
			wave_sin_old[col] = wave_sin_cur[col] = 0.0f;
			vertices[index + 1] = getY() + getHeight() /*+ rand.nextFloat()*32.0f - 10.0f*/;//y
			vertices[index + 2] = Color.toFloatBits(255, 255, 255, 255);//rgba
			vertices[index + 3] = col*1.0f;//u
			vertices[index + 4] = 0.0f;//v
			int index2 = (col + cols2)*vertexlen ;
			vertices[index2    ] = getX() + width*col;//x2
			vertices[index2 + 1] = getY() + getHeight() - game.units;//y2
			vertices[index2 + 2] = Color.toFloatBits(255, 255, 255, 255);//rgba
			vertices[index2 + 3] = col*1.0f;//u
			vertices[index2 + 4] = 1.0f;//v
		}

		for(short col = 0; col < (short)columnCount; col++)
		{
			short index = (short)(col*6);
			indicies[index] = col;
			indicies[index+1] = (short)(col+1);
			indicies[index+2] = (short)(col+cols2);

			indicies[index+3] = (short)(col+cols2);
			indicies[index+4] = (short)(col+1);
			indicies[index+5] = (short)(col+cols2+1);
		}

		waterMesh = new Mesh(
				Mesh.VertexDataType.VertexArray,
				false,
				vertices.length,
				indicies.length,
				new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
		
		waterMesh.setVertices(vertices);
		waterMesh.setIndices(indicies);
	}


	public void beginContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {
		if( fixtureB.getBody().getType() == BodyDef.BodyType.DynamicBody ) {
			waterControl.addBody(fixtureB);
			
			Vector2 vec2 = fixtureB.getBody().getPosition();
			Vector2 vel2 = fixtureB.getBody().getLinearVelocity();
			if( vec2.y*game.units >= getY() + getHeight() - game.units*2 )
			{
				float x = vec2.x*game.units;
				x -= getX();
				int index = (int)((columnCount+1)*x/getWidth());
				if(index < 1)
					index = 1;
				else if( index >= columnCount )
					index = columnCount - 1;
				float tmp = vel2.y*fixtureB.getBody().getMass()/game.units;
				tmp = (tmp < -0.6f )?-1.0f:((tmp > 1/0f)?0.6f:tmp);
				wave_cur[index] += tmp;
			}
		}
	}

	public void endContact(Fixture fixtureA, Fixture fixtureB, Contact contact) {
		if( fixtureB.getBody().getType() == BodyDef.BodyType.DynamicBody ) {
			waterControl.removeBody(fixtureB);
						
			Vector2 vec2 = fixtureB.getBody().getPosition();
			Vector2 vel2 = fixtureB.getBody().getLinearVelocity();
			if( vec2.y*game.units <= getY() + getHeight() + game.units )
			{
				float x = vec2.x*game.units;
				x -= getX();
				int index = (int)((columnCount+1)*x/getWidth());
				if(index < 1)
					index = 1;
				else if( index >= columnCount )
					index = columnCount - 1;
				float tmp = vel2.y*fixtureB.getBody().getMass()/game.units;
				tmp = (tmp < -0.6f )?-1.0f:((tmp > 0.6f)?1.0f:tmp);
				wave_cur[index] += tmp;
			}
		}
	}

	public void preSolve(Contact contact, Manifold oldManifold) {
//		Fixture dynamicFixture = null;
//		if( contact.getFixtureA().getBody().getType() == BodyDef.BodyType.DynamicBody )
//			dynamicFixture = contact.getFixtureA();
//		else 
//			dynamicFixture = contact.getFixtureB();
	}

	public void postSolve(Contact contact, ContactImpulse impulse) {
//		Fixture dynamicFixture = null;
//		if( contact.getFixtureA().getBody().getType() == BodyDef.BodyType.DynamicBody )
//			dynamicFixture = contact.getFixtureA();
//		else 
//			dynamicFixture = contact.getFixtureB();
//		
//		if( dynamicFixture != null ) {
//			//узнаем вектор столкновения
//			if( impulse.getNormalImpulses().length > 1 ) {
//				float vel = impulse.getNormalImpulses()[1] * impulse.getTangentImpulses()[1];
//				
//				Vector2 vec2 = dynamicFixture.getBody().getPosition();
//				if( vec2.y*game.units <= getY() + getHeight() + game.units )
//				{
//					float x = vec2.x*game.units;
//					x -= getX();
//					int index = (int)((columnCount+1)*x/getWidth());
//					if(index < 1)
//						index = 1;
//					else if( index >= columnCount )
//						index = columnCount - 1;
//					wave_cur[index] +=  vel;
//				}
//			}
//		}
	}

	@Override
	public void act(float delta) {
		waterControl.step();
		float vis = 0.010f; //что то типа вязкости
		//тест рандомных капель на поверхность
//		if(rand.nextInt(128) % 30 == 0 )	{
//			int index = rand.nextInt( wave_cur.length -2)+1;
//			 wave_cur[index] -= 1.0f;
//		}
		//float step = 360 / (wave_cur.length - 2);
		//расчет волны
		for(int i = 1; i < wave_cur.length-1; i++ ) {
			vertices[i*vertexlen + 1] -= wave_sin_cur[i];
			wave_sin_cur[0] += delta;
			wave_sin_cur[i] = MathUtils.sinDeg(120*i - 30 + wave_sin_cur[0]*9)*wave_height*0.17f;
			vertices[i*vertexlen + 1] += wave_sin_cur[i];
			vertices[i*vertexlen + 1] += (wave_cur[i] - wave_old[i])*wave_height;
			this.waterMesh.setVertices(vertices);
			float laplas =
				(wave_cur[i-1]+wave_cur[i+1])*0.5f-wave_cur[i];
			wave_old[i] = ((2.0f-vis)*wave_cur[i]-wave_old[i]*(1.0f-vis)+laplas*delta);
			if( wave_old[i] < -1.0f )
				wave_old[i] = - 1.0f;
			else if( wave_old[i] > 1.0f )
				wave_old[i] = 1.0f;
		}
		float temp[] = wave_old;
		wave_old = wave_cur;
		wave_cur = temp;
		
//		temp = wave_sin_old;
//		wave_sin_old = wave_sin_cur;
//		wave_sin_cur = temp;
	}

	public void draw (Batch batch, float parentAlpha) {
		//super.draw(batch, parentAlpha);
		if(game.isDebug)
		{
			Vector2 pos = new Vector2(getX() + getWidth() / 2, getY() + getHeight() / 2);
			game.font.draw(batch, this.getName(), pos.x, pos.y);
		}
		{// draw mesh
//			int polycount = vertices.length;
//			if (idx == 0) return;

//			renderCalls++;
//			totalRenderCalls++;
//			int spritesInBatch = idx / 20;
//			if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
//			int count = spritesInBatch * 6;

//			lastTexture.bind();
//			game.asset.get( "pinguin.png", Texture.class).bind();
//			Mesh mesh = ;
//			mesh.setVertices(vertices, 0, vertices.length);
//			mesh.getIndicesBuffer().position(0);
//			mesh.getIndicesBuffer().limit(20);//cols*2

//			if (blendingDisabled) {
//				Gdx.gl.glDisable(GL20.GL_BLEND);
//			} else {
				//Gdx.gl.glEnable(GL20.GL_BLEND);
//				if (blendSrcFunc != -1) Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc);
//			}
			//batch.setTransformMatrix(game.camera.combined);
			//batch.
			
			game.asset.get( "defaultbg.png", Texture.class).bind();
			this.waterMesh.render(batch.getShader(), GL20.GL_TRIANGLES);
//			game.modelBatch.begin(game.camera);
//			game.modelBatch.render(this.water);
//			game.modelBatch.end();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}