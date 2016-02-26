package com.penguin.physics;

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
	protected Renderable water;
	protected float vertices[];
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

		int cols = (int)(getWidth()/game.units);
		int vertexcount = (cols + 1)*2;
		int verexlen = 5;
		
		float width = getWidth()/(float)cols;
		float height = width*2;
		int cols2 = cols+1;
		
		vertices = new float[vertexcount*verexlen];
		short indicies[] = new short[cols*6];
		RandomXS128 rand = new RandomXS128();
		for(int col = 0; col < cols2; col++)
		{
			int index = col*verexlen;
			vertices[index    ] = getX() + width*col;//x
			vertices[index + 1] = getY() + getHeight() + rand.nextFloat()*32.0f - 10.0f;//y
			vertices[index + 2] = Color.toFloatBits(255, 255, 255, 255);//rgba
			vertices[index + 3] = col*1.0f;//u
			vertices[index + 4] = 0.0f;//v
			int index2 = (col + cols2)*verexlen ;
			vertices[index2    ] = getX() + width*col;//x2
			vertices[index2 + 1] = getY() + getHeight() - game.units;//y2
			vertices[index2 + 2] = Color.toFloatBits(255, 255, 255, 255);//rgba
			vertices[index2 + 3] = col*1.0f;//u
			vertices[index2 + 4] = 1.0f;//v
		}

		for(short col = 0; col < (short)cols; col++)
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
//		waterMesh.setVertices(new float[]{ getX(),getY(), Color.toFloatBits(255, 0, 0, 255) , 0, 0,
//				getX(), getY() + getHeight(), Color.toFloatBits(0, 255, 0, 255) , 1, 0,
//				getX() + getWidth(), getY(), Color.toFloatBits(0, 0, 255, 255) , 0, 1,
//				getX() + getWidth(), getY()+ getHeight(), Color.toFloatBits(0, 0, 255, 255) , 1, 1});
//
//		waterMesh.setVertices(new float[]{ 
//				getX(), getY(), Color.toFloatBits(255, 0, 0, 255) , 0, 0,
//				getX(), getY() + getHeight(), Color.toFloatBits(0, 255, 0, 255) , 1, 0,
//				getX() + getWidth(), getY(), Color.toFloatBits(0, 0, 255, 255) , 0, 1,
//				getX() + getWidth(), getY()+ getHeight(), Color.toFloatBits(0, 0, 255, 255) , 1, 1});
//		waterMesh.setIndices(new short[]{3, 1, 0});

		this.water = new Renderable();
		this.water.mesh = waterMesh;
		this.water.primitiveType = GL20.GL_TRIANGLES;
		this.water.material = new Material();
//		this.water.material.set(ColorAttribute.createDiffuse(1.0f,0.7f,0.6f,0.5f));
//		this.water.material.set(Attribute);
		//shader = new TestShader();
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
		{// draw mesh
			int polycount = vertices.length;
//			if (idx == 0) return;

//			renderCalls++;
//			totalRenderCalls++;
//			int spritesInBatch = idx / 20;
//			if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
//			int count = spritesInBatch * 6;

//			lastTexture.bind();
//			game.asset.get( "pinguin.png", Texture.class).bind();
			Mesh mesh = this.waterMesh;
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
			mesh.render(batch.getShader(), GL20.GL_TRIANGLES);
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
