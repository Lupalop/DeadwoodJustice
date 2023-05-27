package game.entities;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.scenes.LevelScene;
import javafx.scene.canvas.GraphicsContext;

public abstract class Powerup extends LevelSprite {

    public static final int TOTAL_POWERUPS = 4;

    public static final long POWERUP_TIMEOUT =
            TimeUnit.SECONDS.toNanos(5);

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

    @Override
    public int compareTo(Sprite o) {
        return Integer.MAX_VALUE;
    }

}
