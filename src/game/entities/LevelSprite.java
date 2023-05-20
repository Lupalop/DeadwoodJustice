package game.entities;

import game.scenes.LevelScene;

public abstract class LevelSprite extends Sprite {

    private LevelScene parent;

    public LevelSprite(int x, int y, LevelScene parent) {
        super(x, y);
        this.parent = parent;
    }

    public LevelSprite(LevelScene parent) {
        super(0, 0);
        this.parent = parent;
    }

    protected LevelScene getParent() {
        return this.parent;
    }

}
