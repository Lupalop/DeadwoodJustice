package game.entities;

import java.util.concurrent.TimeUnit;

import game.scenes.LevelScene;
import javafx.scene.canvas.GraphicsContext;

public abstract class Powerup extends Sprite {

    public static final int TOTAL_POWERUPS = 3;
    public static final long POWERUP_TIMEOUT =
            TimeUnit.SECONDS.toNanos(5);
    
    public long spawnTime;
    
    public Powerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setScale(2);
        this.spawnTime = System.nanoTime();
    }
    
    private boolean consumed;
    
    public boolean getConsumed() {
        return this.consumed;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        if (this.consumed) {
            return;
        }

        super.draw(gc);
    }
    
    public void update(long currentNanoTime, LevelScene scene) {
        if (this.consumed) {
            return;
        }

        super.update(currentNanoTime);
        
        if (scene.getOutlaw().isAlive()) {
            if (this.intersects(scene.getOutlaw())) {
                this.consumed = true;
                doPowerup(scene);
            }
        }
        
        long deltaTime = (currentNanoTime - spawnTime);
        if (deltaTime >= POWERUP_TIMEOUT) {
            this.spawnTime = currentNanoTime;
            this.consumed = true;
        }
    }
    
    public abstract void doPowerup(LevelScene scene);

    @Override
    public int compareTo(Sprite o) {
        return (int) ((this.getBounds().getMaxY() * 5) - o.getBounds().getMaxY());
    }
}
