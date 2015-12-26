package com.pinguin.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

//import com.badlogic.gdx.graphics.PerspectiveCamera;


public class MyPinguinGame extends ApplicationAdapter {
	
	private class MyInput implements InputProcessor {
		private int LEFT_KEY  = 0;
		private int RIGHT_KEY = 0;
		private int UP_KEY    = 0;
		private int DOWN_KEY  = 0;
		private int ZOOM_IN   = 0;
		private int ZOOM_OUT  = 0;
		private	int TOUCH     = 0;
		//своего рода джойстик 
		private Vector2 touchBegin = new Vector2();
		private Vector2 touchCurrent = new Vector2();
		private Vector2 touchVector = new Vector2();
		
		@Override
		public boolean keyDown(int keycode) {
			if(keycode == Keys.A)
				LEFT_KEY = 1;
			if(keycode == Keys.D)
				RIGHT_KEY = 1;
			if(keycode == Keys.W)
				UP_KEY = 1;
			if(keycode == Keys.S)
				DOWN_KEY = 1;
			return true;
		}

		@Override
		public boolean keyUp(int keycode) {
			if(keycode == Keys.A)
				LEFT_KEY = 0;
			if(keycode == Keys.D)
				RIGHT_KEY = 0;
			if(keycode == Keys.W)
				UP_KEY = 0;
			if(keycode == Keys.S)
				DOWN_KEY = 0;
			
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			TOUCH = 1;
			touchBegin.x = screenX;
			touchBegin.y = screenY;
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			TOUCH = 0;
			touchCurrent.x = screenX;
			touchCurrent.y = screenY;
			touchVector.x = 0;
			touchVector.y = 0;
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			touchCurrent.x = screenX;
			touchCurrent.y = screenY;
			
			if( TOUCH == 1 )
			{
				touchVector.x = touchCurrent.x - touchBegin.x;
				touchVector.y = touchBegin.y - touchCurrent.y;
				
				if( touchVector.len2()*0.25f > camera_max_vel.y )
				{
					touchVector.setLength2(camera_max_vel.y*4);
				}
			}
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return false;
		}

		@Override
		public boolean scrolled(int amount) {
			return false;
		}
		
		public void setCamera(OrthographicCamera _camera) {
			camera = _camera;
		}
		
		public void update() {
			//обработка прикосновений
//			if( TOUCH == 1 )
//			{
//				touchVector.x = touchCurrent.x - touchBegin.x;
//				touchVector.y = touchCurrent.y - touchBegin.y;
//			}
			//обработка клавиш
			if( LEFT_KEY == 1 )
			{
	            if( camera_vel.x > -camera_max_vel.x )
	            {
	            	camera_vel.x -= camera_accel.x;
	            	if( camera_vel.x < -camera_max_vel.x )
	            		camera_vel.x = -camera_max_vel.x;
	            }
			}
			else 
			{
				if( camera_vel.x < 0 && RIGHT_KEY == 0 )
				{
					camera_vel.x += camera_accel.x + camera_accel.x;
					if( camera_vel.x > 0 )
	            		camera_vel.x = 0;
				}
			}
			
			if( RIGHT_KEY == 1 )
			{
	            if( camera_vel.x < camera_max_vel.x )
	            {
	            	camera_vel.x += camera_accel.x;
	            	if( camera_vel.x > camera_max_vel.x )
	            		camera_vel.x = camera_max_vel.x;
	            }
			}
			else 
			{
				if( camera_vel.x > 0 )
				{
					camera_vel.x -= camera_accel.x + camera_accel.x;
					if( camera_vel.x < 0 && LEFT_KEY == 0 )
	            		camera_vel.x = 0;
				}
			}
			
			if( DOWN_KEY == 1 )
			{
	            if( camera_vel.y > -camera_max_vel.x )
	            {
	            	camera_vel.y -= camera_accel.y;
	            	if( camera_vel.y < -camera_max_vel.x )
	            		camera_vel.y = -camera_max_vel.x;
	            }
			}
			else 
			{
				if( camera_vel.y < 0 && UP_KEY == 0 )
				{
					camera_vel.y += camera_accel.y + camera_accel.y;
					if( camera_vel.y > 0 )
	            		camera_vel.y = 0;
				}
			}
			
			if( UP_KEY == 1 )
			{
	            if( camera_vel.y < camera_max_vel.x )
	            {
	            	camera_vel.y += camera_accel.y;
	            	if( camera_vel.y > camera_max_vel.x )
	            		camera_vel.y = camera_max_vel.x;
	            }
			}
			else 
			{
				if( camera_vel.y > 0 )
				{
					camera_vel.y -= camera_accel.y + camera_accel.y;
					if( camera_vel.y < 0 && DOWN_KEY == 0 )
	            		camera_vel.y = 0;
				}
			}
			
			if( camera != null )
			{
				if ( !touchVector.isZero() && TOUCH == 1 )
					camera.translate(touchVector.x * camera.zoom * 0.5f, touchVector.y * camera.zoom * 0.5f);
				else if( !camera_vel.isZero() )
					camera.translate(camera_vel.x * camera.zoom, camera_vel.y * camera.zoom);
			}
		}
		
		private Vector2   camera_vel = new Vector2(0,0);
		private Vector2   camera_accel = new Vector2(0.5f,0.5f);
		private Vector2   camera_max_vel = new Vector2(10,100);
		private OrthographicCamera camera = null;
	}
	
	SpriteBatch        batch;
	Texture            img;
	//TileLayer          m_layer;
	OrthographicCamera m_camera;
	MyInput            m_input;
	TiledMap           m_tiledMap;
	TiledMapRenderer   m_tmRender;
	
	@Override
	public void create () {
		float width = 800;//Gdx.graphics.getWidth();
		float height = 480;//Gdx.graphics.getHeight();
		batch = new SpriteBatch();
		
		m_input = new MyInput();
		
		m_camera = new OrthographicCamera(width, height);
		m_camera.setToOrtho(false,width,height);
		m_camera.update();
		//m_camera.translate(128, 128);
		//m_camera.position
		m_input.setCamera(m_camera);
		m_camera.zoom = 2.0f;
		
		img = new Texture("badlogic.jpg");
		//m_layer = new TileLayer("tiles.png");
		//m_layer.randomMap(16, 10);
		
		m_tiledMap = new TmxMapLoader().load("maps/test.tmx");
		m_tmRender = new OrthogonalTiledMapRenderer(m_tiledMap);
		
		Gdx.input.setInputProcessor(m_input);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.75f, 0.75f, 1, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		m_input.update();
		m_camera.update();
		m_tmRender.setView(m_camera);
		m_tmRender.render();
		batch.setProjectionMatrix(m_camera.projection);
		batch.begin();
		Vector3 posn = new Vector3(10,10,0);
		Vector3 pos = m_camera.project(posn);
		batch.draw(img, pos.x, pos.y );
		batch.end();
	}
	
	@Override 
	public void resize(int width, int height) {
//		if( m_camera != null )
//		{
//			m_camera.setToOrtho(false,width,width);
//			m_camera.update();
//		}
	}

	@Override 
	public void pause() {
	}
	
	@Override 
	public void resume() {
	}

	@Override 
	public void dispose()
	{
		batch.dispose();
		img.dispose();
		m_tiledMap.dispose();
	}
}
