package com.mypinguin.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by savegame on 11.11.15.
 */
public class MapBodyManager implements Disposable {

/** вспомогательные классы данных */

	/**
	 * Класс для хранения шаблона физического объекта
	 */
	public class BodyTemplate implements Disposable {
		public FixtureDef  fixturDef;
		public BodyDef     bodyDef;
		public Shape       shape;
		//public boolean     isdynamic;

		public BodyTemplate( Shape shape ) {
			this.shape = shape;
		}

		@Override
		public void dispose() {
			shape.dispose();
		}
	}

	public class Part {
		TextureRegion region; //текстура
		Vector2       shift; // смещение
	}

	public class TextureTemplate {
		public String name;
		ObjectMap<String, Part> parts = new ObjectMap<String, Part>();
	}
/** Параметрыы */
	private Logger logger;
	private World world;
	private List<BodyActor> actors = new ArrayList<BodyActor>();
	private Array<Body> bodies = new Array<Body>();
	private ObjectMap<String, FixtureDef> materials = new ObjectMap<String, FixtureDef>();
	private ObjectMap<String, PlatformActor> platforms = new ObjectMap<String, PlatformActor>();
	private ObjectMap<String, PolylineMapObject> paths = new ObjectMap<String, PolylineMapObject>();
	private ObjectMap<String, BodyTemplate> templates = new ObjectMap<String, BodyTemplate>();
	private ObjectMap<String, TextureTemplate> textempl = new ObjectMap<String, TextureTemplate>();
	private PenguinGame game;

	private float abs(float a) {
		return (a < 0 )?-a:a;
	}
	/**
	 * @param pg pinguin game
//	 * @param unitsPerPixel conversion ratio from pixel units to box2D metres.
	 * @param materialsFile json file with specific physics properties to be assigned to newly created bodies.
	 * @param loggingLevel verbosity of the embedded logger.
	 */
	public MapBodyManager(PenguinGame pg, FileHandle materialsFile, int loggingLevel) {
		logger = new Logger("MapBodyManager", loggingLevel);
		logger.info("initialising");

		game = pg;
		this.world = game.world;

		FixtureDef defaultFixture = new FixtureDef();
		defaultFixture.density = 1.0f;
		defaultFixture.friction = 0.8f;
		defaultFixture.restitution = 0.2f;

		materials.put("default", defaultFixture);


		if (materialsFile != null) {
			loadMaterialsFile(materialsFile);
		}
	}

	/**
	 * @param map will use the "physics" layer of this map to look for shapes in order to create the static bodies.
	 */
	public void createPhysics(Map map) {
		createTemplates(map, "Templates");
		createPhysics(map, "Objects");
	}

