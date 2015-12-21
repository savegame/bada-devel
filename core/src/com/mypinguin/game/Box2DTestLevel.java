


package com.mypinguin.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.penguin.menu.ExtendedScreen;

public class Box2DTestLevel extends ExtendedScreen {
	private boolean             needUpdateViewport = false;
	private OrthographicCamera  camera             = null;
	private Stage               stage              = null;
	private CameraControl       camControl         = null;
	private InputMultiplexer    m_multiplexer      = null;
	// user inteface
	private Stage               ui                 = null;
	private MovePanel           movePanel          = null;
	private MPButton            btnLeft            = null;
	private MPButton            btnRight           = null;
	private MPButton            btnUp              = null;
	private MPButton            btnPick              = null;
	// tiled map
	OrthogonalTiledMapRenderer  m_mapRenderer      = null;
	TiledMap map;
	// Box2D physics
	private World               world              = null;
	private Box2DDebugRenderer  debugRenderer      = null;
	private float               accumulator        = 0;
	
	private float    time_stamp  = 1/30f;

	private MapBodyManager     mapBodyManager      = null;
	//randomizer
	RandomXS128 rand = new RandomXS128();
	
	
	Box2DTestLevel( PenguinGame penguinGame ) {
		super(penguinGame);
		loadTextures(game.asset);
		game.asset.finishLoading();

		camera = new OrthographicCamera(game.width, game.height);
		camera.setToOrtho(false, game.width, game.height);
		camera.zoom = 1.0f;

		camControl = new CameraControl(camera);
		camControl.setShift(0, 128);
		camControl.setPosition(game.width / 2, game.height / 2);
//		camControl.draw(null, 1f);
		camera.update();
		//b2d_matrix = new Matrix4( camera.combined.scale(b2d_scale, b2d_scale, 1f) );
		stage = new Stage( new ExtendViewport(game.width, game.height, camera), game.batch);
		ui = new Stage( new ExtendViewport(game.width, game.height), game.batch);
		createUI(ui);
		stage.addActor(camControl);
		
		map = new TmxMapLoader().load("maps/ice_map_0.tmx");
		m_mapRenderer = new OrthogonalTiledMapRenderer(map);
		//m_mapRenderer.setCamera(camera);
		m_multiplexer = new InputMultiplexer();
		m_multiplexer.addProcessor(stage);
		m_multiplexer.addProcessor(ui);
		Gdx.input.setInputProcessor(m_multiplexer);

		world = game.world;
		debugRenderer = new Box2DDebugRenderer();
		
//		createGround();
//		for(int i = 0 ; i < 10; ++i ) {
//			createRect();
//		}

//		positionPlayer(map, camControl );
		
		mapBodyManager = new MapBodyManager(game, Gdx.files.internal("PhysicsMaterials.json"), 0 );
		mapBodyManager.createPhysics(map);
		mapBodyManager.actorsToStage(stage);

		if( game.player != null )
		{
			camControl.setTarget(game.player);
			positionPlayer(map, game.player );
			movePanel.setPlayer(game.player);
			stage.addActor(game.player);
			setupPlayer(game.player);
		}
	}

	private void setupPlayer(PlayerActor player) {
		//player.setAnimation();
		if( game.asset.isLoaded("run_0.png") ) {
			Texture tex = game.asset.get("run_0.png", Texture.class );
			Array<TextureRegion> array = new Array<TextureRegion>();
			int frameW = 128;
			int frameH = 128;
			int cols = (int)(tex.getWidth() / frameW);
			int rows = (int)(tex.getHeight() / frameH);
			int frameN = 0;
			for(int j = 0; j < rows; j++ ) {
				for(int i = 0; i < cols; i++ ){
					TextureRegion region = new TextureRegion(tex, (int)i*frameW, (int)j*frameH, (int)frameW, (int)frameH);
					array.add(region);
					frameN++;
					switch(frameN){
						case 8:
							Animation anim = new Animation(0.85f/frameN, array, Animation.PlayMode.LOOP);
							player.setAnimation(anim, PlayerActor.AnimType.run);
							break;
						case 9:
							player.setTexRegion(region, PlayerActor.StaticTextureType.side);
						case 10:
							player.setTexRegion(region, PlayerActor.StaticTextureType.side_pick);
						case 11:
							player.setTexRegion(region, PlayerActor.StaticTextureType.front);
					}
				}
			}

		}
	}

	private void positionPlayer(Map map, Actor player )
	{
		MapLayer layer = map.getLayers().get("Objects");
		if(layer != null){
			for( MapObject object : layer.getObjects() ) {
				String name = object.getName();
				if( name != null &&  name.equalsIgnoreCase("player") ) {
					float X = object.getProperties().get("x", float.class);
					float Y = object.getProperties().get("y", float.class);
					player.setPosition(X, Y);
				}
			}
		}
	}
	private void createGround() {
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(0, 0);
		bodyDef.type = BodyType.StaticBody;
		
		Body body = world.createBody(bodyDef);
		PolygonShape groundShape = new PolygonShape();
		groundShape.setAsBox(game.width/game.units, 2);
		
		body.createFixture(groundShape, 0.0f);
		groundShape.dispose();
	}
	
