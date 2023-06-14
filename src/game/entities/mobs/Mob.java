package game.entities.mobs;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.UIUtils;
import game.entities.Bullet;
import game.entities.FrameRange;
import game.entities.LevelSprite;
import game.entities.Mote;
import game.entities.Outlaw;
import game.entities.Sprite;
import game.entities.effects.Effect;
import game.entities.effects.ExplosionEffect;
import game.entities.effects.TornadoEffect;
import game.scenes.LevelScene;
import javafx.scene.canvas.GraphicsContext;

public abstract class Mob extends LevelSprite {

    public static final int TOTAL_MOBS = 3;

    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 5;
    public static final int MAX_DAMAGE = 40;
    public static final int MIN_DAMAGE = 30;

    private static final long FRAME_DEATH_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(300);
    private static final int MOB_SHOOT_INTERVAL_MIN = 1;
    private static final int MOB_SHOOT_INTERVAL_MAX = 3;

    private static final String SFX_DEAD_MOB = "sfx_dead_mob.wav";

    private int health;
    private int damage;
    private int speed;

    private boolean alive;
    private boolean dying;
    private boolean deadOnPlayerImpact;
    private boolean chasingPlayer;
    private boolean playerInMobBounds;
    private boolean movingStuck;
    private boolean movingRight;
    private boolean shooter;

    private boolean passability[];
    private FrameRange frameRange;

    private Effect deathEffect;
    private Effect zeroSpeedEffect;

