package com.mypinguin.game.android;

//import android.os.Build;
import android.os.Bundle;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;


import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mypinguin.game.PenguinGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.hideStatusBar = true;
		config.useImmersiveMode = true; // прячем кнопки андройда!
		config.useAccelerometer = false;
		config.useCompass = false;
		//config.width = 800;
		//config.height = 480;
		//config.title = "Pinguins Game";
		
		//initialize(new MyPinguinGame(), config);
		initialize(new PenguinGame(), config);
	}
}
