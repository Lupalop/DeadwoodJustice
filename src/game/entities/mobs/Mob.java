package game.entities.mobs;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.UIUtils;
import game.entities.Bullet;
import game.entities.Entity;
import game.entities.FrameRange;
import game.entities.Mote;
import game.entities.Outlaw;
import game.entities.effects.Effect;
import game.entities.effects.ExplosionEffect;
import game.entities.effects.TornadoEffect;
import game.scenes.LevelScene;
import javafx.scene.canvas.GraphicsContext;

/**
 * This class is the base class for all enemies in the game.
 * It provides common features that can be used by all mobs,
 * such as health and damage handling, and movement.
 * @author Francis Dominic Fajardo
 */
public abstract class Mob extends Entity {

    /** The total number of known mobs. */
    public static final int TOTAL_MOBS = 3;

    /** Tuning: minimum movement speed. */
    private static final int MIN_SPEED = 1;
    /** Tuning: maximum movement speed. */
    private static final int MAX_SPEED = 5;
    /** Tuning: minimum damage dealt on shoot. */
    private static final int MIN_DAMAGE = 30;
    /** Tuning: maximum damage dealt on shoot. */
    private static final int MAX_DAMAGE = 40;
    /** Tuning: death frame sequence change speed. */
    private static final long FRAME_DEATH_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(300);
    /** Tuning (seconds): minimum interval at which the mob shoots. */
    private static final int MOB_SHOOT_INTERVAL_MIN = 1;
    /** Tuning (seconds): maximum interval at which the mob shoots. */
    private static final int MOB_SHOOT_INTERVAL_MAX = 3;

    /** Path to the mob death sound effect. */
    private static final String SFX_DEAD_MOB = "sfx_dead_mob.wav";

    /** State: health. */
    private int health;
    /** State: damage dealt on shoot. */
    private int damage;
    /** State: movement speed. */
    private int speed;
    /** State: effective speed after applying level state. */
    private int currentSpeed;
    /** State: alive. */
    private boolean alive;
    /** State: dying. */
    private boolean dying;

    /** AI: mob should be dead on player collision. */
    private boolean deadOnPlayerImpact;
    /** AI: mob follows player movement. */
    private boolean chasingPlayer;
    /** AI: stop collision checks if we're in player bounds. */
    private boolean playerInMobBounds;
    /** AI: are we stuck? */
    private boolean movingStuck;
    /** AI: are we moving to the right. */
    private boolean movingRight;
    /** AI: are we allowed to shoot? */
    private boolean shooter;
    /** AI: while following player, are we steering up? */
    private boolean steeringUp;
    /** AI: while following player, are we steering down? */
    private boolean steeringDown;

    /** Passability of surrounding tiles. */
    private boolean passability[];
    /** Sprite frame range. */
    private FrameRange frameRange;
    /** Effect: death. */
    private Effect deathEffect;
    /** Effect: zero speed. */
    private Effect zeroSpeedEffect;

