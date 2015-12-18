package com.penguin.menu;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.mypinguin.game.PenguinGame;

/**
 * Created by savegame on 01.12.15.
 */
public class MainMenuStage extends ExtendedScreen {

	public MainMenuStage(PenguinGame penguinGame)
	{
		super(penguinGame);
		loadResources();
	}

	public void loadResources()
	{
		//
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
	}

	/** @see ApplicationListener#resize(int, int) */
	public void resize (int width, int height) {

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
	}
}
