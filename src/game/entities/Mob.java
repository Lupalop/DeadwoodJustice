package game.entities;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import javafx.scene.canvas.GraphicsContext;

public abstract class Mob extends Sprite {

    public static final int TOTAL_MOBS = 3;
    public static final int MAX_MOB_SPEED = 5;

    private final static long FRAME_DEATH_INTERVAL = 
            TimeUnit.MILLISECONDS.toNanos(300);

    public final static int MAX_DAMAGE = 40;
    public final static int MIN_DAMAGE = 30;

    private boolean isAlive;
    private boolean isDying;

    protected boolean moveRight;
    private Sprite deathEffect;

    private int health;
    private int damage;
    private int speed;
    private boolean isMaxSpeed;
    private boolean isDeadOnPlayerImpact;
    private boolean isChasingPlayer;
    protected boolean isExcludedFromMaxSpeed;
    private boolean isPlayerInMobBounds;
    private boolean isStuck;

    private int[] frameRanges;

    public Mob(int x, int y, int health, int damage) {
        super(x, y);
        this.health = health;
        this.isDeadOnPlayerImpact = true;
        this.isAlive = true;
        this.isDying = false;

        Random rand = new Random();
        this.speed = rand.nextInt(1, MAX_MOB_SPEED);
        this.moveRight = rand.nextBoolean();
        this.flipHorizontal(!this.moveRight);
        if (damage <= -1) {
            this.damage = rand.nextInt(MIN_DAMAGE, MAX_DAMAGE + 1);
        } else {
            this.damage = damage;
        }
        this.isMaxSpeed = false;
        this.isExcludedFromMaxSpeed = false;
        this.isPlayerInMobBounds = false;
        this.isStuck = false;
        
        if (Game.FLAG_SMARTER_MOBS) {
            this.setIsChasingPlayer(rand.nextBoolean());
        }
    }

    protected void initialize() {
        this.setScale(2);
        this.setMinMaxFrame(frameRanges[0], frameRanges[1]);
    }
    
    public void update(long currentNanoTime) {
        super.update(currentNanoTime);

        if (this.deathEffect != null) {
            this.deathEffect.update(currentNanoTime);
        }

        if (this.isDying) {
            if (this.isFrameSequenceDone()) {
                this.isDying = false;
            }
        }

        if (!this.isAlive()) {
            return;
        }

        this.dx = (this.isMaxSpeed && !this.isExcludedFromMaxSpeed)
                ? MAX_MOB_SPEED
                : this.speed;

        int nextX = (int) (getBounds().getMinX() + dx);
        boolean changeFromRight = this.moveRight
                && nextX >= Game.WINDOW_WIDTH - this.getBounds().getWidth();
        boolean changeFromLeft = !this.moveRight
                && nextX <= 0;
        if (changeFromRight || changeFromLeft) {
            this.changeDirection();
        }

        if (this.isChasingPlayer) {
            if (!passableX[this.moveRight ? 1 : 0]) {
                this.dx = 0;
            }
            if (!passableY[0] && this.dy >= 0 || !passableY[1] && this.dy <= 0) {
                this.dy = 0;
            }
            this.addY(dy);
        } else {
            if (!passableX[0] && !passableX[1] && !this.isStuck) {
                this.dx = 0;
                this.isStuck = true;
            } else if (!passableX[this.moveRight ? 1 : 0] && !this.isStuck) {
                this.dx = 0;
                this.changeDirection();
                this.isStuck = true;
            } else {
                this.dy = 0;
                this.isStuck = false;
            }

            this.addY(passableY[1] ? this.dy : -this.dy);
        }

        this.addX(this.moveRight ? this.dx : -this.dx);
    }

    boolean passableX[] = new boolean[2];
    boolean passableY[] = new boolean[2];

