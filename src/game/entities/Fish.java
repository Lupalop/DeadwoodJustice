package game.entities;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import javafx.scene.image.Image;

public class Fish extends Sprite {

    public static final int MAX_FISH_SPEED = 5;

    public final static Image FRAMESET_W = new Image(
            Game.getAsset("cactus_sheet_w.png"));
    public final static int FRAMESET_ROWS = 4;
    public final static int FRAMESET_COLUMNS = 11;
    public final static int[] FRAMESET_OFFSET = 
            new int[] { 10, 12, 10, 1 };
    
    private final static long FRAME_DEATH_INTERVAL = 
            TimeUnit.MILLISECONDS.toNanos(300);
    
    public final static int MAX_DAMAGE = 40;
    public final static int MIN_DAMAGE = 30;
    
    private boolean isAlive;
    private boolean isDying;

    private boolean moveRight;
    private int speed;
    private int damage;
    private boolean isMaxSpeed;

    public Fish(int x, int y) {
        super(x, y);
        
        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(11, 19);
        this.setScale(2);
        this.setBoundsOffset(FRAMESET_OFFSET);

        this.isAlive = true;
        this.isDying = false;

        Random rand = new Random();
        this.speed = rand.nextInt(1, MAX_FISH_SPEED);
        this.moveRight = rand.nextBoolean();
        this.flipHorizontal(!this.moveRight);
        this.damage = rand.nextInt(MIN_DAMAGE, MAX_DAMAGE + 1);
        this.isMaxSpeed = false;
    }

    public void update(long currentNanoTime) {
        super.update(currentNanoTime);

        if (this.isDying) {
            if (this.isFrameSequenceDone()) {
                this.isDying = false;
            }
        }

        if (!this.isAlive()) {
            return;
        }

        this.dx = this.isMaxSpeed ? MAX_FISH_SPEED : this.speed;

        int nextX = (int) (getBounds().getMinX() + dx);
        boolean changeFromRight = this.moveRight
                && nextX >= Game.WINDOW_WIDTH - this.getBounds().getWidth();
        boolean changeFromLeft = !this.moveRight
                && nextX <= 0;
        if (changeFromRight || changeFromLeft) {
            this.changeDirection();
        }
        this.addX(this.moveRight ? this.dx : -this.dx);
    }

    public void update(long currentNanoTime, Ship ship, ArrayList<Fish> otherFishes, boolean isMaxSpeed) {
        this.isMaxSpeed = isMaxSpeed;
        this.update(currentNanoTime);

        if (!this.isAlive()) {
            return;
        }
        
        if (this.intersects(ship) && ship.isAlive()) {
            ship.reduceStrength(this.damage);
            this.prepareDeath();
            this.setMinMaxFrame(33, 36);
            return;
        }

        for (Bullet bullet : ship.getBullets()) {
            if (this.intersects(bullet)) {
                this.prepareDeath();
                bullet.setVisible(false);
                break;
            }
        }
    }
    
    private void prepareDeath() {
        this.isAlive = false;
        this.isDying = true;
        this.setFrameAutoReset(false);
        this.setFrameInterval(FRAME_DEATH_INTERVAL);
        this.setMinMaxFrame(33, 36);
    }

    public void changeDirection() {
        this.flipHorizontal(this.moveRight);
        this.moveRight = !this.moveRight;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public boolean isDying() {
        return this.isDying;
    }
    
}
