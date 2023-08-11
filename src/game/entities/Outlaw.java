package game.entities;

import java.util.Arrays;

import game.Game;
import game.UIUtils;
import game.entities.effects.Effect;
import game.entities.effects.ImmortalityEffect;
import game.entities.effects.SmokeEffect;
import game.scenes.LevelScene;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * This class is a sprite representing the player's character
 * in the game and is responsible for movement and shooting.
 * @author Francis Dominic Fajardo
 */
public final class Outlaw extends Entity {

    /** Frame set: facing west. */
    private static final Image FRAMESET_W = new Image(
            Game.getAsset("player_sheet_w.png"));
    /** Frame set: facing south west. */
    private static final Image FRAMESET_SW = new Image(
            Game.getAsset("player_sheet_sw.png"));
    /** Frame set: facing north. */
    private static final Image FRAMESET_N = new Image(
            Game.getAsset("player_sheet_n.png"));
    /** Frame set: facing north west. */
    private static final Image FRAMESET_NW = new Image(
            Game.getAsset("player_sheet_nw.png"));
    /** Frame set: facing south. */
    private static final Image FRAMESET_S = new Image(
            Game.getAsset("player_sheet_s.png"));
    /** Frame set: rows. */
    private static final int FRAMESET_ROWS = 5;
    /** Frame set: columns. */
    private static final int FRAMESET_COLUMNS = 14;
    /** Frame set: offset from sides. */
    private static final int[] FRAMESET_OFFSET =
            new int[] { 15, 17, 9, 3 };
    /** Frame set: range. */
    private static final FrameRange FRAME_RANGE =
            new FrameRange(14, 21,
                    56, 58,
                    56, 58,
                    28, 33,
                    56, 69,
                    0, 5);

    /** Tuning: base movement speed. */
    private static final int BASE_SPEED = 10;

    /** Path to the outlaw death sound effect. */
    private static final String SFX_DEAD_OUTLAW = "sfx_dead_outlaw.wav";

    /** State: health/strength. */
    private int strength;
    /** State: alive. */
    private boolean alive;
    /** State: dying. */
    private boolean dying;
    /** State: immortal (power-up). */
    private boolean immortal;
    /** State: blocked from shooting (tried to shoot from non-LTR direction). */
    private boolean blockedFromShooting;
    /** Active directions. */
    private byte activeDirections;

    /** Passability of surrounding tiles. */
    private boolean[] passability;
    /** Sprite frame range. */
    private FrameRange frameRange;
    /** Effect: power-up. */
    private Effect powerupEffect;
    /** Effect: immortality. */
    private Effect immortalityEffect;

    /**
     * Constructs an instance of Outlaw.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param parent the LevelScene object owning this entity.
     */
    public Outlaw(int x, int y, LevelScene parent) {
        super(x, y, parent);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setFrameRange(FRAME_RANGE);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.frameRange.playIdle(this);

        this.strength = Game.DEBUG_MODE
                ? 1
                : Game.RNG.nextInt(151) + 100;

        this.alive = true;
        this.dying = false;
        this.immortal = false;
        this.blockedFromShooting = false;

        this.activeDirections = 0;

        this.powerupEffect = null;
        this.immortalityEffect = null;
        this.passability = new boolean[4];
        Arrays.fill(passability, true);
    }

