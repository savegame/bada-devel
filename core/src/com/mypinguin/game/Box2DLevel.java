


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
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.penguin.menu.ExtendedScreen;
import com.penguin.particles.Emitter_Snow;

public class Box2DLevel extends ExtendedScreen {
	private String              m_mapPath          = new String();
	private boolean             m_isMapLoaded      = false;
	private boolean             m_isResourcesLoaded = false;
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
	TiledMap                    map;
	// Box2D physics
	private World               world              = null;
	private Box2DDebugRenderer  debugRenderer      = null;
	private float               accumulator        = 0;
	
	private float              time_stamp  = 1/30f;

	private MapBodyManager     mapBodyManager      = null;
	private Texture            defaultBG           = null;
	private Group              background          = null;
	private Group              middleground        = null;
	private Group              waterlayer          = null;
	private Group              foreground          = null;
	private Emitter_Snow       snowEmitter         = null;
	//randomizer

	
	
	Box2DLevel(PenguinGame penguinGame ) {
		super(penguinGame);
		loadTextures(game.asset);
//		game.asset.finishLoading();
		game.isDebug = false;

		camera = new OrthographicCamera(game.width, game.height);
		camera.setToOrtho(false, game.width, game.height);
		camera.zoom = 1.0f;
		game.camera = this.camera;

		camControl = new CameraControl(camera);
		camControl.setShift(0, 128);
		camControl.setPosition(game.width / 2, game.height / 2);
		camera.update();

		stage = new Stage( new ExtendViewport(game.width, game.height, camera), game.batch);
		background = new Group();
		background.setName("BackgroundLayer");
		middleground = new Group();
		middleground.setName("MiddleLayer");
		waterlayer = new Group();
		waterlayer.setName("WaterLayer");
		foreground = new Group();
		foreground.setName("ForegroundLayer");
		stage.addActor(background);
		stage.addActor(middleground);
		stage.addActor(waterlayer);
		stage.addActor(foreground);

		ui = new Stage( new ExtendViewport(game.width, game.height), game.batch);

		background.addActor(camControl);

		m_multiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(m_multiplexer);

		m_multiplexer.addProcessor(stage);
		m_multiplexer.addProcessor(ui);

		world = game.world;
		debugRenderer = new Box2DDebugRenderer();
		mapBodyManager = new MapBodyManager(game, Gdx.files.internal("PhysicsMaterials.json"), 0 );
	}

	private boolean loadMapPrivate(String path)
	{
		mapBodyManager.dispose();
		if(m_mapRenderer != null)
			m_mapRenderer.dispose();
		if(map != null)
			map.dispose();

		map = new TmxMapLoader().load(path);
		m_mapRenderer = new OrthogonalTiledMapRenderer(map);

		mapBodyManager.createPhysics(map);
		mapBodyManager.actorsToStage(middleground);

		if( game.player != null )
		{
			camControl.setTarget(game.player);
			positionPlayer(map, game.player);
			movePanel.setPlayer(game.player);
			middleground.addActor(game.player);
			setupPlayer(game.player);
		}

		mapBodyManager.waterActorToStage(waterlayer);

		Array<Actor> actors = waterlayer.getChildren();
		for (int i = 0; i < actors.size; i++)
		{
			if (actors.items[i] instanceof com.penguin.physics.WaterActor)
			{
				com.penguin.physics.WaterActor water = (com.penguin.physics.WaterActor) actors.items[i];
				snowEmitter.AddSolidRegion(water.getX(),water.getY(),water.getWidth(),water.getHeight());
			}
		}

		snowEmitter.SetPlayerActor(game.player);
		return false;
	}

