package com.pinguin.test;

import java.util.Random;


//import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

//public interface TileInterface {
//	public void draw(float x, float y);
//}

public class TileLayer extends Actor {

	public interface TileInterface {
		public void draw(float x, float y);
	}
	
	TiledMap m_tiledMap;
	
	public class SimpleTile implements TileInterface {
		@Override 
		public void draw(float x, float y) {
			
		}
	}
	
	public TileLayer(String path) {
		this.loadTexture(path);
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha) {
		for( int i = 0; i < m_mapWidth; i++ ) {
			for( int j = 0; j < m_mapHeight; j++ ) {
				int index = m_map[i][j];
				if( index > 0 ) {
					TextureRegion reg = m_tiles.get( index - 1 );
					batch.draw(reg, getX() + i*m_tileSize.x, getY() + j*m_tileSize.y);
				}
			}
		}
	}
	
	/** Функция загрузки тектуры в слой 
	 */
	public void loadTexture (String path) {
		m_texture = new Texture( path );
		int sz = m_texture.getWidth() / 16;//32
		m_tileSize = new Vector2(sz, sz);
		m_tiles = new Array<TextureRegion>(16*16);

		for( int i = 0; i < 16; i++ ) {
			for( int j = 0; j< 16; j++ ) {
				TextureRegion region = new TextureRegion(m_texture, i*sz, j*sz, sz, sz);
				m_tiles.add(region);
			}
		}
	}
	
	/** 
	 * Генерирует карту 
	 */
	public void randomMap(int width, int height) {
		m_mapWidth = width;
		m_mapHeight = height;
		m_map = new int[width][height];
		Random rand = new Random();
		int rsize = m_tiles.size;
		for( int i = 0; i < m_mapWidth; i++ ) {
			for( int j = 0; j < m_mapHeight; j++ ) {
				m_map[i][j] = rand.nextInt(rsize) - 1;
			}
		}
	}
	
	private Texture					m_texture;	//основная текстура
	private Array<TextureRegion>	m_tiles;	//массив тайлов
	private Vector2		 			m_tileSize;	//размер каждого тайла
	private int[][]					m_map;		//разметка карты 
	private int 					m_mapWidth; //ширина
	private int 					m_mapHeight;//высота
}
