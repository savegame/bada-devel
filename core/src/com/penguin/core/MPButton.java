package com.penguin.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MPButton extends Actor {
	public enum ButtonType {
		LeftButton, RightButton
	}
	
	final TextureRegion region;
	private boolean pushed;
	private float regionAlpha = 0.5f;
	private ButtonType btnType;
	private float radius2;
	
	public MPButton(TextureRegion reg , float x, float y ) {
		region = reg;
		setBounds(x, y, reg.getRegionWidth(), reg.getRegionHeight());
		//setOrigin( getWidth()/2, getHeight()/2 );
		radius2 = getWidth() * getWidth()/4 - 256;
	}
	public void draw (Batch batch, float parentAlpha) {
		if( !isVisible() ) return;
		//setPosition(cam.position.x + sx, cam.position.y + sy);
		batch.setColor(new Color(1, 1, 1, regionAlpha));
		if( isPushed() ) {
			float nx = getWidth()*(0.5f - getScaleX()*0.45f);
			float ny = getHeight()*(0.5f - getScaleY()*0.45f);
			batch.draw(region, getX()+nx, getY()+ny, 0, 0, getWidth(), getHeight(), getScaleX()*0.9f, getScaleY()*0.9f, getRotation() );
		} else
			batch.draw(region, getX(), getY(), 0, 0, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation() );
	}
	
	public boolean isPushed() {
		return pushed;
	}
	
	public void setPushed( boolean push ) {
		pushed = push;
	}
	
	public void setType( ButtonType type ) {
		btnType = type;
	}

	public ButtonType getType( ) {
		return btnType;
	}

	public boolean touch(float x, float y, boolean circle) {
		if( 		   getX() <= x && getY() <= y
						&& getX() + getWidth() >= x
						&& getY() + getHeight() >= y ) {
			if(!circle)
				return true;
			else {
				float cx = x - getWidth()/2 - getX();
				float cy = y - getHeight()/2 - getY();
				Vector2 vector = new Vector2(cx,cy);
				if( vector.len2() <= radius2 )
					return true;
				else
					return false;
			}
		}
		else
			return false;
	}
}
