package com.penguin.particles;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.mypinguin.game.PenguinGame;

public class Emitter_Snow extends BaseEmitter<Particle_Snow>
{
	private float emissionTimer = 0;
	private OrthographicCamera camera;
	private float screenWidth, 	screenHeight,
				  screenLeft, 	screenRight,
				  screenTop, 	screenBottom;
	
	private float windPower = 0;
	private float particleAltitude;
	
	private ArrayList<Rectangle> solidRegions;
	private ArrayList<Rectangle> visibleSolidRegions;
	private int visibleSolidRegionsCount = 0;
	
	private float screenScale = 1.0f;
	private float snowDensity = 1.0f;
	private float generationAccumulator = 0;
	
	public Emitter_Snow(PenguinGame game, OrthographicCamera camera) {
		super(game, game.particles.getParticleSprite("tiny_snowflake"), Particle_Snow.class);
		
		this.unlimitedGenerationEnabled = false;
		
		//setMaxParticlesCount(100);
		this.camera = camera;
		
		solidRegions = new ArrayList<Rectangle>();
		visibleSolidRegions = new ArrayList<Rectangle>();
		
		WarmUp(5.0f,0.1f);
	}
	
	public void SetDensity(float density)
	{
		snowDensity = density;
	}
	
	public void AddSolidRegion(float x, float y, float width, float height)
	{
		solidRegions.add(new Rectangle(x,y,width,height));
	}
	
	public void RecalculateSolidRegionsVisibility()
	{
		Iterator<Rectangle> it = visibleSolidRegions.iterator();
		Rectangle tmp;

		int regionsToCheck = solidRegions.size(); 
		
		while (it.hasNext())
		{
			tmp = it.next();

			if (Math.abs(screenRight - tmp.x - tmp.width) > (tmp.width + screenWidth)*2 &&
				Math.abs(screenTop - tmp.y - tmp.height) > (tmp.height + screenHeight)*2)
			{
				solidRegions.add(tmp);
				it.remove();
				regionsToCheck--;
			}
		}

		it = solidRegions.iterator();
		
		while (it.hasNext() && regionsToCheck > 0)
		{
			tmp = it.next();
			
			if (Math.abs(screenRight - tmp.x - tmp.width) < (tmp.width + screenWidth)*2 &&
				Math.abs(screenTop - tmp.y - tmp.height) < (tmp.height + screenHeight)*2)
			{
				visibleSolidRegions.add(tmp);
				it.remove();
			}
			
			regionsToCheck--;
		}
		
		visibleSolidRegionsCount = visibleSolidRegions.size();
	}
	
	public void emit(float delta) {
		screenScale = camera.zoom / 1.3f; //первоначально эффект был настроен под зум = 1.3, поэтому теперь это база масштабирования
		screenWidth = camera.viewportWidth*(camera.zoom + 0.1f);//1.4f;
		screenHeight = camera.viewportHeight*(camera.zoom + 0.1f);
		
		screenLeft = camera.position.x - screenWidth*0.5f;
		screenRight = screenLeft + screenWidth;
		screenTop = camera.position.y + screenHeight*0.5f;
		screenBottom = screenTop - screenHeight;
		
		setPosition(camera.position.x, camera.position.y + screenHeight*0.5f);
		
		windPower += -1 + Math.random()*2;
		if (windPower > 10) windPower = 10;
		if (windPower < -10) windPower = -10;
		
		emissionTimer += delta*1000;
		if (emissionTimer > (100 / screenScale))
		{
			emissionTimer = 0;
			
			generationAccumulator += 1.0f * snowDensity;
			int particlesToGenerate = (int) Math.floor(generationAccumulator);
			generate(particlesToGenerate);
			generationAccumulator -= particlesToGenerate;
		}
		
		if (solidRegions.size() > 0 || visibleSolidRegionsCount > 0)
			RecalculateSolidRegionsVisibility(); 
	}
	
	public boolean updateParticle(float delta, Particle particle)
	{ 
		Particle_Snow snowflake = (Particle_Snow) particle;
		
		snowflake.life -= delta;
		if (snowflake.life <= 0) return false;
		
		if (visibleSolidRegionsCount > 0)
		{
			Iterator<Rectangle> it = visibleSolidRegions.iterator();

			while (it.hasNext())
			{
				if (it.next().contains(snowflake.getX(), snowflake.getY()))
				{
					snowflake.life *= 0.8;
					break;
				}
			}
		}
		
		if (snowflake.life <= 0) return false;
		
		particleAltitude = screenTop - snowflake.getY();
		
		if (particleAltitude < screenHeight*0.1f)
		snowflake.horizontal_speed += (windPower / 10)*0.25f;
		else
		snowflake.horizontal_speed*=0.99f;
		
		if (snowflake.horizontal_speed > 3) snowflake.horizontal_speed = 3;
		if (snowflake.horizontal_speed < -3) snowflake.horizontal_speed = -3;
			
		snowflake.setAlpha(Math.min(1.0f - particleAltitude / screenHeight, snowflake.life/(6*snowflake.screenScale)));
		
		snowflake.translateX(snowflake.horizontal_speed);
		snowflake.translateY(-delta*100);
		
		if (snowflake.getX() < screenLeft) snowflake.setX(snowflake.getX()+screenWidth);
		if (snowflake.getX() > screenRight) snowflake.setX(snowflake.getX()-screenWidth);
		if (snowflake.getY() < screenBottom) snowflake.setY(snowflake.getY()+screenHeight);
		if (snowflake.getY() > screenTop) snowflake.setY(snowflake.getY()-screenHeight);
		
		return true;
	}
	
	public void resetParticle(Particle particle)
	{
		Particle_Snow snowflake = (Particle_Snow) particle;
		
		snowflake.life = 6*screenScale;
		snowflake.screenScale = screenScale;
		snowflake.horizontal_speed = 0;
		snowflake.setPosition((float) (screenLeft+Math.random()*screenWidth), getY());
	}
}
