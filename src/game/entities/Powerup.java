package game.entities;

import java.util.concurrent.TimeUnit;

import game.LevelScene;
import javafx.scene.canvas.GraphicsContext;

public abstract class Powerup extends LevelSprite {

    public static final int TOTAL_POWERUPS = 4;

    public static final long POWERUP_TIMEOUT =
            TimeUnit.SECONDS.toNanos(5);

    private long spawnTime;
    private boolean consumed;

    public Powerup(int x, int y, LevelScene parent) {
        super(x, y, parent);
        this.spawnTime = System.nanoTime();
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

        long deltaTime = (now - spawnTime);
        if (deltaTime >= POWERUP_TIMEOUT) {
            this.spawnTime = now;
            this.consumed = true;
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
