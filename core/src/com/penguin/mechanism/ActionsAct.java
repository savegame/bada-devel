package com.penguin.mechanism;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by savegame on 06.01.16.
 */
public class ActionsAct {
	enum ChangeType {
		Activate,
		Deactivate,
		Change
	}

	class ChangeTargetAction extends Action {
		ChangeType change;
		ChangeTargetAction(ActionObject target, ChangeType change) {
			this.target = target;
			this.change = change;
		}

		@Override
		public boolean act(float delta) {
			//ActionObject actor = (ActionObject)this.actor;
			ActionObject target = (ActionObject)this.target;

			//if( actor != null && actor.isActive() )
			{
				switch (this.change) {
					case Activate:
						target.activate();
						break;
					case Deactivate:
						target.deactivate();
						break;
					case Change:
						if( target.isActive() )
							target.deactivate();
						else
							target.activate();
						break;
				}
			}
			return false;
		}

		@Override
		public void setActor (Actor actor) {
			if( actor instanceof ActionObject )
				super.setActor(actor);
			else
				super.setActor(null);
		}

		@Override
		public void setTarget (Actor target) {
			if( target instanceof ActionObject )
				this.target = target;
			else
				super.setTarget(null);
		}
	}
}
