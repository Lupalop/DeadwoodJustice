package game.entities;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;

public abstract class Mob extends Sprite {

    public static final int MAX_MOB_SPEED = 5;

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

    private int[] frameRanges;

    public Mob(int x, int y) {
        super(x, y);
    }

    protected void initialize() {
        this.setScale(2);
        this.setMinMaxFrame(frameRanges[0], frameRanges[1]);

        this.isAlive = true;
        this.isDying = false;

        Random rand = new Random();
        this.speed = rand.nextInt(1, MAX_MOB_SPEED);
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

        this.dx = this.isMaxSpeed ? MAX_MOB_SPEED : this.speed;

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

    public void update(long currentNanoTime, Outlaw outlaw, ArrayList<Mob> otherMobs, boolean isMaxSpeed) {
        this.isMaxSpeed = isMaxSpeed;
        this.update(currentNanoTime);

        if (!this.isAlive()) {
            return;
        }
        
        if (this.intersects(outlaw) && outlaw.isAlive()) {
            outlaw.reduceStrength(this.damage);
            this.prepareDeath();
            return;
        }

        for (Bullet bullet : outlaw.getBullets()) {
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
        this.setMinMaxFrame(frameRanges[2], frameRanges[3]);
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

    public void setFrameRanges(int[] frameRanges) {
        this.frameRanges = frameRanges;
    }

}
