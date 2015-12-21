package com.mypinguin.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.penguin.menu.MainMenuStage;

public class PenguinGame extends Game {
	public class ContactsController implements ContactListener {
		public PenguinGame game;

		public ContactsController(PenguinGame gm) {
			this.game = gm;
		}

		@Override
		public void beginContact(Contact contact) {
			Fixture fixtureA = contact.getFixtureA();
			Fixture fixtureB = contact.getFixtureB();
			Object objA = fixtureA.getBody().getUserData();
			Object objB = fixtureB.getBody().getUserData();
			if( objA != null && objA instanceof BodyActor ) {
				((BodyActor)objA).beginContact(fixtureA, fixtureB, contact);
			}
			if( objB != null && objB instanceof BodyActor ) {
				((BodyActor)objB).beginContact(fixtureB, fixtureA, contact);
			}
		}

		@Override
		public void endContact(Contact contact) {
			Fixture fixtureA = contact.getFixtureA();
			Fixture fixtureB = contact.getFixtureB();
			Object objA = fixtureA.getBody().getUserData();
			Object objB = fixtureB.getBody().getUserData();
			if( objA != null && objA instanceof BodyActor ) {
				((BodyActor)objA).endContact(fixtureA, fixtureB, contact);
			}
			if( objB != null && objB instanceof BodyActor ) {
				((BodyActor)objB).endContact(fixtureB, fixtureA, contact);
			}
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
			Fixture fixtureA = contact.getFixtureA();
			Fixture fixtureB = contact.getFixtureB();
			Object objA = fixtureA.getBody().getUserData();
			Object objB = fixtureB.getBody().getUserData();
			if( objA != null && objA instanceof BodyActor ) {
				((BodyActor)objA).preSolve(contact, oldManifold);
			}
			if( objB != null && objB instanceof BodyActor ) {
				((BodyActor)objB).preSolve(contact, oldManifold);
			}
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			Fixture fixtureA = contact.getFixtureA();
			Fixture fixtureB = contact.getFixtureB();
			Object objA = fixtureA.getBody().getUserData();
			Object objB = fixtureB.getBody().getUserData();
			if( objA != null && objA instanceof BodyActor ) {
				((BodyActor)objA).postSolve(contact, impulse);
			}
			if( objB != null && objB instanceof BodyActor ) {
				((BodyActor)objB).postSolve(contact, impulse);
			}
		}
	}

	public FontsManager fonts;

	public BitmapFont   font;  //шрифт по умолчанию
	public BitmapFont   bigFont;
	public SpriteBatch  batch; //отрисовщик текстур
	public AssetManager asset; //менеджер ресурсов
	public PlayerActor  player;
	public boolean      isDebug = true;

	// physics
	public World        world;
	public float        units  = 64f; //пикселей на физю метр

	public ContactsController contacts;
	public final int m_mainFontSize = 32; //размер главного шрифта
//	public static final String FONT_CHARACTERS = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
//            + "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
//            + "1234567890.,:;_¡!¿?\"'+-*/()[]={}";
	/* разрешение экрана,в будущем сделать функцию
	 * для выбора разного разрешения экрана
	 * */ 
	public float       width  = 800;
	public float       height = 480;

	@Override
	public void create() {
//		font = new BitmapFont();
//		font.setUseIntegerPositions(true);
		batch = new SpriteBatch();
		asset = new AssetManager();
		
		fonts = new FontsManager();
		fonts.LoadStylesFromJson(Gdx.files.internal("styles.json"));
		
		// тест загрузки шрифта
		/*float densityIndependentSize = m_mainFontSize * Gdx.graphics.getDensity();
		int fontSize = Math.round(densityIndependentSize );
//		FreeTypeFontGenerator.NO_MAXIMUM = 32;
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Agentorange.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = m_mainFontSize;
		bigFont = generator.generateFont(parameter);
		generator.dispose(); // don't forget to dispose to avoid memory leaks!*/
		
		bigFont = fonts.GetFont("bigFont");

		font = new BitmapFont();

		Box2D.init();
		world = new World(new Vector2(0, -10), true);
		contacts = new ContactsController(this);
		world.setContactListener(contacts);
		// установка начального уровня MainMenu
		this.setScreen(new MainMenuStage(this));
		
		//this.setScreen( new MainMenuScreen(this) );
		//this.setScreen( new Box2DTestLevel(this) );
//		this.setScreen( new MainMenuScreen(this) );
//		this.setScreen( new PhysicsTest() );
	}
	
	@Override
	public void render() {
		super.render();
	}
	
	public void dispose() {
		font.dispose();
		fonts.dispose();
		batch.dispose();
		asset.dispose();
		world.dispose();
	}
}