    /**
     * Constructs an instance of Mob.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param health initial mob health.
     * @param damage initial mob damage, -1 if random.
     * @param parent the LevelScene object owning this entity.
     */
    public Mob(int x, int y, int health, int damage, LevelScene parent) {
        super(x, y, parent);

        this.health = health;
        // Randomize damage if an invalid initial value was provided.
        if (damage <= -1) {
            this.damage = Game.RNG.nextInt(MIN_DAMAGE, MAX_DAMAGE + 1);
        } else {
            this.damage = damage;
        }
        this.speed = Game.RNG.nextInt(MIN_SPEED, MAX_SPEED);
        this.currentSpeed = this.speed;
        this.alive = true;
        this.dying = false;
        this.deadOnPlayerImpact = true;
        this.chasingPlayer = false;
        this.playerInMobBounds = false;
        this.movingStuck = false;
        this.movingRight = Game.RNG.nextBoolean();
        this.shooter = this.getShootingCapability();
        this.steeringUp = false;
        this.steeringDown = false;

        this.passability = new boolean[4];
        this.frameRange = null;
        this.deathEffect = null;
        this.zeroSpeedEffect = null;

        // Create an action timer for shooting if it's allowed.
        if (this.shooter) {
            // Shoot every X seconds.
            long shootInterval = TimeUnit.SECONDS.toNanos(Game.RNG.nextInt(
                    MOB_SHOOT_INTERVAL_MIN, MOB_SHOOT_INTERVAL_MAX + 1));
            this.getParent().getTimers().add(shootInterval, true, new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    if (isAlive() && !getParent().isZeroSpeed()) {
                        shoot();
                    }
                    return true;
                }
            });
        }
        // Allow mob to chase the player if we're not in a restricted mode.
        if (!this.getParent().getRestrictedMode()) {
            this.chasingPlayer = Game.RNG.nextBoolean();
        }
        // Flip the mob's sprite to the right direction.
        this.setFlip(!this.movingRight, false);
    }

    /**
     * Initializes this Mob.
     */
    protected void initialize() {
        this.frameRange.playWalk(this);
    }

    @Override
    public void update(long now) {
        super.update(now);
        // Update effects if available.
        if (this.deathEffect != null) {
            this.deathEffect.update(now);
        } else if (this.zeroSpeedEffect != null) {
            this.zeroSpeedEffect.update(now);
        }
        // Remove this mob if the death frame sequence is done.
        if (this.dying) {
            if (this.isFrameSequenceDone()) {
                this.dying = false;
                this.remove();
            }
        }
        // Don't bother doing anything else if this mob is dead.
        if (!this.isAlive()) {
            return;
        }
        // Spawn a zero speed effect if it's active in the level.
        if (this.getParent().isZeroSpeed()) {
            if (this.zeroSpeedEffect == null || this.zeroSpeedEffect.isFrameSequenceDone()) {
                this.zeroSpeedEffect = new TornadoEffect(this);
            }
        }
        // Update mob positions and check for collisions.
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

    /**
     * NAI: Moves this mob up.
     * @implNote This goes to the opposite direction if it is impassable.
     * @param targetPassable whether the x-target direction is passable.
     * @param speed speed to move towards this direction.
     */
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

    /**
     * NAI: Moves this mob down.
     * @implNote This goes to the opposite direction if it is impassable.
     * @param targetPassable whether the x-target direction is passable.
     * @param speed speed to move towards this direction.
     */
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

    /**
     * NAI: Follow entity y-target direction, ignoring passability.
     * @param target the entity to follow.
     * @param speed speed to move towards this direction.
     */
    private void tryMovingYToTarget(Entity target, int speed) {
        if (target.intersects(this, true, false, true)) {
            this.dy = 0;
        } else if (target.getBounds().getMinY() > this.getBounds().getMinY()) {
            this.dy = speed;
        } else {
            this.dy = -speed;
        }
    }

    /**
     * NAI: Follows the player's movement and evaluate passability.
     * @return a boolean indicating if movement was successful.
     */
    private boolean chasePlayer() {
        Outlaw outlaw = this.getParent().getOutlaw();
        // Return early if this mob cannot follow the player or if
        // the player is already dead.
        if (!this.chasingPlayer || !outlaw.isAlive()) {
            return false;
        }
        // Check if the horizontal direction is passable and
        // adjust speed accordingly.
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
        // Change x-direction based on the player's x-position.
        if (!outlaw.intersects(this, false, true, false)) {
            if (outlaw.getBounds().getMinX() > this.getBounds().getMinX()) {
                if (!this.movingRight) {
                    this.changeDirection();
                }
            } else if (this.movingRight) {
                this.changeDirection();
            }
        }
        // Stop y-movement if the level says so.
        if (this.getParent().isZeroSpeed()
                || !passability[SIDE_TOP] && this.dy <= 0
                || !passability[SIDE_BOTTOM] && this.dy >= 0) {
            this.dy = 0;
        }
        return true;
    }

    /**
     * NAI: Evaluates x-direction passability if not in a restricted level.
     */
    private void wanderEdges() {
        // Don't do anything if we're in a restricted level.
        if (this.getParent().getRestrictedMode()) {
            return;
        }
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

    /**
     * Computes for the effective "real" speed.
     */
    private void computeCurrentSpeed() {
        this.currentSpeed = this.getParent().isMaxSpeed()
                ? MAX_SPEED
                : this.speed;
        if (this.getParent().isSlowSpeed()) {
            this.currentSpeed = MIN_SPEED;
        }
        // Zero speed power-up takes precedence over slow speed power-up.
        if (this.getParent().isZeroSpeed()) {
            this.currentSpeed = 0;
        }
        this.dx = this.currentSpeed;
    }

    /**
     * Moves this mob.
     */
    private void moveMob() {
        computeCurrentSpeed();
        // Change direction if we're at screen bounds.
        int nextX = (int) (getBounds().getMinX() + dx);
        boolean changeFromRight = this.movingRight
                && nextX >= Game.WINDOW_MAX_WIDTH - this.getBounds().getWidth();
        boolean changeFromLeft = !this.movingRight
                && nextX <= 0;
        if (changeFromRight || changeFromLeft) {
            this.changeDirection();
        }
        // Try to follow the player's movement. Otherwise, just move
        // from left to right if we're not restricted.
        if (!chasePlayer()) {
            wanderEdges();
        }
        // Update final XY location.
        this.addX(this.movingRight ? this.dx : -this.dx);
        this.addY(dy);
        // Update passability state for this entity.
        this.passability = this.getParent().getLevelMap().getPassability(this);
    }

    /**
     * Checks if this mob has collided with the player character.
     * @param outlaw the player character.
     */
    private void checkOutlawCollision(Outlaw outlaw) {
        // Don't bother if the player's dead.
        if (!outlaw.isAlive()) {
            return;
        }

        if (this.intersects(outlaw)) {
            // Stop collision checking if the player is still intersecting.
            if (this.playerInMobBounds) {
                return;
            }
            outlaw.reduceStrength(this.damage);
            // Determine if the mob dies or not on impact.
            if (this.deadOnPlayerImpact) {
                this.getParent().addScore(outlaw.getStrength());
                this.prepareDeath();
            } else {
                this.frameRange.playImpact(this);
            }
            this.playerInMobBounds = true;
            return;
        }

        this.playerInMobBounds = false;
    }

    /**
     * Prepare this mob's death sequence.
     */
    private void prepareDeath() {
        this.alive = false;
        this.dying = true;
        this.getParent().incrementMobKillCount();
        this.setFrameAutoReset(false);
        this.setFrameInterval(FRAME_DEATH_INTERVAL);
        this.frameRange.playDeath(this);
        this.deathEffect = new ExplosionEffect(this);
        Game.playSFX(SFX_DEAD_MOB, 0.3);
    }

    /**
     * Change this mob's movement direction.
     */
    public void changeDirection() {
        this.setFlip(this.movingRight, false);
        this.movingRight = !this.movingRight;
        int multiplier = this.movingRight ? 1 : -1;
        this.addX((int) this.getBounds().getWidth() / 2 * multiplier);
    }

    /**
     * Retrieves whether this mob is allowed to shoot.
     * @return a boolean indicating shooting capability.
     */
    protected boolean getShootingCapability() {
        // On higher difficulties, the mob's shooting capability is randomized.
        boolean allowMobShooting = !getParent().getRestrictedMode();
        return (allowMobShooting && Game.RNG.nextBoolean());
    }

    /**
     * Induces this mob to shoot.
     */
    protected void shoot() {
        if (!this.isAlive() || !this.shooter) {
            return;
        }

        byte activeDirections = this.movingRight
                ? Game.DIR_RIGHT
                : Game.DIR_LEFT;
        Bullet bullet = new Bullet(this, getParent(), activeDirections, true);
        this.getParent().getLevelMap().addEntity(bullet);
        this.frameRange.playShoot(this);
    }

    /**
     * Retrieves this mob's damage points.
     * @return an integer.
     */
    public int getDamage() {
        return this.damage;
    }

    /**
     * Retrieves whether this mob is alive.
     * @return a boolean.
     */
    public boolean isAlive() {
        return this.alive;
    }

    /**
     * Retrieves whether this mob is dying.
     * @return a boolean.
     */
    public boolean isDying() {
        return this.dying;
    }

    /**
     * Retrieves whether this mob is moving to the right.
     * @return a boolean.
     */
    public boolean isMovingRight() {
        return this.movingRight;
    }

    /**
     * Reduces the health points of this mob.
     * @param value a positive integer.
     */
    public void reduceHealth(int value) {
        // This must be a positive integer.
        if (value < 0) {
            return;
        }
        this.health -= value;
        this.getParent().addScore(value);

        byte moteType = Mote.TYPE_NEUTRAL;
        if (this.health <= 0) {
            this.prepareDeath();
            moteType = Mote.TYPE_BAD;
        } else {
            this.frameRange.playDamage(this);
        }

        this.getParent().spawnMote(this, value, moteType);
    }

    /**
     * Specifies whether this mob should be dead on player impact.
     * @param value a boolean.
     */
    protected void setDeadOnPlayerImpact(boolean value) {
        this.deadOnPlayerImpact = value;
    }

    /**
     * Specifies whether this mob should follow the player.
     * @param value a boolean.
     */
    protected void setChasingPlayer(boolean value) {
        this.chasingPlayer = value;
    }

    /**
     * Specifies the frame range of this sprite.
     * @param frameRange a FrameRange object.
     */
    protected void setFrameRange(FrameRange frameRange) {
        this.frameRange = frameRange;
    }

}
