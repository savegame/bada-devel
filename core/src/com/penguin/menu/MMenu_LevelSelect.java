package com.penguin.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class MMenu_LevelSelect extends Stage {

	private class LevelInfo {
		public String     image   = new String();
		public String      name   = new String();
		public String      path   = new String();
		public ImageButton button = null;
//		public boolean     tmx    = false;
		
		public boolean isEmpty() {
			return image.isEmpty() || name.isEmpty() ;
		}
	}
	
	private boolean   allLoaded = false;
	private TextButton btnBack = null;
	private MainMenuStage mmenu = null;
	private ArrayList<LevelInfo> levels = new ArrayList<LevelInfo>();
	private int cols = 4;//кол-во кнопок по горизонтали
	private int rows = 2;//кол-во кнопок по вертикали
	private int pages = 1;
	
	public MMenu_LevelSelect(Viewport viewport, Batch batch, MainMenuStage mmenu) {
		super(viewport, batch);
		this.mmenu = mmenu;
		//проверяем уровни в папке maps
		/*FileHandle[] files = Gdx.files.internal("maps/ice_map_0.tmx").parent().list();
		for(FileHandle file: files) {
			LevelInfo current = null;
			for(LevelInfo level: levels)
			{
				if( file.name().compareTo(level.name) == 0 ) {
					current = level;
					break;
				}
			}
			if( current == null ) {
				current = new LevelInfo();
//				int index = file.name().indexOf(".tmx");
//				if(  index != -1 )
//					current.name = file.name().substring(0, index );
//				else
					current.name = file.name().substring(0, file.name().length() - 4);
			}
			if( file.extension().compareToIgnoreCase("tmx") == 0 ) {
			   //файл уровня
			   current.tmx = true;
			}
			else if( file.extension().compareToIgnoreCase("png") == 0 ) {
				current.image = true;
				mmenu.loadAsset(file.path(), Texture.class);
				current.path = "maps/" + file.name();
			}
		}//*/

		loadMapList(Gdx.files.internal("maps/maps.json") );
	}

	public void loadMapList(FileHandle file) {
//		logger.info("adding default material");
//		logger.info("loading materials file");

		try {
			JsonReader reader = new JsonReader();
			JsonValue root = reader.parse(file);
			JsonValue.JsonIterator mapIt = root.iterator();

			while (mapIt.hasNext()) {
				JsonValue value = mapIt.next();

				if ( !value.has("name") || !value.has("file") || !value.has("thumb") ) {
					//logger.error("material without name");
					continue;
				}

				String name = value.getString("name");
				String fileName = value.getString("file");
				String thumbName = value.getString("thumb");

				LevelInfo current = new LevelInfo();
				current.name = name;
				current.path = "maps/" + fileName;
				current.image = "maps/" + thumbName;
			}

		} catch (Exception e) {
			//logger.error("error loading " + materialsFile.name() + " " + e.getMessage());
		}
	}

	public void buildLayout(Skin skin)
	{
		btnBack = new TextButton("BACK",skin,"button_green");
		
		btnBack.sizeBy(20, 0);
		
		btnBack.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				if (btnBack.isPressed()) {
					mmenu.OnLevelsBackPressed();	
				}
			}
		});
		
		this.addActor(btnBack);
		refreshLayout();
	}
	
	public void refreshLayout()
	{
		float width = this.getViewport().getWorldWidth();
		float height = this.getViewport().getWorldHeight();
		
		btnBack.setPosition(0.025f * width,0.025f * height);
	}
	
	public void act(float delta)
	{
		super.act(delta);
		if( !allLoaded && mmenu.game.asset.getProgress() >= 1 )
		{
			allLoaded = true;
			for(LevelInfo level: levels)
			{
				if( level.isEmpty() ) {
					if( !level.image.isEmpty() )
						mmenu.unloadAsset( level.image );
					levels.remove(level);
				}
			}
			
			int i = 0;
			float width = this.getViewport().getWorldWidth();
			float height = this.getViewport().getWorldHeight();
			float btnWidth = height*0.7f/rows*0.85f;
			float btnHeight = btnWidth;
			int crow = 0, ccol = 0;
			for(LevelInfo level: levels)
			{
				String pngPath = new String(level.image);
				level.button = new ImageButton(
						new SpriteDrawable(new Sprite( mmenu.game.asset.get(pngPath, Texture.class) )),
			            new SpriteDrawable(new Sprite( mmenu.game.asset.get(pngPath, Texture.class) ))
			        );
				level.button.setBackground(
						new SpriteDrawable(new Sprite( mmenu.game.asset.get(pngPath, Texture.class) )) 
					);
				level.button.setUserObject(level);
				
				level.button.addListener( new ChangeListener() {
					public void changed(ChangeEvent event, Actor actor) {
						ImageButton button = (ImageButton)actor;
						if (button.isPressed()) {
							selectLevel((LevelInfo)button.getUserObject());
						}
					}
				});

				level.button.setPosition(
						width*0.8f + (height*0.7f/rows)*0.85f + ccol*(width*0.8f/cols) 
						, height*0.7f + (height*0.7f/rows)*0.85f + crow*(height*0.7f/rows)
					);
				
				ccol++;
				if(ccol == cols)
				{
					ccol = 0;
					crow++;
					if( crow == rows )
					{
						crow = 0;
						pages++;
					}
				}
				
				this.addActor(level.button);
			}
			
		}
	}

	public void draw()
	{
		super.draw();
		
	}
	
	public void dispose()
	{
		super.dispose();
	}
	
	public void selectLevel(LevelInfo level) {
		if(level != null) {
			return;
		}
	}
}
