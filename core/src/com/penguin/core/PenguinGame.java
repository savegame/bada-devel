package com.penguin.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.penguin.menu.MainMenuStage;
import com.penguin.physics.BodyActor;
import com.penguin.physics.BoxActor;

import java.util.ArrayList;

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
			if( objA != null && objA instanceof com.penguin.physics.BodyActor) {
				((com.penguin.physics.BodyActor)objA).beginContact(fixtureA, fixtureB, contact);
			}
			if( objB != null && objB instanceof com.penguin.physics.BodyActor) {
				((com.penguin.physics.BodyActor)objB).beginContact(fixtureB, fixtureA, contact);
			}
		}

		@Override
		public void endContact(Contact contact) {
			Fixture fixtureA = contact.getFixtureA();
			Fixture fixtureB = contact.getFixtureB();
			Object objA = fixtureA.getBody().getUserData();
			Object objB = fixtureB.getBody().getUserData();
			if( objA != null && objA instanceof com.penguin.physics.BodyActor) {
				((com.penguin.physics.BodyActor)objA).endContact(fixtureA, fixtureB, contact);
			}
			if( objB != null && objB instanceof com.penguin.physics.BodyActor) {
				((com.penguin.physics.BodyActor)objB).endContact(fixtureB, fixtureA, contact);
			}
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
			Fixture fixtureA = contact.getFixtureA();
			Fixture fixtureB = contact.getFixtureB();
			Object objA = fixtureA.getBody().getUserData();
			Object objB = fixtureB.getBody().getUserData();
			if( objA != null && objA instanceof com.penguin.physics.BodyActor) {
				((com.penguin.physics.BodyActor)objA).preSolve(contact, oldManifold);
			}
			if( objB != null && objB instanceof com.penguin.physics.BodyActor) {
				((com.penguin.physics.BodyActor)objB).preSolve(contact, oldManifold);
			}
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			Fixture fixtureA = contact.getFixtureA();
			Fixture fixtureB = contact.getFixtureB();
			Object objA = fixtureA.getBody().getUserData();
			Object objB = fixtureB.getBody().getUserData();
			if( objA != null && objA instanceof com.penguin.physics.BodyActor) {
				((com.penguin.physics.BodyActor)objA).postSolve(contact, impulse);
			}
			if( objB != null && objB instanceof com.penguin.physics.BodyActor) {
				((com.penguin.physics.BodyActor)objB).postSolve(contact, impulse);
			}
		}
	}

	public FontsManager fonts;
	public ParticlesManager particles;

	public BitmapFont    font;  //шрифт по умолчанию
	public BitmapFont    bigFont;
	public SpriteBatch   batch; //отрисовщик текстур
	public ModelBatch    modelBatch;
	public AssetManager  asset; //менеджер ресурсов
	public PlayerActor player = null;
	public boolean       isDebug = false;
	public Camera        camera = null;
	public Box2DLevel m_level = null;
	public MainMenuStage m_mainMenu = null;

	private ArrayList<BodyActor> destroy = new ArrayList<BodyActor>();
	private ArrayList<Joint> destroyJoint = new ArrayList<Joint>();

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
	//глобальный генератор случайных чисел
	public RandomXS128 rand = new RandomXS128();

	//public int eParticles_Foreground = 0;

	@Override
	public void create() {
		batch = new SpriteBatch();
		modelBatch = new ModelBatch();
		asset = new AssetManager();
		
		fonts = new FontsManager();
		fonts.LoadStylesFromJson(Gdx.files.internal("styles.json"));
		
		particles = new ParticlesManager(this);
		
		bigFont = fonts.GetFont("bigFont");

		font = new BitmapFont();

		Box2D.init();
		world = new World(new Vector2(0, -10), true);
		contacts = new ContactsController(this);
		world.setContactListener(contacts);
		// установка начального уровня MainMenu
		m_mainMenu = new MainMenuStage(this);
		this.setScreen(m_mainMenu);
	}
	
	@Override
	public void render() {
		super.render();
	}
	
	public void dispose() {
		super.dispose();
//		world.dispose();
//		world = null;
//		fonts.dispose();
//		particles.dispose();
//		batch.dispose();
		asset.dispose();
		asset = null;
	}

	public void addToDestroy(BodyActor actor) {
		if(!destroy.contains(actor)) {
			destroy.add(actor);
			actor.setDestroy();
			if(player != null && actor instanceof BoxActor )
				player.detachIfNeed( (BoxActor) actor);
		}
	}

	public void addToDestroy(Joint joint) {
		if(!destroyJoint.contains(joint)) {
			destroyJoint.add(joint);
		}
	}

	public void destroyBodies() {
		if( world.isLocked() ) return;
		while( destroyJoint.iterator().hasNext() ) {
			Joint joint = destroyJoint.iterator().next();
			world.destroyJoint(joint);
			destroyJoint.remove(joint);
		}
		while( destroy.iterator().hasNext() ) {
			BodyActor actor = destroy.iterator().next();

			if( actor.destroyBody() ) {
				actor.clear();
				actor.remove();
				destroy.remove(actor);
			}
		}
	}

	public void loadLevel(String path) {
		if(m_level == null)
			m_level = new Box2DLevel(this);
		this.setScreen(m_level);
		m_level.loadMap(path);
	}
}