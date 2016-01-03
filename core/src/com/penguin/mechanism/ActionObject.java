package com.penguin.mechanism;

import java.util.ArrayList;
import java.util.Iterator;

public class ActionObject extends Trigger {
	/**
	 * Тип действия
	 */
	public enum ActionType {
		Active,
		Deactive,
		Change, //Active or Deactive
		Delete,
		RemoveAfter //удалить из списка после активации
	}

	private ArrayList<ActionType> m_actions;
	private ArrayList<ActionObject> m_items;

	public ActionObject() {
		m_items = new ArrayList<ActionObject>();
	}

	@Override
	public void activate() {
		super.activate();

		Iterator<ActionObject> it = m_items.iterator();

		while( it.hasNext() ) {
			ActionObject object = it.next();
			object.activate();
		}
	}
}
