package com.penguin.mechanism;

import com.penguin.physics.BodyActor;
import com.mypinguin.game.PenguinGame;

public class ActionObject extends BodyActor {
	boolean active = true;
	/**
	 * Тип действия
	 */
//	public enum ActionType {
//		Active,
//		Deactive,
//		Change, //Active or Deactive
//		Delete,
//		RemoveAfter //удалить из списка после активации
//	}

//	private ArrayList<ActionType> m_actions;
//	private ArrayList<ActionObject> m_items;

	public ActionObject(PenguinGame penguinGame) {
		super(penguinGame);
//		m_items = new ArrayList<ActionObject>();
	}

//	public void addItem(ActionObject object) {
//		m_items.add(object);
//	}
//
//	public void rmItem(ActionObject object) {
//		int index = m_items.indexOf(object);
//		if( index != -1 )
//			m_items.remove(index);
//	}

	@Override
	public void activate() {
		active = true;

//		Iterator<ActionObject> it = m_items.iterator();
//
//		while( it.hasNext() ) {
//			ActionObject object = it.next();
//			object.activate();
//		}
	}

	@Override
	public void deactivate() {
		active = false;

//		Iterator<ActionObject> it = m_items.iterator();
//
//		while( it.hasNext() ) {
//			ActionObject object = it.next();
//			object.deactivate();
//		}
	}

	@Override
	public boolean isActive() {
		return active;
	}
}
