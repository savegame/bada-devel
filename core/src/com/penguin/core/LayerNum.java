package com.penguin.core;

/**
 * Created by savegame on 27.09.16.
 */

public enum LayerNum {
	Background (0),
	BG         (0),
	Middle     (1),
	Top        (2),
	Foreground (2);

	private final int m_number;

	LayerNum(int number)
	{
		m_number = number;
	}

	public int n()
	{
		return m_number;
	}
}
