package com.penguin.menu;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.mypinguin.game.PenguinGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by savegame on 01.12.15.
 */
public class ExtendedScreen implements Screen {
	public PenguinGame 	game;

	private List<String> assetList = new ArrayList<String>();

	public ExtendedScreen( PenguinGame penguinGame )
	{
		if (penguinGame == null) throw new IllegalArgumentException("PenguinGame cannot be null.");
		this.game = penguinGame;
	}

	/** Adds the given asset to the loading queue of the AssetManager.
	 * @param fileName the file name
	 * @param type the type of the asset. */
	public synchronized <T> void loadAsset (String fileName, Class<T> type) {
		this.assetList.add(fileName);
		game.asset.load(fileName,type);
	}

	public void unloadAsset( String fileName ) {
		game.asset.unload(fileName);
		this.assetList.remove( this.assetList.indexOf(fileName) );
	}

	/** Called when this screen becomes the current screen for a {@link Game}. */
	public void show () {

	}

	/** Called when the screen should render itself.
	 * @param delta The time in seconds since the last render. */
	public void render (float delta) {

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
		for (String path : this.assetList) {
			game.asset.unload( path );
		}
	}
}
