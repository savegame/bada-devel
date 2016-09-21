package com.penguin.menu;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mypinguin.game.PenguinGame;

/**
 * Created by savegame on 01.12.15.
 */
public class MainMenuStage extends ExtendedScreen {

	private boolean needUpdateViewport = true;
	private OrthographicCamera  camera = null;
	private Stage currentStage = null;
	
	private Stage main_stage = null;
	private MMenu_Options options_stage = null;
	private MMenu_LevelSelect levels_stage = null;
	
	private TextButton btnPlay = null;
	private TextButton btnOptions= null;
	private TextButton btnExit = null;

	private TextButton btnUpdateView = null;
	
	private Skin skin = null;
	private TextureAtlas buttonsAtlas = null;
	
	private TextureRegion texreg_mmenu_background = null;
	private Sprite spr_mmenu_logo = null;
	
	public MainMenuStage(PenguinGame penguinGame)
	{
		super(penguinGame);
		loadResources(game.asset);
		
		camera = new OrthographicCamera(game.width, game.height);
		camera.setToOrtho(false, game.width, game.height);
		camera.zoom = 1.0f;
		
		main_stage = new Stage( new ExtendViewport(game.width, game.height, camera), game.batch);
		options_stage = new MMenu_Options(new ExtendViewport(game.width, game.height, camera), game.batch,this);
		levels_stage = new MMenu_LevelSelect(new ExtendViewport(game.width, game.height, camera), game.batch, this);

		buildSkin();
		
		buildLayout(skin);
		options_stage.buildLayout(skin);
		levels_stage.buildLayout(skin);
		
		currentStage = main_stage;
		Gdx.input.setInputProcessor(currentStage);
	}
	
	public void buildSkin()
	{
		skin = new Skin();
		
		skin.add("default", game.font);
		
		Array<String> buttons = new Array<String>(new String[]{"green","red","blue","yellow","dumbgray"}); 
		
		for (String color : buttons)
		{
			skin.add("patch9_button_" + color, buttonsAtlas.createPatch("button_" + color));
			
			TextButtonStyle btnStyle = new TextButtonStyle();
			btnStyle.up = skin.newDrawable("patch9_button_"+color,Color.WHITE);
			btnStyle.down = skin.newDrawable("patch9_button_"+color,Color.LIGHT_GRAY);
			btnStyle.font = game.fonts.GetFont("mmenu-btn-normal");
			btnStyle.fontColor = btnStyle.font.getColor();
			
			skin.add("button_"+color, btnStyle);
		}
	}
	
	public void buildLayout(Skin skin)
	{
		btnPlay = new TextButton("PLAY",skin,"button_green");
		btnOptions = new TextButton("OPTIONS",skin,"button_blue");
		btnExit = new TextButton("EXIT",skin,"button_red");

		btnUpdateView = new TextButton("UPD",skin,"button_blue");
		btnUpdateView.sizeBy(20, 0);
		btnUpdateView.setWidth(20);
		btnUpdateView.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				if (btnUpdateView.isPressed()) OnUpdateViewPressed();
			}
		});
		
		btnOptions.sizeBy(20, 0);
		btnPlay.setWidth(btnOptions.getWidth());
		btnExit.setWidth(btnOptions.getWidth());
		btnPlay.sizeBy(20);
		
		btnPlay.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				if (btnPlay.isPressed()) OnPlayPressed();
			}
		});
		
		btnOptions.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				if (btnOptions.isPressed()) OnOptionsPressed();
			}
		});
		
		btnExit.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				if (btnExit.isPressed()) OnExitPressed();
			}
		});
		
		main_stage.addActor(btnPlay);
		main_stage.addActor(btnExit);
		main_stage.addActor(btnOptions);
		main_stage.addActor(btnUpdateView);
		
		spr_mmenu_logo.setOriginCenter();
		refreshLayout();
	}
	
	public void refreshLayout()
	{
		float width = main_stage.getViewport().getWorldWidth();
		float height = main_stage.getViewport().getWorldHeight();

		btnPlay.setWidth(width * 0.25f);
		btnPlay.setPosition(0.500f * (-btnPlay.getWidth() + width), 0.025f * height);
		btnOptions.setWidth(width * 0.25f);
		btnOptions.setPosition(0.025f * width, 0.025f * height);
		btnExit.setWidth(width * 0.25f);
		btnExit.setPosition(0.975f * (-btnExit.getWidth() + width), 0.025f * height);
		btnUpdateView.setPosition		(0.975f * (-btnUpdateView.getWidth()+ width), height- 0.025f * height - btnUpdateView.getHeight() );
		
		spr_mmenu_logo.setCenter(width * 0.5f, height * 0.8f);
	}

	public void loadResources(AssetManager manager)
	{
		this.loadAsset("ui/buttons.atlas",TextureAtlas.class);
		this.loadAsset("ui/mmenu-background.jpg", Texture.class);
		this.loadAsset("ui/mmenu-logo.png", Texture.class);
		manager.finishLoading();
		
		buttonsAtlas = manager.get("ui/buttons.atlas",TextureAtlas.class);
		texreg_mmenu_background = new TextureRegion(manager.get("ui/mmenu-background.jpg",Texture.class));
		spr_mmenu_logo = new Sprite(manager.get("ui/mmenu-logo.png",Texture.class));
	}

	/** Called when this screen becomes the current screen for a {@link Game}. */
	public void show () {

	}

	/** Called when the screen should render itself.
	 * @param delta The time in seconds since the last render. */
	public void render (float delta) {
		Gdx.gl.glClearColor(0.8f, 0.8f, 0.85f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		currentStage.act(delta);
		camera.update();

		game.batch.begin();
		game.batch.draw(texreg_mmenu_background,
						0, 0,
						main_stage.getViewport().getWorldWidth(),
						main_stage.getViewport().getWorldHeight());
			if (currentStage == main_stage)
			spr_mmenu_logo.draw(game.batch);
		game.batch.end();

		currentStage.draw();

		if(needUpdateViewport){
			main_stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			options_stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			levels_stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			//currentStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

			refreshLayout();
			options_stage.refreshLayout();
			levels_stage.refreshLayout();
			
			needUpdateViewport = false;
		}
	}

	/** @see ApplicationListener#resize(int, int) */
	public void resize (int width, int height) {
		camera.setToOrtho(false, width, height);
		needUpdateViewport = true;
	}

	/** @see ApplicationListener#pause() */
	public void pause () {

	}

	/** @see ApplicationListener#resume() */
	public void resume () {

	}

	/** Called when this screen is no longer the current screen for a {@link Game}. */
	public void hide () {

	}

	/** Called when this screen should release all resources. */
	public void dispose () {
		super.dispose();
		main_stage.dispose();
		options_stage.dispose();
		levels_stage.dispose();

		skin.dispose();
	}
	
	public void OnPlayPressed()
	{
		currentStage = levels_stage;
		Gdx.input.setInputProcessor(currentStage);
	}
	
	public void OnOptionsPressed()
	{
		currentStage = options_stage;
		Gdx.input.setInputProcessor(currentStage);
	}
	
	public void OnExitPressed()
	{
		//game.dispose();
		Gdx.app.exit();
	}

	public void OnUpdateViewPressed()
	{
		needUpdateViewport = true;
	}

	public void OnOptionsBackPressed()
	{
		currentStage = main_stage;
		Gdx.input.setInputProcessor(currentStage);
	}
	
	public void OnLevelsBackPressed()
	{
		currentStage = main_stage;
		Gdx.input.setInputProcessor(currentStage);
	}
}
