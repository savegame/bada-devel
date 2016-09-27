package com.penguin.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class FontsManager {

	Array<FontStyleLoaded> stylesLibrary = null;
	
	public FontsManager() {
	}
	
	public void dispose()
	{
		for (FontStyleLoaded style : stylesLibrary)
		{
			style.bitmapFont.dispose();
		}
		stylesLibrary.clear();
	}
	
	public BitmapFont GetFont(String styleName)
	{
		for (FontStyleLoaded style : stylesLibrary)
		{
			if (style.styleName.equals(styleName))
			return style.bitmapFont;
		}
		
		return null;
	}

	public void LoadStylesFromJson(FileHandle file)
	{
		float density = Gdx.graphics.getDensity();
		
		Json json = new Json();
		json.setTypeName(null);
		json.setUsePrototypes(false);
		json.setIgnoreUnknownFields(true);
		json.setOutputType(OutputType.json);
		
		FontStyleData styles = json.fromJson(FontStyleData.class, file);
		
		Array<FreeTypeFontGeneratorExt> generators = new Array<FreeTypeFontGeneratorExt>();
		stylesLibrary = new Array<FontStyleLoaded>(styles.styles.size);
		
		Color tmpColor = null;
		boolean rebuild;
		
		for (FontStyleDataEntry style : styles.styles)
		{
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = Math.round(style.size * density);
			
			FreeTypeFontGeneratorExt generator = GetGenerator(generators,style.font,parameter);
			FontStyleLoaded loadedFont = new FontStyleLoaded(style.name,generator.fontBitmap);
			
			tmpColor = new Color(style.color.r,style.color.g,style.color.b,style.color.a);
			
			//Если будут найдены какие-то отличия в настройках BitmapFont, то он
			//копируется с новыми параметрами. При этом текстура все равно используется общая.
			if (generator.justCreated)
			{
				loadedFont.bitmapFont.setColor(tmpColor);
			}
			else
			{
				rebuild = false;
				
				for (;;)
				{
					if (false == loadedFont.bitmapFont.getColor().equals(tmpColor))
					{
						rebuild = true;
						break;
					}
				}
				
				if (rebuild)
				{
					loadedFont.bitmapFont = new BitmapFont(loadedFont.bitmapFont.getData(),
														   loadedFont.bitmapFont.getRegions(),
														   loadedFont.bitmapFont.usesIntegerPositions());
					loadedFont.bitmapFont.setColor(tmpColor);
				}
			}
			
			stylesLibrary.add(loadedFont);
		}
		
		for (FreeTypeFontGeneratorExt generator : generators)
		{
			generator.dispose();
		}
	}
	
	public FreeTypeFontGeneratorExt GetGenerator(Array<FreeTypeFontGeneratorExt> generators, String name, FreeTypeFontParameter parameters)
	{
		for (FreeTypeFontGeneratorExt generator : generators)
		{
			if (generator.name.equals(name)) 
			if (generator.parameters.equals(parameters))
			{
				generator.justCreated = false;
				return generator;
			}
		}
		
		FreeTypeFontGeneratorExt new_generator = new FreeTypeFontGeneratorExt(Gdx.files.internal(name),name,parameters);
		generators.add(new_generator);
		
		return new_generator;
	}
}

class FontStyleLoaded
{
	public String styleName;
	public BitmapFont bitmapFont;
	
	public FontStyleLoaded(String styleName, BitmapFont bitmapFont)
	{
		this.styleName = styleName;
		this.bitmapFont = bitmapFont;
	}
}

class FreeTypeFontGeneratorExt extends FreeTypeFontGenerator
{
	public String name;
	public FreeTypeFontParameter parameters;
	public BitmapFont fontBitmap;
	public boolean justCreated;
	
	public FreeTypeFontGeneratorExt(FileHandle font) {
		super(font);
		justCreated = true;
	}
	
	public FreeTypeFontGeneratorExt(FileHandle font, String name, FreeTypeFontParameter parameters) {
		super(font);
		this.name = name;
		this.parameters = parameters;
		
		fontBitmap = generateFont(parameters);
		justCreated = true;
	}
}

class FontStyleData {
	public Array<FontStyleDataEntry> styles;
}

class FontStyleDataEntry
{
	public String name;
	public String font;
	public int size;
	public FontStyleColorEntry color;
}

class FontStyleColorEntry
{
	public int r,g,b,a;
}


