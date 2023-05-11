package game.entities;

import game.scenes.LevelScene;

public interface LevelUpdatable {
    public void update(long currentNanoTime, LevelScene level);
}