	/**
	 * @param map map to be used to create the static bodies.
	 * @param layerName name of the layer that contains the shapes.
	 */
	public void createPhysics(Map map, String layerName) {
		MapLayer layer = map.getLayers().get(layerName);

		if (layer == null) {
			logger.error("layer " + layerName + " does not exist");
			return;
		}

		MapObjects objects = layer.getObjects();
		Iterator<MapObject> objectIt = objects.iterator();

		while(objectIt.hasNext()) {
			MapObject object = objectIt.next();

			BodyDef bodyDef = new BodyDef();

			MapProperties properties = object.getProperties();
			String material = properties.get("material", "default", String.class);
			String dynamic = properties.get("dynamic", "false", String.class);
			String type = properties.get("type", "notype", String.class);
			String name = object.getName();

			if (object instanceof TextureMapObject){
				TextureMapObject tmo = (TextureMapObject)object;
				if( templates.containsKey(name) )
				{
					BodyTemplate tmpl = templates.get(name);
					BoxActor box = new BoxActor(game, tmo.getTextureRegion(), tmpl.fixturDef );
					box.setPosition( tmo.getX() + tmo.getTextureRegion().getRegionWidth()/2, tmo.getY() + tmo.getTextureRegion().getRegionHeight()/2 );
					box.setRotation( tmo.getRotation() );
					box.initialize(tmpl.shape);
					actors.add(box);
				}
				continue;
			}

			Shape shape;

			if (object instanceof RectangleMapObject) {
//				RectangleMapObject rectangle = (RectangleMapObject)object;
				shape = getRectangle((RectangleMapObject)object, bodyDef);
			}
			else if (object instanceof PolygonMapObject) {
				shape = getPolygon((PolygonMapObject)object);
			}
			else if (object instanceof PolylineMapObject) {
				shape = getPolyline((PolylineMapObject)object);
			}
			else if (object instanceof CircleMapObject) {
				shape = getCircle((CircleMapObject)object);
			}
			else {
				logger.error("non suported shape " + object);
				continue;
			}

			if( type.equalsIgnoreCase("box") ) {
				FixtureDef fixtureDef = materials.get(material);
				TextureRegion reg = null;
				if( game.asset.isLoaded("box_0.png") )
				{
					reg = new TextureRegion( game.asset.get("box_0.png", Texture.class) );
				}
				BoxActor box = new BoxActor(game, reg, fixtureDef );
				box.setPosition( bodyDef.position.x*game.units, bodyDef.position.y*game.units );
				box.initialize(shape);
				actors.add(box);
			}
			else if( type.equalsIgnoreCase("player") ) {
				FixtureDef fixtureDef = materials.get(material);
				game.player = new PlayerActor(game, fixtureDef);

				game.player.setName("PlayerActor");
				game.player.setPosition(bodyDef.position.x * game.units, bodyDef.position.y * game.units);
				game.player.setBodyType(BodyDef.BodyType.DynamicBody);
				game.player.initialize(shape);
				//actors.add(player);
			}
			else if( type.equalsIgnoreCase("lift") ) {
				RectangleMapObject rect =(RectangleMapObject)object;
				float width = rect.getRectangle().getWidth();
				float height = rect.getRectangle().getHeight();
				FixtureDef fixtureDef = materials.get(material);
				PlatformActor plat = new PlatformActor(game, fixtureDef);
				//ищем текстуру если нужно
				String texTemplateName = properties.get("textmpl", "none", String.class);
				if( texTemplateName != "none" && textempl.containsKey(texTemplateName) ){
					TextureTemplate texTempl = textempl.get(texTemplateName);
					float length = width;
					Part leftR = texTempl.parts.get("left");
					Part rightR = texTempl.parts.get("right");
					Part midR = texTempl.parts.get("mid");
					length -= leftR.region.getRegionWidth();
					plat.addTextureRegion(leftR.region, leftR.shift.x, leftR.shift.y);
					//float rightT = rightR.region.getRegionWidth() - abs(rightR.shift.x);
					while ( length > rightR.region.getRegionWidth() ) {
						plat.addTextureRegion(midR.region, midR.shift.x, midR.shift.y );
						length -= midR.region.getRegionWidth() ;
					}
					plat.addTextureRegion(rightR.region, rightR.shift.x, rightR.shift.y);
				}
				plat.setName(name);
				plat.setPosition(bodyDef.position.x * game.units, bodyDef.position.y * game.units);
				plat.initialize(shape);
				plat.setSize(width, height);

				if( properties.containsKey("speed") ){
					float speed = Float.parseFloat(properties.get("speed", "2", String.class));
					plat.setMoveSpeed(speed*game.units);
				}
				if( properties.containsKey("isactive") ){
					boolean active = Boolean.parseBoolean(properties.get("isactive", "true", String.class));
					if( active )
						plat.activate();
					else
						plat.deactivate();
				}

				platforms.put(name, plat);
				actors.add(plat);
			}
			else if( type.equalsIgnoreCase("path") ) {
				paths.put(name, (PolylineMapObject)object);
			}
			else if( type.equalsIgnoreCase("button") ) {
				//TODO Дописать инициализацию кнопок, с захватом списка действий

			}
			else if( type.equalsIgnoreCase("water") ) {
				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.isSensor = true;
				WaterActor water = new WaterActor(game, fixtureDef);
				water.setName(name);
				RectangleMapObject rectangle = (RectangleMapObject) object;
				water.setSize(rectangle.getRectangle().getWidth(), rectangle.getRectangle().getHeight() );
				water.setPosition(bodyDef.position.x * game.units, bodyDef.position.y * game.units);
				water.initialize(shape);

				actors.add(water);
			}
			else if( type.equalsIgnoreCase("ground") ) {
				if (dynamic.equalsIgnoreCase("true"))
					bodyDef.type = BodyDef.BodyType.DynamicBody;
				else
					bodyDef.type = BodyDef.BodyType.StaticBody;
				FixtureDef fixtureDef = materials.get(material);

				if (fixtureDef == null) {
					logger.error("material does not exist " + material + " using default");
					fixtureDef = materials.get("default");
				}

				fixtureDef.shape = shape;
				//			fixtureDef.filter.categoryBits = Env.game.getCategoryBitsManager().getCategoryBits("level");

				Body body = world.createBody(bodyDef);
				body.createFixture(fixtureDef);

				bodies.add(body);

				fixtureDef.shape = null;
			}
			shape.dispose();
		}

		//Add paths to platforms
		ObjectMap.Entries<String, PolylineMapObject> it = paths.iterator();
		while( it.hasNext() ) {
			ObjectMap.Entry<String, PolylineMapObject> current = it.next();

			if( platforms.containsKey(current.key) ){
				PlatformActor plat = platforms.get(current.key);
				PolylineMapObject poly = current.value;
				float[] vertices = poly.getPolyline().getVertices();
				Vector2[] path = new Vector2[vertices.length / 2];

				for (int i = 0; i < vertices.length / 2; ++i) {
					path[i] = new Vector2();
					path[i].x = vertices[i * 2] + poly.getPolyline().getX();
					path[i].y = vertices[i * 2 + 1] + poly.getPolyline().getY();
				}
				plat.setPath(path);
			}
		}
		paths.clear();
	}

