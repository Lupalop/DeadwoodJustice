package game.entities;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.LevelScene;
import javafx.scene.canvas.GraphicsContext;

public abstract class Mob extends Sprite implements LevelUpdatable {

    public static final int TOTAL_MOBS = 3;
    public static final int MIN_MOB_SPEED = 1;
    public static final int MAX_MOB_SPEED = 5;

    private final static long FRAME_DEATH_INTERVAL = 
            TimeUnit.MILLISECONDS.toNanos(300);
    private static final long MOB_SHOOT_INTERVAL =
            TimeUnit.SECONDS.toNanos(1);

    public final static int MAX_DAMAGE = 40;
    public final static int MIN_DAMAGE = 30;

    private boolean isAlive;
    private boolean isDying;

    private long lastShootTime;
    private boolean canShoot;
    
    protected boolean moveRight;
    private Effect deathEffect;

    private int health;
    private int damage;
    private int speed;
    private boolean isMaxSpeed;
    private boolean isSlowSpeed;
    private boolean isZeroSpeed;
    private boolean isDeadOnPlayerImpact;
    private boolean isChasingPlayer;
    protected boolean isExcludedFromMaxSpeed;
    private boolean isPlayerInMobBounds;
    private boolean isStuck;
    private ArrayList<Bullet> bullets;

    private int[] frameRanges;

    public Mob(int x, int y, int health, int damage, boolean canShoot) {
        super(x, y);
        this.health = health;
        this.isDeadOnPlayerImpact = true;
        this.isAlive = true;
        this.isDying = false;

        Random rand = new Random();
        this.speed = rand.nextInt(MIN_MOB_SPEED, MAX_MOB_SPEED);
        this.moveRight = rand.nextBoolean();
        this.flipHorizontal(!this.moveRight);
        if (damage <= -1) {
            this.damage = rand.nextInt(MIN_DAMAGE, MAX_DAMAGE + 1);
        } else {
            this.damage = damage;
        }
        this.isMaxSpeed = false;
        this.isSlowSpeed = false;
        this.isZeroSpeed = false;
        this.isExcludedFromMaxSpeed = false;
        this.isPlayerInMobBounds = false;
        this.isStuck = false;
        this.canShoot = canShoot;
        if (this.canShoot) {
            this.bullets = new ArrayList<Bullet>();
            this.lastShootTime = System.nanoTime();
        }
        
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
        if (this.isSlowSpeed) {
            this.dx = MIN_MOB_SPEED;
        }
        // Zero speed power-up takes precedence over slow speed power-up.
        if (this.isZeroSpeed) {
            this.dx = 0;
        }

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
        } else {
            // Regular mobs are only allowed to move from left to right.
            if (this.dy != 0) {
                this.dy = 0;
            }
            // Check for passability if we're not stuck.
            if (!this.isStuck) {
                if (!passableX[0] && !passableX[1]) {
                    this.dx = 0;
                    this.isStuck = true;
                } else if (!passableX[this.moveRight ? 1 : 0]) {
                    this.dx = 0;
                    this.changeDirection();
                    this.isStuck = true;
                }
            // Stop marking as stuck if one side is now passable.
            } else if (passableX[0] || passableX[1]) {
                this.isStuck = false;
            }
        }

        this.addX(this.moveRight ? this.dx : -this.dx);
        this.addY(dy);
    }

    boolean passableX[] = new boolean[2];
    boolean passableY[] = new boolean[2];

    public void update(long currentNanoTime, LevelScene level) {
        this.isMaxSpeed = level.isMaxSpeed();
        this.isSlowSpeed = level.isSlowSpeed();
        this.isZeroSpeed = level.isZeroSpeed();
        this.update(currentNanoTime);

        if (this.canShoot) {
            // Keep a list containing bullets to be removed.
            ArrayList<Bullet> removalList = new ArrayList<Bullet>();
    
            // Loop through the bullet list and remove used bullets.
            for (Bullet bullet : this.getBullets()) {
                bullet.update(currentNanoTime);
                if (!bullet.getVisible()) {
                    removalList.add(bullet);
                }
            }
            
            this.getBullets().removeAll(removalList);
        }

        if (!this.isAlive()) {
            return;
        }

        passableX[0] = this.getBounds().getMinX() >= 0;
        passableX[1] = this.getBounds().getMaxX() <= Game.WINDOW_WIDTH;
        
        passableY[0] = this.getBounds().getMinY() >= 0;
        passableY[1] = this.getBounds().getMaxY() <= Game.WINDOW_HEIGHT;

        for (Sprite sprite : level.getSprites()) {
            if (sprite == this) {
                continue;
            } else if (sprite instanceof Outlaw) {
                checkOutlaw((Outlaw)sprite);
                continue;
            }

            if (!Game.FLAG_MOBS_CHECK_PASSABILITY
                    || (Game.FLAG_IGNORE_PROP_COLLISION && sprite instanceof Prop)
                    || (sprite instanceof Mob && !((Mob)sprite).isAlive()) 
                    || sprite instanceof Powerup
                    || sprite instanceof StatusHUD) {
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

        if (this.canShoot) {
            // Shoot every n seconds.
            long deltaTime = (currentNanoTime - lastShootTime);
            if (deltaTime >= MOB_SHOOT_INTERVAL) {
                this.shoot();
                this.lastShootTime = currentNanoTime;
            }
        }
    }
    
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        if (this.canShoot) {
            for (Bullet bullet : this.getBullets()) {
                bullet.draw(gc);
            }
        }
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
        
        if (this.canShoot) {
            for (Bullet bullet : this.getBullets()) {
                if (bullet.getVisible() && outlaw.intersects(bullet)) {
                    outlaw.reduceStrength(this.damage);
                    bullet.setVisible(false);
                }
            }
        }
    }
    
    private void prepareDeath() {
        this.isAlive = false;
        this.isDying = true;
        this.setFrameAutoReset(false);
        this.setFrameInterval(FRAME_DEATH_INTERVAL);
        this.setMinMaxFrame(frameRanges[6], frameRanges[7]);
        this.deathEffect = new ExplosionEffect(this);
    }

    public void changeDirection() {
        this.flipHorizontal(this.moveRight);
        this.moveRight = !this.moveRight;
        int multiplier = this.moveRight ? 1 : -1;
        this.addX((int) this.getBounds().getWidth() / 2 * multiplier);
    }

    public ArrayList<Bullet> getBullets() {
        return this.bullets;
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

        if (outlaw.intersects(this, true, false) || this.isZeroSpeed) {
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

    public void shoot() {
        if (!this.isAlive() || !this.canShoot) {
            return;
        }

        int x = (int) (this.getBounds().getMaxX());
        int y = (int) (this.getBounds().getMinY()
            + (this.getBounds().getHeight() / 2)
            - (Bullet.BULLET_IMAGE.getHeight() / 2));

        // compute for the x and y initial position of the bullet
        byte activeDirections = this.moveRight
                ? Game.KEY_DIR_RIGHT
                : Game.KEY_DIR_LEFT;
        Bullet bullet = new Bullet(x, y, activeDirections, true);
        this.bullets.add(bullet);
        this.playFrames(frameRanges[8], frameRanges[9], null, 50);
    }

}
