package game.entities.powerups;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.entities.Entity;
import game.scenes.LevelScene;
import javafx.scene.canvas.GraphicsContext;

public abstract class Powerup extends Entity {

    public static final int TOTAL_POWERUPS = 4;

    protected static final long POWERUP_TIMEOUT =
            TimeUnit.SECONDS.toNanos(5);

    private static final int POWERUP_BASE_SCORE = 100;
    private static final String SFX_POWERUP_COLLECT = "sfx_powerup_collect.wav";

    private boolean consumed;

    public Powerup(int x, int y, LevelScene parent) {
        super(x, y, parent);

        parent.getActions().add(POWERUP_TIMEOUT, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                consumed = true;
                return true;
            }
        });
    }

    @Override
    public void update(long now) {
        if (this.consumed) {
            return;
        }

        super.update(now);

        if (this.getParent().getOutlaw().isAlive()) {
            if (this.intersects(this.getParent().getOutlaw())) {
                this.consumed = true;
                this.applyPowerup();
                this.getParent().getOutlaw().spawnPowerupEffect();
                this.getParent().addScore(POWERUP_BASE_SCORE);
                Game.playSFX(SFX_POWERUP_COLLECT);
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (this.consumed) {
            return;
        }

        super.draw(gc);
    }

    public abstract void applyPowerup();

    public boolean getConsumed() {
        return this.consumed;
    }

}