	/**
	 * Создает шаблонные физические обекты,
	 * берет их из специльного слоя шаблонных объектов
	 */
	public void createTemplates(Map map, String layerName){
		MapLayer layer = map.getLayers().get(layerName);

		if (layer == null) {
			logger.error("layer " + layerName + " does not exist");
			return;
		}

		MapObjects objects = layer.getObjects();
		Iterator<MapObject> objectIt = objects.iterator();

		while(objectIt.hasNext()) {
			MapObject object = objectIt.next();


			MapProperties properties = object.getProperties();
			String material = properties.get("material", "default", String.class);
			String type = properties.get("type", "notype", String.class);
			String name = object.getName();

			if (object instanceof TextureMapObject && type.equalsIgnoreCase("texture") ){
				TextureMapObject tmo = (TextureMapObject)object;
				String key = properties.get("key", "nokey", String.class);
				//String str = ;
				float sx = Float.parseFloat(properties.get("shiftX", "0", String.class));
				float sy = Float.parseFloat(properties.get("shiftY", "0", String.class));
				if( key == "nokey" )
					continue;

				if( textempl.containsKey(name) ) {
					TextureTemplate txtempl = textempl.get(name);
					Part part = new Part();
					part.region = tmo.getTextureRegion();
					part.shift = new Vector2(sx,sy);
					txtempl.parts.put(key, part);
				}
				else
				{
					TextureTemplate txtempl = new TextureTemplate();
					Part part = new Part();
					part.region = tmo.getTextureRegion();
					part.shift = new Vector2(sx,sy);
					txtempl.parts.put(key, part);
					textempl.put( name, txtempl );
				}
				continue;
			}

			Shape shape;
			BodyDef bodyDef = new BodyDef();

			if (object instanceof RectangleMapObject) {
//				RectangleMapObject rectangle = (RectangleMapObject)object;
				shape = getRectangle((RectangleMapObject)object, bodyDef);
			}
			else if (object instanceof PolygonMapObject) {
				shape = getPolygon((PolygonMapObject)object, bodyDef);
			}
			else if (object instanceof PolylineMapObject) {
				shape = getPolyline((PolylineMapObject)object);
			}
			else if (object instanceof CircleMapObject) {
				shape = getCircle((CircleMapObject)object);
			}
			else {
				logger.error("non suported shape " + object);
				continue;
			}

			if( type.equalsIgnoreCase("dynamic") ) {
//				FixtureDef fixtureDef = materials.get(material);
				BodyTemplate temp = new BodyTemplate(shape);
				temp.bodyDef = bodyDef;
				temp.fixturDef = materials.get(material);
				templates.put(name, temp);
			}
			else if( type.equalsIgnoreCase("static") ) {
				bodyDef.type = BodyDef.BodyType.StaticBody;
				FixtureDef fixtureDef = materials.get(material);
				if (fixtureDef == null) {
					logger.error("material does not exist " + material + " using default");
					fixtureDef = materials.get("default");
				}
				fixtureDef.shape = shape;
				//			fixtureDef.filter.categoryBits = Env.game.getCategoryBitsManager().getCategoryBits("level");

				Body body = world.createBody(bodyDef);
				body.createFixture(fixtureDef);

				bodies.add(body);

				fixtureDef.shape = null;
			}
			else
				shape.dispose();
		}
	}
	/**
	 * Destroys every static body that has been created using the manager.
	 */
	@Override
	public void dispose() {
		for (Body body : bodies) {
			world.destroyBody(body);
		}
		ObjectMap.Entries<String,BodyTemplate> it = templates.iterator();
		while( it.hasNext() ) {
			ObjectMap.Entry<String,BodyTemplate> cur = it.next();
			cur.value.dispose();
		}
		bodies.clear();
	}