	public boolean loadMap(String path)
	{
		m_isMapLoaded = false;
		m_mapPath = path;
		return true;
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

	private void loadTextures(AssetManager manager) {
//		this.loadAsset("ui/btn_lr.png", Texture.class);
		game.asset.load("ui/btn_lr.png", Texture.class);
		game.asset.load("ui/btn_jump.png", Texture.class);
		game.asset.load("ui/btn_hand.png", Texture.class);
//		this.loadAsset("pinguin.png", Texture.class);
		game.asset.load("run_0.png", Texture.class);
		this.loadAsset("defaultbg.png", Texture.class);
		this.loadAsset("water.png", Texture.class);
	}

	private void createUI(Stage ui_stage) {
		btnLeft = new MPButton(new TextureRegion(game.asset.get("ui/btn_lr.png", Texture.class), 0, 0, 128, 128), 10, 26);
		btnLeft.setType(MPButton.ButtonType.LeftButton);
		btnRight = new MPButton(new TextureRegion(game.asset.get("ui/btn_lr.png", Texture.class), 128, 0, 128, 128), btnLeft.getWidth() + 5, 2);
		btnRight.setType(MPButton.ButtonType.RightButton);
		movePanel = new MovePanel(0, 0, game.width/2, game.height);
		movePanel.addButton(btnLeft);
		movePanel.addButton(btnRight);

		btnUp = new MPButton(new TextureRegion(game.asset.get("ui/btn_jump.png", Texture.class), 0, 0, 128, 128), Gdx.graphics.getWidth() - 64 - 5, 5);
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

		btnPick = new MPButton(new TextureRegion(game.asset.get("ui/btn_hand.png", Texture.class), 0, 0, 128, 128), Gdx.graphics.getWidth() - 256 - 5, 5);
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

	}

	@Override
	public void render(float delta) {
		if( m_isResourcesLoaded == false || m_isMapLoaded == false ) {
			game.asset.update();

			Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

			game.batch.begin();
			game.font.draw(game.batch, "Loading " + (int)(game.asset.getProgress()*100) + "%",
							ui.getViewport().getWorldWidth()*0.5f,
							(ui.getViewport().getWorldHeight() - game.font.getLineHeight())*0.5f);
			game.batch.end();

			if( m_isResourcesLoaded == false && game.asset.getProgress() == 1.0f)
			{
				m_isResourcesLoaded = true;

				defaultBG = game.asset.get("defaultbg.png", Texture.class);
				createUI(ui);

				snowEmitter = new Emitter_Snow(game, camera);
				game.particles.addEmitter(snowEmitter, 0);
				foreground.addActor(game.particles.getLayer(0));
				middleground.addActor(game.particles.getLayer(1));
			}

			if( m_isMapLoaded == false && game.asset.getProgress() == 1.0f)
			{
				m_isMapLoaded = true;
				loadMapPrivate(m_mapPath);
			}
			return;
		}
		else
		{
			Gdx.gl.glClearColor(0.8f, 0.8f, 0.85f, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

			//render world
			ui.act();
			stage.act();

			float frameTime = Math.min(delta, 0.25f);
			accumulator += frameTime;
//		while (accumulator >= time_stamp)
			{
				time_stamp = delta;
				world.step(time_stamp, 6, 2);
				game.destroyBodies();
				accumulator -= time_stamp;
			}

			camControl.act(Gdx.graphics.getDeltaTime());
			camera.update();

			game.batch.begin();
			game.batch.draw(defaultBG, 0, 0, ui.getViewport().getWorldWidth(), ui.getViewport().getWorldHeight());
			game.batch.end();

			m_mapRenderer.setView(camera);
			m_mapRenderer.render();

			stage.draw();
			if (game.isDebug)
				debugRenderer.render(world, camera.combined.scale(game.units, game.units, 1f));
			ui.draw();
//		String str = ;
			game.batch.begin();
			if (game.isDebug) {
				if (game.player != null)
					game.font.draw(game.batch, "Grounded: " + game.player.isGrounded() + "  count " + game.player.groundedCount()
									+ "\nUnderwater: " + game.player.isUnderwater()
									+ "\nContactCount: " + game.player.allContacts.size()
									+ "\nCanPick: " + game.player.canPick()
									+ "\nVelocity: \n  X(" + game.player.getVelocity().x + ")\n  Y(" + game.player.getVelocity().y + ")"
									+ "\nFriction: " + game.player.getFriction(), 3, ui.getViewport().getWorldHeight() - 3);
				else
					game.font.draw(game.batch, "Player don't exists!", 3, ui.getViewport().getWorldHeight() - 3);
			}
			game.batch.end();
		}
		
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
		camera.setToOrtho(false, width, height);
//		Vector2 pos = new Vector2(140f, 5f);
		needUpdateViewport = true;
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {
		//
	}

	@Override
	public void hide() {
		//
	}

	@Override
	public void dispose() {
		super.dispose();
		map.dispose();
		mapBodyManager.dispose();
		game.particles.clearLayers();
	}

}
