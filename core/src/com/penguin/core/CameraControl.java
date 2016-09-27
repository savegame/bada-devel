package com.penguin.core;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class CameraControl extends Actor {
	private OrthographicCamera camera = null;
	private Actor   targetActor = null;
	private Vector2 m_shift = new Vector2();
	private Vector2 m_lastPos = new Vector2();
	private Vector2 m_velocity = new Vector2();
	private float   m_zoomAccel = 0.2f; //скорость изменения приближения
	private float   m_setZoom = 1.5f;   //фиксированное приближение

	enum FollowType {//стадии преследования
		Wait,
		Link,
		Follow
	}
	private FollowType followType = FollowType.Wait;

	public CameraControl(OrthographicCamera cam) {
		setCamera(cam);
		setPosition(cam.position.x, cam.position.y);
		setTouchable(Touchable.disabled);
		setName("CameraControl");
		setCameraZoomAccel(1.3f);
		setCameraZoom(1.3f);
	}
	
	public void setCamera(OrthographicCamera cam) {
		camera = cam;
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public void setTarget( Actor target ) {
		targetActor = target;
	}

	/** Задать сдвиг относительно targetActor
	 * */
	public void setShift( float x, float y ) {
		m_shift = new Vector2(x,y);
	}

	/** Задать скорость приближения.отдаления камеры */
	public void setCameraZoomAccel( float accel ) {
		if(accel < 0)
			m_zoomAccel = 0.1f;
		else
			m_zoomAccel = accel;
	}

	public Actor getTarget() {
		return targetActor;
	}

	/** фиксировать приближение камеры */
	public void setCameraZoom(float zoom) {
		m_setZoom = zoom;
	}

	/** обработка приближения камеры */
	private void updateZoom(float delta) {
		if( camera.zoom < m_setZoom ) {
			camera.zoom += m_zoomAccel*delta;
			if(camera.zoom > m_setZoom)
				camera.zoom = m_setZoom;
		}
		else if( camera.zoom > m_setZoom ) {
			camera.zoom -= m_zoomAccel*delta;
			if(camera.zoom < m_setZoom)
				camera.zoom = m_setZoom;
		}
	}

	/** обработка скорости */
	private void updateVelocity(float delta) {
		m_velocity.x = (m_lastPos.x - getX()) / delta;
		m_velocity.y = (m_lastPos.y - getY()) / delta;
	}

	private void updatePosition(float delta) {
		if( targetActor == null )
		{
			m_lastPos.x = getX();
			m_lastPos.y = getY();
			return;
		}
		Vector2 distance = new Vector2( targetActor.getX()  - getX(), targetActor.getY() - getY() );
		Vector2 newPos = new Vector2();
		newPos.x = targetActor.getX();
		newPos.y = targetActor.getY();
		setPosition( newPos.x , newPos.y );

		m_lastPos = newPos;
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		if( targetActor != null ){
			updateVelocity(delta);
			updatePosition(delta);
		}
		if( camera != null ) {
			updateZoom(delta);
			camera.position.set(getX() + m_shift.x , getY() + m_shift.y, 0);
			camera.update();
		}
	}
}
