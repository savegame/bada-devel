/**
 * 
 */
package com.penguin.mechanism;

/**
 * @author savegame
 * Интерфейс активируемого объекта
 */
public interface Activable {
	/**
	 * Активируем объект 
	 */
	public void activate();
	
	/** 
	 * Декативирует объект 
	 */
	public void deactivate();
	
	/** 
	 * Возвращает статус активности
	 */
	public boolean isActive();
}