	private void createRect() {
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set( rand.nextFloat()*(game.width/game.units-4)+2, rand.nextFloat()*(game.height/game.units-2)+5);
		bodyDef.angle = rand.nextFloat()*360f;
		bodyDef.type = BodyType.DynamicBody;
		
		Body body = world.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(2, 2);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 0.5f; 
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.2f;
		
		
		body.createFixture(fixtureDef);
		shape.dispose();
	}

	private void loadTextures(AssetManager manager) {
		this.loadAsset("ui/btn_lr.png", Texture.class);
		this.loadAsset("pinguin.png", Texture.class);
		this.loadAsset("box_0.png", Texture.class);
		this.loadAsset("run_0.png", Texture.class);
	}

	private void createUI(Stage ui_stage) {
		btnLeft = new MPButton(new TextureRegion(game.asset.get("ui/btn_lr.png", Texture.class), 0, 0, 128, 128), 10, 26);
		btnLeft.setType(MPButton.ButtonType.LeftButton);
		btnRight = new MPButton(new TextureRegion(game.asset.get("ui/btn_lr.png", Texture.class), 128, 0, 128, 128), btnLeft.getWidth() + 5, 2);
		btnRight.setType(MPButton.ButtonType.RightButton);
		movePanel = new MovePanel(0, 0, game.width/2, game.height);
		movePanel.addButton(btnLeft);
		movePanel.addButton(btnRight);

		btnUp = new MPButton(new TextureRegion(game.asset.get("ui/btn_lr.png", Texture.class), 128, 0, 128, 128), Gdx.graphics.getWidth() - 64 - 5, 5);
//		btnUp.setRotation(90);
		ui.addActor(movePanel);
		ui.setKeyboardFocus(movePanel);

		ui.addActor(btnUp);

		btnUp.addListener( new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				//boolean result = false;
				game.player.jump();
				btnUp.setPushed(true);
				return true;
			}
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				btnUp.setPushed(false);
			}
		});

		btnPick = new MPButton(new TextureRegion(game.asset.get("ui/btn_lr.png", Texture.class), 0, 0, 128, 128), Gdx.graphics.getWidth() - 256 - 5, 5);
//		btnUp.setRotation(90);;

		ui.addActor(btnPick);

		btnPick.addListener( new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				//boolean result = false;
				game.player.pick();
				btnPick.setPushed(true);
				return true;
			}
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				btnPick.setPushed(false);
			}
		});
//		movePanel.setPlayer(player);
//		player.setTexRegion(new TextureRegion(game.asset.get("pinguin.png", Texture.class)));
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.8f, 0.8f, 0.85f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		ui.act();
		stage.act();
		float frameTime = Math.min(delta, 0.25f);
		accumulator += frameTime;
//		while (accumulator >= time_stamp)
		{
			time_stamp = delta;
			world.step(time_stamp, 6, 2);
			accumulator -= time_stamp;
		}

		camControl.act(Gdx.graphics.getDeltaTime());
		camera.update();
		
		m_mapRenderer.setView(camera);
		m_mapRenderer.render();
		stage.draw();
		debugRenderer.render(world, camera.combined.scale(game.units, game.units, 1f));
		ui.draw();
//		String str = ;
		game.batch.begin();
		if( game.player != null)
			game.font.draw(game.batch, "Grounded: " + game.player.isGrounded() + "  count " + game.player.groundedCount()
							+ "\nGrounded2: " + game.player.isPlayerGrounded()
							+ "\nUnderwater: " + game.player.isUnderwater()
							+ "\nContactCount: " + game.player.allContacts.size()
							+ "\nCanPick: " + game.player.canPick()
							+ "\nVelocity: \n  X(" + game.player.getVelocity().x + ")\n  Y(" + game.player.getVelocity().y + ")"
						+ "\nFriction: " + game.player.getFriction(), 3, ui.getViewport().getWorldHeight() - 3);
		else
			game.font.draw(game.batch, "Player don't exists!", 3, ui.getViewport().getWorldHeight() - 3);

		game.batch.end();
		
		if(needUpdateViewport){
			stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			ui.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

			btnUp.setPosition(ui.getViewport().getWorldWidth() - 133, 5);
			btnPick.setPosition(ui.getViewport().getWorldWidth() - 263, 5);
			needUpdateViewport = false;
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		camera.setToOrtho(false, width, height);
		Vector2 pos = new Vector2(140f, 5f);
		needUpdateViewport = true;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
		map.dispose();
		mapBodyManager.destroyPhysics();
	}

}
