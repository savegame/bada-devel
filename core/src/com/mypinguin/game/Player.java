package com.mypinguin.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.utils.Array;
import com.penguin.core.PenguinGame;

/*
 * Created by savegame on 04.11.15.
 */
public class Player extends Actor {
	private PenguinGame m_game = null;
	//Animations
	private Animation animRun;    //бег
	private Animation animStay;   //стоять на месте
	private Animation animStop;   //остановка
	private Animation animJump;   //прыжок
	private Animation animFalling;//падение
	//Debug
	private TextureRegion m_static = null; //

	enum MoveDirection {
		Left,
		Right,
		None
	}

	MoveDirection m_dir = MoveDirection.None;

	public Player( PenguinGame game ) {
		m_game = game;
		this.setName("PlayerActor");
	}

	public Player(PenguinGame game, TextureRegion staticR ) {
		m_game = game;
		setTexRegion(staticR);
		this.setName("PlayerActor");
	}

	/** Функция устанавливает тестовый регион для отрисовки
	 * игрока
	 * */
	public void setTexRegion(TextureRegion staticR){
		m_static = staticR;
		setBounds(0, 0, m_static.getRegionWidth(), m_static.getRegionHeight());
		setOrigin(getWidth()/2,getHeight()/2);
	}

	public void setTiledMap() {
	}

	@Override
	public void act(float delta) {
		m_dir = MoveDirection.None;
		Array<Action> arr = getActions();
		for( Action act : arr ) {
			if( act instanceof MoveByAction) {
				MoveByAction moveact = (MoveByAction) act;
				if( moveact.getAmountX() < 0 )
					m_dir = MoveDirection.Left;
				else if( moveact.getAmountX() > 0 )
					m_dir = MoveDirection.Right;
			}
		}
		super.act(delta);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		if(m_static != null) {
			batch.setColor(1, 1, 1, parentAlpha);
			float scale = getScaleX();
			switch ( m_dir ){
				case Left:
					scale *= -1;
					break;
				case None:
					scale *= 1.1;
					break;
			}
			batch.draw(m_static, getX() - getWidth()/2, getY()-25,
							getOriginX(), getOriginY(),
							getWidth(), getHeight(),
							scale, getScaleY(),
							getRotation() );
		}
	}
}