    public Mob(int x, int y, int health, int damage, LevelScene parent) {
        super(x, y, parent);

        this.health = health;
        if (damage <= -1) {
            this.damage = Game.RNG.nextInt(MIN_DAMAGE, MAX_DAMAGE + 1);
        } else {
            this.damage = damage;
        }
        this.speed = Game.RNG.nextInt(MIN_SPEED, MAX_SPEED);

        this.alive = true;
        this.dying = false;
        this.deadOnPlayerImpact = true;
        this.chasingPlayer = false;
        this.playerInMobBounds = false;
        this.movingStuck = false;
        this.movingRight = Game.RNG.nextBoolean();
        this.shooter = this.getShootingCapability();

        this.passability = new boolean[4];
        this.frameRange = null;

        if (this.shooter) {
            // Shoot every n seconds.
            long shootInterval = TimeUnit.SECONDS.toNanos(Game.RNG.nextInt(
                    MOB_SHOOT_INTERVAL_MIN, MOB_SHOOT_INTERVAL_MAX + 1));
            this.getParent().getActions().add(shootInterval, true, new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    if (isAlive() && !getParent().isZeroSpeed()) {
                        shoot();
                    }
                    return true;
                }
            });
        }

        if (this.getParent().getDifficulty() > LevelScene.DIFFICULTY_EASY) {
            this.chasingPlayer = Game.RNG.nextBoolean();
        }

        this.setFlip(!this.movingRight, false);
    }

    protected void initialize() {
        this.frameRange.playWalk(this);
    }

    @Override
    public void update(long now) {
        super.update(now);

        if (this.deathEffect != null) {
            this.deathEffect.update(now);
        } else if (this.zeroSpeedEffect != null) {
            this.zeroSpeedEffect.update(now);
        }

        if (this.dying) {
            if (this.isFrameSequenceDone()) {
                this.dying = false;
            }
        }

        if (!this.isAlive()) {
            return;
        }

        if (this.getParent().isZeroSpeed()) {
            if (this.zeroSpeedEffect == null || this.zeroSpeedEffect.isFrameSequenceDone()) {
                this.zeroSpeedEffect = new TornadoEffect(this);
            }
        }

        moveMob();
        checkOutlawCollision(this.getParent().getOutlaw());
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        if (this.deathEffect != null) {
            this.deathEffect.draw(gc);
        } else if (this.zeroSpeedEffect != null) {
            this.zeroSpeedEffect.draw(gc);
        }

        if (Game.DEBUG_MODE && !hideWireframe) {
            UIUtils.drawPassability(gc, this, passability);
        }
    }

    private boolean steeringUp;
    private boolean steeringDown;

    private void tryMovingUp(boolean targetPassable, int speed) {
        // Don't bother moving up if it's impassable.
        if (!passability[SIDE_TOP]) {
            if (passability[SIDE_BOTTOM]) {
                tryMovingDown(targetPassable, speed);
            }
            // Bail. None of the vertical sides are passable.
            return;
        }

        if (!steeringUp) {
            this.steeringUp = true;
        }

        if (targetPassable) {
            this.steeringUp = false;
            this.dy = 0;
        } else {
            this.dy = -speed;
        }
    }

    private void tryMovingDown(boolean passableX, int speed) {
        // Don't bother moving down if it's impassable.
        if (!passability[SIDE_BOTTOM]) {
            if (passability[SIDE_TOP]) {
                tryMovingUp(passableX, speed);
            }
            // Bail. None of the vertical sides are passable.
            return;
        }

        if (!steeringDown) {
            this.steeringDown = true;
        }

        if (passableX) {
            this.steeringDown = false;
            this.dy = 0;
        } else {
            this.dy = speed;
        }
    }

    private void tryMovingYToTarget(Sprite target, int speed) {
        if (target.intersects(this, true, false, true)) {
            this.dy = 0;
        } else if (target.getBounds().getMinY() > this.getBounds().getMinY()) {
            this.dy = speed;
        } else {
            this.dy = -speed;
        }
    }

    private void moveMob() {
        int currentSpeed = this.getParent().isMaxSpeed()
                ? MAX_SPEED
                : this.speed;
        if (this.getParent().isSlowSpeed()) {
            currentSpeed = MIN_SPEED;
        }
        // Zero speed power-up takes precedence over slow speed power-up.
        if (this.getParent().isZeroSpeed()) {
            currentSpeed = 0;
        }
        this.dx = currentSpeed;

        int nextX = (int) (getBounds().getMinX() + dx);
        boolean changeFromRight = this.movingRight
                && nextX >= Game.WINDOW_MAX_WIDTH - this.getBounds().getWidth();
        boolean changeFromLeft = !this.movingRight
                && nextX <= 0;
        if (changeFromRight || changeFromLeft) {
            this.changeDirection();
        }

        Outlaw outlaw = this.getParent().getOutlaw();
        if (this.chasingPlayer && outlaw.isAlive()) {
            int sideX = this.movingRight
                    ? SIDE_RIGHT
                    : SIDE_LEFT;
            boolean passableX = passability[sideX];
            if (!passableX) {
                this.dx = 0;
            }

            // This is a good-enough substitute for implementing a
            // full-blown pathfinder. We check if we're steering in a
            // certain direction and decide where to move accordingly.
            if (this.steeringUp) {
                tryMovingUp(passableX, currentSpeed);
            } else if (this.steeringDown) {
                tryMovingDown(passableX, currentSpeed);
            } else if (!passableX) {
                if (Game.RNG.nextBoolean()) {
                    tryMovingUp(passableX, currentSpeed);
                } else {
                    tryMovingDown(passableX, currentSpeed);
                }
            } else {
                tryMovingYToTarget(outlaw, currentSpeed);
            }

            if (!outlaw.intersects(this, false, true, false)) {
                if (outlaw.getBounds().getMinX() > this.getBounds().getMinX()) {
                    if (!this.movingRight) {
                        this.changeDirection();
                    }
                } else if (this.movingRight) {
                    this.changeDirection();
                }
            }

            if (this.getParent().isZeroSpeed()
                    || !passability[SIDE_TOP] && this.dy <= 0
                    || !passability[SIDE_BOTTOM] && this.dy >= 0) {
                this.dy = 0;
            }
        } else {
            // Regular mobs are only allowed to move from left to right.
            if (this.dy != 0) {
                this.dy = 0;
            }
            // Check for passability if we're not stuck.
            if (!this.movingStuck) {
                if (!passability[SIDE_LEFT] && !passability[SIDE_RIGHT]) {
                    this.dx = 0;
                    this.movingStuck = true;
                } else if (!passability[this.movingRight ? 1 : 0]) {
                    this.dx = 0;
                    this.changeDirection();
                    this.movingStuck = true;
                }
            // Stop marking as stuck if one side is now passable.
            } else if (passability[SIDE_LEFT] && passability[SIDE_RIGHT]) {
                this.movingStuck = false;
            }
        }

        this.addX(this.movingRight ? this.dx : -this.dx);
        this.addY(dy);

        this.passability = this.getParent().getLevelMap().getPassability(this);
    }

    private void checkOutlawCollision(Outlaw outlaw) {
        if (!outlaw.isAlive()) {
            return;
        }

        if (this.intersects(outlaw)) {
            if (this.playerInMobBounds) {
                return;
            }
            outlaw.reduceStrength(this.damage);
            if (this.deadOnPlayerImpact) {
                this.getParent().addScore(outlaw.getStrength() / 4);
                this.prepareDeath();
            } else {
                this.frameRange.playImpact(this);
            }
            this.playerInMobBounds = true;
            return;
        } else {
            this.playerInMobBounds = false;
        }
    }

    private void prepareDeath() {
        this.alive = false;
        this.dying = true;
        this.setFrameAutoReset(false);
        this.setFrameInterval(FRAME_DEATH_INTERVAL);
        this.frameRange.playDeath(this);
        this.deathEffect = new ExplosionEffect(this);
        Game.playSFX(SFX_DEAD_MOB, 0.3);
    }

    public void changeDirection() {
        this.setFlip(this.movingRight, false);
        this.movingRight = !this.movingRight;
        int multiplier = this.movingRight ? 1 : -1;
        this.addX((int) this.getBounds().getWidth() / 2 * multiplier);
    }

    protected boolean getShootingCapability() {
        // On higher difficulties, the mob's shooting capability is randomized.
        boolean allowMobShooting = !getParent().getRestrictedMode();
        return (allowMobShooting && Game.RNG.nextBoolean());
    }

    protected void shoot() {
        if (!this.isAlive() || !this.shooter) {
            return;
        }

        byte activeDirections = this.movingRight
                ? Game.DIR_RIGHT
                : Game.DIR_LEFT;
        Bullet bullet = new Bullet(this, getParent(), activeDirections, true);
        this.getParent().getLevelMap().addSpriteOnUpdate(bullet);
        this.frameRange.playShoot(this);
    }

    public int getDamage() {
        return this.damage;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public boolean isDying() {
        return this.dying;
    }

    public boolean isMovingRight() {
        return this.movingRight;
    }

    public void reduceHealth(int value) {
        // This must be a positive integer.
        if (value < 0) {
            return;
        }
        this.health -= value;
        this.getParent().addScore(value / 3);

        byte moteType = Mote.TYPE_NEUTRAL;
        if (this.health <= 0) {
            this.prepareDeath();
            moteType = Mote.TYPE_BAD;
        } else {
            this.frameRange.playDamage(this);
        }

        this.getParent().spawnMote(this, value, moteType);
    }

    protected void setDeadOnPlayerImpact(boolean value) {
        this.deadOnPlayerImpact = value;
    }

    protected void setChasingPlayer(boolean value) {
        this.chasingPlayer = value;
    }

    protected void setFrameRange(FrameRange frameRange) {
        this.frameRange = frameRange;
    }

}
