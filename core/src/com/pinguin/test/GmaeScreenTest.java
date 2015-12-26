package com.pinguin.test;

//import java.util.BitSet;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mypinguin.game.CameraControl;
import com.mypinguin.game.MPButton;
import com.mypinguin.game.MovePanel;
import com.mypinguin.game.PenguinGame;
import com.mypinguin.game.Player;
import com.mypinguin.game.MPButton.ButtonType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GmaeScreenTest implements Screen {
	
	class LevelMapRenderer extends OrthogonalTiledMapRenderer {
		List<Actor> actors = null;
		ShapeRenderer renderer = null;
		OrthographicCamera camera = null;
		
		public void setCamera(OrthographicCamera cam) {
			camera = cam;
		}
		
		public LevelMapRenderer(TiledMap map) {
			super(map);
			renderer = new ShapeRenderer();
			actors = new ArrayList<Actor>();
		}
		
		@Override
		public void render (int[] layers) {
			beginRender();
			for (int layerIdx : layers) {
				MapLayer layer = map.getLayers().get(layerIdx);
				if (layer.isVisible()) {
					if (layer instanceof TiledMapTileLayer) {
						renderTileLayer((TiledMapTileLayer)layer);
					} else if (layer instanceof TiledMapImageLayer) {
						renderImageLayer((TiledMapImageLayer)layer);
					} else {
						
						renderObjects(layer);
						
					}
				}
			}
			endRender();
		}
		
		@Override
		public void renderObject(MapObject object) {
//			if( object instanceof PolygonMapObject ) {
//				PolygonMapObject polyObject = (PolygonMapObject)object;
//				//batch.begin(ShapeRenderer.ShapeType.Line);
//				renderer.begin(ShapeType.Line);
//				renderer.setColor(1, 0, 0, 1f);
//				if( camera != null )
//					renderer.setProjectionMatrix(camera.combined);
//				renderer.polygon( polyObject.getPolygon().getTransformedVertices() );
//				renderer.end();
//			}
//			else if( object instanceof RectangleMapObject ) {
//				RectangleMapObject rectObject = (RectangleMapObject)object;
//				renderer.begin(ShapeType.Line);
//				renderer.setColor(1, 0, 0, 1f);
//				if( camera != null )
//					renderer.setProjectionMatrix(camera.combined);
//				Rectangle rect = rectObject.getRectangle();
//				renderer.rect(rect.x, rect.y, rect.width, rect.height);
//				renderer.end();
//			}
		}
		
	}
	
	OrthographicCamera m_camera = null;
	CameraControl camCtrl = null;
	Player player = null;
	final PenguinGame m_game;
	AssetManager manager;

	Stage stage;
	Stage ui;
	
	InputMultiplexer m_multiplexer;
	LevelMapRenderer m_mapRenderer;
	TiledMap map;
	boolean needUpdateViewport = false;
	MovePanel panel;
	MPButton bleft;
	MPButton bright;
	
	public GmaeScreenTest(final PenguinGame game) {
		m_game = game;
		m_camera = new OrthographicCamera(m_game.width, m_game.height);
		m_camera.setToOrtho(false, m_game.width, m_game.height);
		m_camera.zoom = 2f;
		
		camCtrl = new CameraControl(m_camera);
		player = new Player(m_game);
		camCtrl.setTarget(player);
		camCtrl.setShift(0, 128);
		camCtrl.setPosition(-100,-100);
		
		m_camera.update();
		stage = new Stage( new ExtendViewport(m_game.width, m_game.height, m_camera), m_game.batch);
		ui = new Stage( new ExtendViewport(m_game.width, m_game.height), m_game.batch);
		
		manager = new AssetManager();
//		manager.load("ui/uiskin.atlas", TextureAtlas.class);
		manager.load("ui/btn_lr.png", Texture.class);
		manager.load("pinguin.png", Texture.class);
		manager.finishLoading();
		stage.addActor(player);
		stage.addActor(camCtrl);
		
		bleft = new MPButton(new TextureRegion(manager.get("ui/btn_lr.png", Texture.class), 0, 0, 128, 128), 10, 10);
		bleft.setType(ButtonType.LeftButton);
		bright = new MPButton(new TextureRegion(manager.get("ui/btn_lr.png", Texture.class), 128, 0, 128, 128), bleft.getWidth()+10, 10);
		bright.setType(ButtonType.RightButton);
		panel = new MovePanel(0, 0, m_game.width/2, m_game.height);
		panel.addButton(bleft);
		panel.addButton(bright);
		panel.setPlayer(player);
		player.setTexRegion( new TextureRegion(manager.get("pinguin.png", Texture.class)) );
		ui.addActor(panel);

		map = new TmxMapLoader().load("maps/first.tmx");
		m_mapRenderer = new LevelMapRenderer(map);
		m_mapRenderer.setCamera(m_camera);
		m_multiplexer = new InputMultiplexer();
		m_multiplexer.addProcessor(stage);
		m_multiplexer.addProcessor(ui);
		Gdx.input.setInputProcessor(m_multiplexer);
		
		MapLayer layer = map.getLayers().get("Objects");
		if(layer != null){
			for( MapObject object : layer.getObjects() ) {
				String name = object.getName();
				System.out.println( name + " :" );
				Iterator<String> iter = object.getProperties().getKeys();
				while(iter.hasNext()){
					String element = iter.next();
					System.out.println( "  " + element + " " + object.getProperties().get(element).toString());
				}
				if( name.equalsIgnoreCase("player") ) {
					float X = object.getProperties().get("x", float.class);
					float Y = object.getProperties().get("y", float.class);
					player.setPosition(X, Y);
				}
			}
		}
	}
	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.3f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		float fntX = -240;
		float fntY = 200;
		ui.act();
		stage.act();
		m_camera.update();

		m_mapRenderer.setView(m_camera);
		m_mapRenderer.render();
		stage.draw();
		ui.draw();

		m_game.batch.begin();
		m_game.batch.setProjectionMatrix(m_camera.projection);
		m_game.font.draw(m_game.batch, "no game now", fntX , fntY);
		m_game.batch.end();

		if(needUpdateViewport){
			stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			ui.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			needUpdateViewport = false;
		}
	}

	@Override
	public void resize(int width, int height) {
		m_camera.setToOrtho(false, width, height);
		
		needUpdateViewport = true;
	}

	@Override
	public void pause() {
		//saveAllGameProgress()
	}

	@Override
	public void resume() {
		//restoreGame
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		stage.dispose();
		manager.dispose();
//		skin.dispose();
	}

}
