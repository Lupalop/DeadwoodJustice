package game.entities;

import game.LevelScene;

public interface LevelUpdatable {
    public void update(long currentNanoTime, LevelScene level);
}
