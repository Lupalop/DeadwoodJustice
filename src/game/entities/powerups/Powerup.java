package game.entities.powerups;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.entities.Entity;
import game.scenes.LevelScene;
import javafx.scene.canvas.GraphicsContext;

/**
 * This class represents power-ups that can be collected in-game.
 * @author Francis Dominic Fajardo
 */
public abstract class Powerup extends Entity {

    /** The total number of known power-ups. */
    public static final int TOTAL_POWERUPS = 4;
    /** The duration before a power-up disappears. */
    protected static final long POWERUP_TIMEOUT =
            TimeUnit.SECONDS.toNanos(5);
    /** Tuning: the number of points added for each collected power-up. */
    private static final int POWERUP_BASE_SCORE = 100;
    /** Path to the power-up collected sound effect. */
    private static final String SFX_POWERUP_COLLECT = "sfx_powerup_collect.wav";
    /** Whether this power-up was collected. */
    private boolean collected;

    /**
     * Constructs an instance of Powerup.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param parent the LevelScene object owning this entity.
     */
    public Powerup(int x, int y, LevelScene parent) {
        super(x, y, parent);

        parent.getActions().add(POWERUP_TIMEOUT, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                collected = true;
                return true;
            }
        });
    }

    @Override
    public void update(long now) {
        if (this.collected) {
            return;
        }

        super.update(now);

        if (this.getParent().getOutlaw().isAlive()) {
            if (this.intersects(this.getParent().getOutlaw())) {
                this.collected = true;
                this.applyPowerup();
                this.getParent().getOutlaw().spawnPowerupEffect();
                this.getParent().addScore(POWERUP_BASE_SCORE);
                Game.playSFX(SFX_POWERUP_COLLECT);
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (this.collected) {
            return;
        }

        super.draw(gc);
    }

    /**
     * Applies the effects associated with this power-up.
     */
    public abstract void applyPowerup();

    /**
     * Retrieves whether this power-up was collected.
     * @return a boolean.
     */
    public boolean getCollected() {
        return this.collected;
    }

}
