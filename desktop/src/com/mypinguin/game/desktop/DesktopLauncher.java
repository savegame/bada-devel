package com.mypinguin.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
//import com.mypinguin.game.MyPinguinGame;
import com.mypinguin.game.PenguinGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 480;
		config.title = "Penguins Game";
		//new LwjglApplication(new MyPinguinGame(), config);
		new LwjglApplication(new PenguinGame(), config);
	}
}
