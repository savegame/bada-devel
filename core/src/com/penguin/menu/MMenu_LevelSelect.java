package com.penguin.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

import javax.swing.event.ChangeEvent;

public class MMenu_LevelSelect extends Stage {

	private class LevelInfo {
		public String     image   = new String();
		public String      name   = new String();
		public String      path   = new String();
		public ImageButton button = null;
		public int page = 0;
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
	private int currentPage = 0;
	private BitmapFont font = null;

	
	public MMenu_LevelSelect(Viewport viewport, Batch batch, MainMenuStage mmenu) {
		super(viewport, batch);
		this.mmenu = mmenu;

		font = mmenu.game.fonts.GetFont("mmenu-btn-normal");
		loadMapList(Gdx.files.internal("maps/maps.json") );
	}

	public void loadMapList(FileHandle file) {
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

				mmenu.loadAsset(current.image, Texture.class);
				levels.add(current);
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
		
		btnBack.setPosition(0.025f * width, 0.025f * height);
	}
	
	public void act(float delta)
	{
		super.act(delta);
		float progress = mmenu.game.asset.getProgress();
		if( !allLoaded && progress >= 1 )
		{
			allLoaded = true;
			for(LevelInfo level: levels)
			{
				if( level.isEmpty() ) {
					if( level.image.isEmpty() )
						mmenu.unloadAsset( level.image );
					levels.remove(level);
				}
			}
			
			int i = 0;
			float width = this.getViewport().getWorldWidth();
			float height = this.getViewport().getWorldHeight();
			float btnWidth = height*0.7f/rows*0.85f;
			float btnHeight = btnWidth;
			int row = 0, col = 0, page = 0;
			for(LevelInfo level: levels)
			{
				if( page > 0 ) break;
				//String pngPath = new String(level.image);
				if( !mmenu.game.asset.isLoaded(level.image) ) {
					mmenu.loadAsset(level.image, Texture.class);
					mmenu.game.asset.finishLoadingAsset(level.image);
				}

				level.button = new ImageButton(
						new SpriteDrawable(new Sprite( mmenu.game.asset.get(level.image, Texture.class) )),
			            new SpriteDrawable(new Sprite( mmenu.game.asset.get(level.image, Texture.class) ))
			        );
				level.button.setBackground(
						new SpriteDrawable(new Sprite( mmenu.game.asset.get(level.image, Texture.class) ))
					);
				level.button.setUserObject(level);
				
				level.button.addListener(new ChangeListener() {
					public void changed(ChangeEvent event, Actor actor) {
						ImageButton button = (ImageButton) actor;
						if (button.isPressed()) {
							selectLevel((LevelInfo) button.getUserObject());
						}
					}
				});

				level.button.setPosition(10 + ((width - 20) / cols) * col, height - 10 - btnHeight - ((height - 10 - btnBack.getHeight())/rows)*row );

				level.button.setSize(btnWidth, btnWidth);

				level.page = page;

				col++;
				if(col >= cols)
				{
					col = 0;
					row ++;
					if( row >= rows )
					{
						row = 0;
						page ++;
						pages ++;
					}
				}
				
				this.addActor(level.button);
			}
		}
		else if (!allLoaded )
			mmenu.game.asset.update();
//			mmenu.game.asset.finishLoading();
	}

	public void draw()
	{
		super.draw();
		if( font == null )
			return;
		getBatch().begin();
		for(LevelInfo level: levels) {
			if(level.page < currentPage )
				continue;
			else if(level.page > currentPage )
				break;
			if(level.button != null)
				font.draw(mmenu.game.batch, level.name, level.button.getX(), level.button.getY() );
		}
		if( !allLoaded )
			font.draw(getBatch(), "LOADING ...", this.getViewport().getWorldWidth() * 0.5f, this.getViewport().getWorldHeight() * 0.5f );
		getBatch().end();
	}
	
	public void dispose()
	{
		super.dispose();
	}
	
	public void selectLevel(LevelInfo level) {
		if(level != null) {
			mmenu.game.loadLevel(level.path);
		}
	}
}
