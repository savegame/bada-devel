package com.penguin.menu;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MMenu_Options extends Stage {

	private TextButton btnBack = null;
	private MainMenuStage mmenu = null;
	
	public MMenu_Options(Viewport viewport, Batch batch, MainMenuStage mmenu) {
		super(viewport, batch);
		this.mmenu = mmenu;
	}
	
	public void buildLayout(Skin skin)
	{
		btnBack = new TextButton("BACK",skin,"button_green");
		
		btnBack.sizeBy(20, 0);
		
		btnBack.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				if (btnBack.isPressed()) {
					mmenu.OnOptionsBackPressed();	
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
	}

	public void draw()
	{
		super.draw();
	}
	
	public void dispose()
	{
		super.dispose();
	}
}
