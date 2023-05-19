package game.entities;

import java.util.concurrent.TimeUnit;

import game.LevelScene;
import javafx.scene.canvas.GraphicsContext;

public abstract class Powerup extends Sprite implements LevelUpdatable {

    public static final int TOTAL_POWERUPS = 4;

    public static final long POWERUP_TIMEOUT =
            TimeUnit.SECONDS.toNanos(5);

    private long spawnTime;
    private boolean consumed;

    public Powerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.spawnTime = System.nanoTime();
    }

    @Override
    public void update(long now, LevelScene level) {
        if (this.consumed) {
            return;
        }

        super.update(now);

        if (level.getOutlaw().isAlive()) {
            if (this.intersects(level.getOutlaw())) {
                this.consumed = true;
                applyPowerup(level);
                level.getOutlaw().spawnPowerupEffect();
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

    public abstract void applyPowerup(LevelScene scene);

    public boolean getConsumed() {
        return this.consumed;
    }

    @Override
    public int compareTo(Sprite o) {
        return Integer.MAX_VALUE;
    }

}
