package game;

import javafx.animation.AnimationTimer;

public final class GameTimer extends AnimationTimer {

    @Override
    public void handle(long now) {
        if (Game.getGameScene() == null) {
            return;
        }

        if (Game.getActionManager() != null) {
            Game.getActionManager().update(now);
        }

        Game.getGameScene().update(now);
        Game.getGameScene().draw(now);
    }

}
