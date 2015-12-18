package com.mypinguin.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.mypinguin.game.MPButton.ButtonType;

import java.util.ArrayList;
import java.util.List;

public class MovePanel extends Actor {
	private List<MPButton> children = null;
	Actor player = null;
	
	public MovePanel(float x, float y, float width, float height ) {
		setBounds(x, y, width, height);
		setPosition(x, y);
		setTouchable(Touchable.enabled);
		children = new ArrayList<MPButton>();
		
		initialize();
	}
	
	public void draw (Batch batch, float parentAlpha) {
		for(int i = 0; i < children.size(); i++ ) {
			MPButton btn = children.get(i);
			btn.draw(batch, parentAlpha);
		}
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		for(int i = 0; i < children.size(); i++ ) {
			MPButton btn = children.get(i);
			if( btn.isPushed() ) {
				//Pushed!
				if( player != null ) {
					player.addAction(Actions.moveBy( ((btn.getType() == ButtonType.LeftButton)?-1:1), 0));
				}
			}
		}
	}
	
	public void setPlayer(Actor other){
		player = other;
	}
	
	private void initialize() {
		//setBounds(getX(), getY(), getWidth(), getHeight());
		addListener(
			new InputListener() {

				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					//boolean result = false;
					for(int i = 0; i < children.size(); i++ ) {
						MPButton btn = children.get(i);
							btn.setPushed( btn.touch(getX()+x, getY()+y,true));
					}
					return true;
				}
				
				public void touchDragged (InputEvent event, float x, float y, int pointer) {
					for(int i = 0; i < children.size(); i++ ) {
						MPButton btn = children.get(i);
						btn.setPushed( btn.touch(getX()+x, getY()+y,true));
					}
				}
				
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					for(int i = 0; i < children.size(); i++ ) {
						MPButton btn = children.get(i);
						btn.setPushed(false);
					}
				}

				/** Called when a key goes down. When true is returned, the event is {@link Event#handle() handled}. */
				public boolean keyDown (InputEvent event, int keycode) {
					for(int i = 0; i < children.size(); i++ ) {
						MPButton btn = children.get(i);
						if( (keycode == Input.Keys.A || keycode == Input.Keys.LEFT)
										&& btn.getType() == ButtonType.LeftButton ) {
							btn.setPushed(true);
						}
						else if( (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT)
										&& btn.getType() == ButtonType.RightButton ) {
							btn.setPushed(true);
						}
					}
					if( keycode == Input.Keys.W || keycode == Input.Keys.UP )
						((PlayerActor)player).jump();
					if( keycode == Input.Keys.E )
						((PlayerActor)player).pick();
					return true;
				}

				/** Called when a key goes up. When true is returned, the event is {@link Event#handle() handled}. */
				public boolean keyUp (InputEvent event, int keycode) {
					for(int i = 0; i < children.size(); i++ ) {
						MPButton btn = children.get(i);
						if( (keycode == Input.Keys.A || keycode == Input.Keys.LEFT)
										&& btn.getType() == ButtonType.LeftButton ) {
							btn.setPushed(false);
						}
						else if( (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT)
										&& btn.getType() == ButtonType.RightButton ) {
							btn.setPushed(false);
						}
					}
					return true;
				}

			}
		);
	}
	
	public void addButton( MPButton button ) {
		children.add(button);
	}
}
