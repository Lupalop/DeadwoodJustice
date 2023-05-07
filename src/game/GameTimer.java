package game;

import javafx.animation.AnimationTimer;

public class GameTimer extends AnimationTimer {

    private Game manager;
    
    GameTimer(Game manager) {
        this.manager = manager;
    }
    
    @Override
    public void handle(long now) {
        if (this.manager.getGameScene() == null) {
            return;
        }

        this.manager.getGameScene().update(now);
        this.manager.getGameScene().draw(now);
    }

}