    @Override
    public void update(long now) {
        super.update(now);
        // Update running effects.
        if (immortalityEffect != null) {
            immortalityEffect.update(now);
        }
        if (powerupEffect != null) {
            powerupEffect.update(now);
        }
        // Check if the dying frame sequence is done and update level state.
        if (this.dying) {
            if (this.isFrameSequenceDone()) {
                this.dying = false;
                this.getParent().markLevelDone();
                return;
            }
        }
        // Don't update movement or passability if this entity is dead.
        if (!this.isAlive()) {
            return;
        }
        // Update passability state.
        passability = this.getParent().getLevelMap().getPassability(this);
        // Movement: horizontal directions.
        if (this.getBounds().getMinX() + dx >= 0
                && Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT)
                && passability[SIDE_LEFT]) {
            this.dx = -BASE_SPEED;
        } else if (this.getBounds().getMinX() + this.dx <= Game.WINDOW_MAX_WIDTH - this.getBounds().getWidth()
                && Game.isDirectionActive(this.activeDirections, Game.DIR_RIGHT)
                && passability[SIDE_RIGHT]) {
            this.dx = BASE_SPEED;
        } else {
            this.dx = 0;
        }
        // Movement: vertical directions.
        if (this.getBounds().getMinY() + dy >= 0
                && Game.isDirectionActive(this.activeDirections, Game.DIR_UP)
                && passability[SIDE_TOP]) {
            this.dy = -BASE_SPEED;
        } else if (this.getBounds().getMinY() + dy <= Game.WINDOW_MAX_HEIGHT - this.getBounds().getHeight()
                && Game.isDirectionActive(this.activeDirections, Game.DIR_DOWN)
                && passability[SIDE_BOTTOM]) {
            this.dy = BASE_SPEED;
        } else {
            this.dy = 0;
        }
        // Determine whether to play the walking or idle frame range.
        if (this.dx != 0 || this.dy != 0) {
            this.frameRange.playWalk(this);
        } else {
            this.frameRange.playIdle(this);
        }
        // Update entity positions.
        this.addX(this.dx);
        this.addY(this.dy);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw the immortality effect, if applicable.
        if (immortalityEffect != null) {
            immortalityEffect.draw(gc);
        }
        // Draw the sprite.
        super.draw(gc);
        // Draw the power-up effect, if applicable.
        if (powerupEffect != null) {
            powerupEffect.draw(gc);
        }
        // Debug only: Draw passability overlay.
        if (Game.DEBUG_MODE && !hideWireframe) {
            UIUtils.drawPassability(gc, this, passability);
        }
    }

    /**
     * Induces this character to shoot with a gun.
     */
    public void shoot() {
        // Can't shoot if this entity is dead.
        if (!this.isAlive()) {
            return;
        }
        // Play a sound effect and create the bullet.
        Game.playSFX(Bullet.SFX_SHOOT, 0.3);
        Bullet bullet = new Bullet(this, getParent(), activeDirections,
                Game.FLAG_DIRECTIONAL_SHOOTING);
        this.getParent().getLevelMap().addEntity(bullet);
    }

    /**
     * Increases this character's strength.
     * @param value a positive integer.
     */
    public void increaseStrength(int value) {
        // This must be a positive integer.
        if (value < 0) {
            return;
        }
        this.strength += value;
        // Spawn a mote.
        this.getParent().spawnMote(this, value, Mote.TYPE_GOOD);
    }

    /**
     * Reduces this character's strength.
     * @param value a positive integer.
     */
    public void reduceStrength(int value) {
        // This must be a positive integer.
        if (value < 0 || this.immortal || !this.alive) {
            return;
        }
        // Clamp the strength value to 0.
        if (this.strength - value < 0) {
            this.strength = 0;
        } else {
            this.strength -= value;
        }
        // Spawn a mote.
        byte moteType = Mote.TYPE_NEUTRAL;
        if (this.strength == 0) {
            moteType = Mote.TYPE_BAD;
            this.prepareDeath();
        } else {
            this.frameRange.playDamage(this);
        }
        this.getParent().spawnMote(this, value, moteType);
    }

    /**
     * Prepares this entity for the death sequence.
     */
    private void prepareDeath() {
        this.alive = false;
        this.dying = true;
        this.setFrameAutoReset(false);
        this.frameRange.playDeath(this);
        Game.playSFX(SFX_DEAD_OUTLAW);
    }

    /**
     * Handles the key pressed/released events for movement.
     * @param level the LevelScene object owning this entity.
     */
    public void handleKeyPressEvent(LevelScene level) {
        Scene scene = level.getInner();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (level.isLevelDone() || level.isLevelPaused()) {
                    return;
                }
                KeyCode code = e.getCode();
                startMoving(code);
            }
        });
        scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                stopMoving(code);
            }
        });
    }

    /**
     * Starts movement or shooting.
     * @param keyCode a KeyCode indicating the current action.
     */
    private void startMoving(KeyCode keyCode) {
        if (!this.isAlive()) {
            return;
        }

        switch (keyCode) {
        case UP:
        case W:
            this.activeDirections |= Game.DIR_UP;
            break;
        case DOWN:
        case S:
            this.activeDirections |= Game.DIR_DOWN;
            break;
        case LEFT:
        case A:
            this.activeDirections |= Game.DIR_LEFT;
            if (!Game.FLAG_DIRECTIONAL_SHOOTING) {
                this.blockedFromShooting = true;
            }
            break;
        case RIGHT:
        case D:
            this.activeDirections |= Game.DIR_RIGHT;
            break;
        case SPACE:
        case ENTER:
            if (this.blockedFromShooting) {
                break;
            }
            // We can't shoot in other directions, following a limitation
            // imposed by the problem domain.
            this.blockedFromShooting = true;
            this.frameRange.playShoot(this,
                    Game.FLAG_DIRECTIONAL_SHOOTING ? null : FRAMESET_W);
            this.shoot();
            break;
        default:
            break;
        }
        this.updateFrameSet();
    }

    /**
     * Stops movement or shooting.
     * @param keyCode a KeyCode indicating the current action.
     */
    private void stopMoving(KeyCode keyCode) {
        switch (keyCode) {
        case UP:
        case W:
            this.activeDirections &= ~Game.DIR_UP;
            break;
        case DOWN:
        case S:
            this.activeDirections &= ~Game.DIR_DOWN;
            break;
        case LEFT:
        case A:
            this.activeDirections &= ~Game.DIR_LEFT;
            this.blockedFromShooting = false;
            break;
        case RIGHT:
        case D:
            this.activeDirections &= ~Game.DIR_RIGHT;
            break;
        case SPACE:
        case ENTER:
            if (Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT) && !Game.FLAG_DIRECTIONAL_SHOOTING) {
                break;
            }
            this.blockedFromShooting = false;
            break;
        default:
            break;
        }

        this.updateFrameSet();
    }

    /**
     * Updates this entity's frame set.
     */
    private void updateFrameSet() {
        this.setFlip(false, false);
        if (Game.isDirectionActive(this.activeDirections, Game.DIR_UP)) {
            if (Game.isDirectionActive(this.activeDirections, Game.DIR_RIGHT)) {
                this.setFrameSet(FRAMESET_NW);
            } else if (Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT)) {
                this.setFrameSet(FRAMESET_NW);
                this.setFlip(true, false);
            } else {
                this.setFrameSet(FRAMESET_N);
            }
        } else if (Game.isDirectionActive(this.activeDirections, Game.DIR_DOWN)) {
            if (Game.isDirectionActive(this.activeDirections, Game.DIR_RIGHT)) {
                this.setFrameSet(FRAMESET_SW);
            } else if (Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT)) {
                this.setFrameSet(FRAMESET_SW);
                this.setFlip(true, false);
            } else {
                this.setFrameSet(FRAMESET_S);
            }
        } else {
            if (Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT)) {
                this.setFlip(true, false);
            }
            this.setFrameSet(FRAMESET_W);
        }
    }

    /**
     * Spawns the power-up effect.
     */
    public void spawnPowerupEffect() {
        this.powerupEffect = new SmokeEffect(this);
    }

    /**
     * Retrieves the value of this character's strength.
     * @return an integer.
     */
    public int getStrength() {
        return this.strength;
    }

    /**
     * Retrieves whether this character is alive.
     * @return a boolean.
     */
    public boolean isAlive() {
        return this.alive;
    }

    /**
     * Retrieves whether this character is dying.
     * @return a boolean.
     */
    public boolean isDying() {
        return this.dying;
    }

    /**
     * Retrieves whether this character is immortal.
     * @return a boolean.
     */
    public boolean isImmortal() {
        return this.immortal;
    }

    /**
     * Specifies whether this character is immortal.
     * @param immortal a boolean.
     */
    public void setImmortal(boolean immortal) {
        if (immortal) {
            this.immortalityEffect = new ImmortalityEffect(this);
            this.immortal = true;
        } else {
            this.immortalityEffect = null;
            this.immortal = false;
        }
    }

    /**
     * Specifies the frame range of this sprite.
     * @param frameRange a FrameRange object.
     */
    protected void setFrameRange(FrameRange frameRange) {
        this.frameRange = frameRange;
    }

}