    public void update(long currentNanoTime, ArrayList<Sprite> sprites, boolean isMaxSpeed) {
        this.isMaxSpeed = isMaxSpeed;
        this.update(currentNanoTime);

        if (!this.isAlive()) {
            return;
        }

        passableX[0] = this.getBounds().getMinX() >= 0;
        passableX[1] = this.getBounds().getMaxX() <= Game.WINDOW_WIDTH;
        
        passableY[0] = this.getBounds().getMinY() >= 0;
        passableY[1] = this.getBounds().getMaxY() <= Game.WINDOW_HEIGHT;

        for (Sprite sprite : sprites) {
            if (sprite == this) {
                continue;
            } else if (sprite instanceof Outlaw) {
                checkOutlaw((Outlaw)sprite);
                continue;
            }

            if (!Game.FLAG_MOBS_CHECK_PASSABILITY) {
                continue;
            }
            
            int side = this.baseIntersectsSide(sprite.getBaseBounds());
            if (passableX[0] && side == 0) {
                passableX[0] = false;
            } else if (passableX[1] && side == 1) {
                passableX[1] = false;
            } else if (passableY[0] && side == 2) {
                passableY[0] = false;
            } else if (passableY[1] && side == 3) {
                passableY[1] = false;
            }
        }
    }
    
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        if (this.deathEffect != null) {
            this.deathEffect.draw(gc);
        }
    }

    private void checkOutlaw(Outlaw outlaw) {
        if (this.isChasingPlayer) {
            if (!outlaw.isAlive()) {
                this.setIsChasingPlayer(false);
            }
            this.chasePlayer(outlaw);
        }
        
        if (outlaw.isAlive()) {
            if (this.intersects(outlaw)) {
                if (this.isPlayerInMobBounds) {
                    return;
                }
                outlaw.reduceStrength(this.damage);
                if (this.isDeadOnPlayerImpact) {
                    this.prepareDeath();
                } else {
                    this.playFrames(frameRanges[2], frameRanges[3], null, 0);
                }
                this.isPlayerInMobBounds = true;
                return;
            } else {
                this.isPlayerInMobBounds = false;
            }
        }

        for (Bullet bullet : outlaw.getBullets()) {
            if (bullet.getVisible() && this.intersects(bullet)) {
                this.health -= outlaw.getStrength();
                if (this.health <= 0) {
                    this.prepareDeath();
                } else {
                    this.playFrames(frameRanges[4], frameRanges[5], null, 0);
                }
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
        this.setMinMaxFrame(frameRanges[6], frameRanges[7]);
        this.deathEffect = new ExplosionEffect(0, this.getY());
        this.deathEffect.setX(
                (int) (this.getBounds().getMinX()
                        + (this.getBounds().getWidth() / 2)
                        - this.deathEffect.getWidth()));
    }

    public void changeDirection() {
        this.flipHorizontal(this.moveRight);
        this.moveRight = !this.moveRight;
        int multiplier = this.moveRight ? 1 : -1;
        this.addX((int) this.getBounds().getWidth() / 2 * multiplier);
    }

    protected void setIsDeadOnPlayerImpact(boolean value) {
        this.isDeadOnPlayerImpact = value;
    }
    
    public boolean isAlive() {
        return this.isAlive;
    }

    public boolean isDying() {
        return this.isDying;
    }

    public void setIsChasingPlayer(boolean value) {
        this.isChasingPlayer = value;
    }
    
    public void setFrameRanges(int[] frameRanges) {
        this.frameRanges = frameRanges;
    }

    private void chasePlayer(Outlaw outlaw) {
        if (!outlaw.isAlive()) {
            return;
        }
        
        if (!Game.FLAG_SMARTER_MOBS) {
            return;
        }

        if (outlaw.intersects(this, true, false)) {
            this.dy = 0;
        } else if (outlaw.getBounds().getMinY() > this.getBounds().getMinY()) {
            this.dy = this.speed;
        } else {
            this.dy = -this.speed;
        }
        
        if (!outlaw.intersects(this, false, true)) {
            if (outlaw.getBounds().getMinX() > this.getBounds().getMinX()) {
                if (!this.moveRight) {
                    this.changeDirection();
                }
            } else if (this.moveRight) {
                this.changeDirection();
            }
        }
    }
    
}
