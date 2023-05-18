package game;

import javafx.animation.AnimationTimer;

public class GameTimer extends AnimationTimer {

    @Override
    public void handle(long now) {
        if (Game.getGameScene() == null) {
            return;
        }

        Game.getGameScene().update(now);
        Game.getGameScene().draw(now);
    }

}
