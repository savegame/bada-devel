package com.penguin.core;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;

/**
 * Created by savegame on 29.09.16.
 */

public class SoundManager implements Disposable {
	private class Item {
		public Sound  m_sound = null;
		public String m_filename = new String();

		Item() { }

		Item(Sound sound, String filename)
		{
			m_filename = filename;
			m_sound = sound;
		}

		Item( Item other ) {
			m_sound = other.m_sound;
			m_filename = other.m_filename;
		}
	}

	protected PenguinGame    m_game = null;
	private ArrayList<Item>  m_soundList;

	SoundManager(PenguinGame game)
	{
		this.m_game = game;
	}

	@Override
	public void dispose() {
		for (Item item : m_soundList ) {
			if( m_game.asset.isLoaded( item.m_filename ) )
			{
				item.m_sound.dispose();
				m_game.asset.unload(item.m_filename);
			}
		}
	}
}