	public void actorsToStage(Stage stage) {
		for( BodyActor actor : actors ) {
			stage.addActor(actor);
		}
	}

	private void loadMaterialsFile(FileHandle materialsFile) {
		logger.info("adding default material");

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 1.0f;
		fixtureDef.restitution = 0.0f;
		materials.put("default", fixtureDef);

		logger.info("loading materials file");

		try {
			JsonReader reader = new JsonReader();
			JsonValue root = reader.parse(materialsFile);
			JsonValue.JsonIterator materialIt = root.iterator();

			while (materialIt.hasNext()) {
				JsonValue materialValue = materialIt.next();

				if (!materialValue.has("name")) {
					logger.error("material without name");
					continue;
				}

				String name = materialValue.getString("name");

				fixtureDef = new FixtureDef();
				fixtureDef.density = materialValue.getFloat("density", 1.0f);
				fixtureDef.friction = materialValue.getFloat("friction", 1.0f);
				fixtureDef.restitution = materialValue.getFloat("restitution", 0.0f);
				logger.info("adding material " + name);
				materials.put(name, fixtureDef);
			}

		} catch (Exception e) {
			logger.error("error loading " + materialsFile.name() + " " + e.getMessage());
		}
	}

	private Shape getRectangle(RectangleMapObject rectangleObject, BodyDef bodyDef) {
		Rectangle rectangle = rectangleObject.getRectangle();
		PolygonShape polygon = new PolygonShape();
		Vector2 size = new Vector2((rectangle.width * 0.5f) / game.units,
						(rectangle.height * 0.5f ) / game.units);
		polygon.setAsBox(size.x,
						size.y,
						size,
						0.0f);
		bodyDef.position.set(rectangle.getX() / game.units, rectangle.getY() / game.units);
		bodyDef.angle = rectangleObject.getProperties().get("rotation", 0f, float.class) * MathUtils.degRad;
		return polygon;
	}

	private Shape getCircle(CircleMapObject circleObject) {
		Circle circle = circleObject.getCircle();
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(circle.radius / game.units);
		circleShape.setPosition(new Vector2(circle.x / game.units, circle.y / game.units));
		return circleShape;
	}

	private Shape getPolygon(PolygonMapObject polygonObject) {
		PolygonShape polygon = new PolygonShape();
		float[] vertices = polygonObject.getPolygon().getTransformedVertices();

		float[] worldVertices = new float[vertices.length];
		for (int i = 0; i < vertices.length; ++i) {
			worldVertices[i] = vertices[i] / game.units;
		}

		polygon.set(worldVertices);
		return polygon;
	}

	private Shape getPolygon(PolygonMapObject polygonObject, BodyDef bodyDef) {
		PolygonShape polygon = new PolygonShape();
		float[] vertices = polygonObject.getPolygon().getTransformedVertices();

		float[] worldVertices = new float[vertices.length];
		//float half_unit = game.units / 2;
		for (int i = 0; i < vertices.length; ++i) {
			worldVertices[i] = (vertices[i] % game.units)/game.units;
		}
		polygon.set(worldVertices);
		return polygon;
	}

	private Shape getPolyline(PolylineMapObject polylineObject) {
		float[] vertices = polylineObject.getPolyline().getTransformedVertices();
		Vector2[] worldVertices = new Vector2[vertices.length / 2];

		for (int i = 0; i < vertices.length / 2; ++i) {
			worldVertices[i] = new Vector2();
			worldVertices[i].x = vertices[i * 2] / game.units;
			worldVertices[i].y = vertices[i * 2 + 1] / game.units;
		}

		ChainShape chain = new ChainShape();
		chain.createChain(worldVertices);
		return chain;
	}
}