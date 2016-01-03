package com.penguin.mechanism;

public class Trigger {
	private boolean  active;
	
	/**
	 * Активирует триггер
	 */
	public void activate() {
		active = true;
	}
	
	/** 
	 * Деуктивирует триггер
	 */
	public void deactivate() {
		active = false;
	}
	
	/**
	 * Возращает true если триггер активный
	 * @return активность триггера
	 */
	public boolean isActive() {
		return active;
	}
}
