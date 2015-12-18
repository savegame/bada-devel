package com.pinguin.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mypinguin.game.PenguinGame;
import com.penguin.menu.ExtendedScreen;

public class MainMenuScreen extends ExtendedScreen {
	OrthographicCamera m_camera = null;
	boolean  needCameraRefresh;
	Stage ui;

	public MainMenuScreen(PenguinGame pgame) {
		super(pgame);
		// создаем ортоганальную камеру
		m_camera = new OrthographicCamera(game.width, game.height);
		m_camera.setToOrtho(false, game.width, game.height);
		m_camera.translate(game.width, game.height / 2);
		ui = new Stage( new ExtendViewport(game.width, game.height, m_camera) );
	}
	@Override
	public void show() {
		// TODO Auльo-generated method stub

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		float fntX = 15;
		float fntY = ui.getViewport().getWorldHeight()/2;
		m_camera.update();
		game.batch.begin();
		game.batch.setProjectionMatrix(m_camera.combined);
		String text = "Penguin Game";
						//game.font.get
		game.font.draw(game.batch, "Penguin Game", fntX + 140, fntY + (game.font.getLineHeight()+2)*4);
		game.font.draw(game.batch, "Play", fntX + 270 , fntY + game.font.getLineHeight()+15);
		game.font.draw(game.batch, "Exit", fntX + 275, fntY);
		game.batch.end();
		if( needCameraRefresh ) {
			//m_camera.setToOrtho(false, ui.getViewport().getWorldWidth(),ui.getViewport().getWorldHeight());
			needCameraRefresh = false;
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		m_camera.setToOrtho( false, width, height );
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
		ui.dispose();
	}

}
