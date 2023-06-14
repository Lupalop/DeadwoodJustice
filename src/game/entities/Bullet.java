package game.entities;

import game.Game;
import game.entities.mobs.Mob;
import game.entities.props.Prop;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

/**
 * The Bullet class is an entity representing bullets shot by
 * both the player and mobs in the game.
 * @author Francis Dominic Fajardo
 */
public final class Bullet extends Entity {

    /** Path to the shoot sound effect. */
    public static final String SFX_SHOOT = "sfx_shoot.wav";
    /** Path to the hit by bullet sound effect. */
    public static final String SFX_HIT = "sfx_hit.wav";

    /** Image representing a bullet from the player. */
    private static final Image BULLET_IMAGE = new Image(
            Game.getAsset("bullet.png"),
            Bullet.BULLET_WIDTH, Bullet.BULLET_WIDTH, false, false);
    /** Image representing a bullet from a mob. */
    private static final Image BULLET_ALT_IMAGE = new Image(
            Game.getAsset("bullet_alt.png"),
            Bullet.BULLET_WIDTH, Bullet.BULLET_WIDTH, false, false);

    /** Speed of the bullet. */
    private static final int BULLET_SPEED = 20;
    /** Width of the bullet texture. */
    private static final int BULLET_WIDTH = 20;
    /** Scale of the bullet sprite. */
    private static final int BULLET_SCALE = 1;

    /** Offset: bullet towards north. */
    private static final int OFFSET_N = -10;
    /** Offset: bullet towards north east. */
    private static final int OFFSET_NE = -10;
    /** Offset: bullet towards north west. */
    private static final int OFFSET_NW = -35;
    /** Offset: bullet towards south. */
    private static final int OFFSET_S = -35;
    /** Offset: bullet towards south east. */
    private static final int OFFSET_SE = -30;
    /** Offset: bullet towards south west. */
    private static final int OFFSET_SW = -20;
    /** Offset: bullet towards west. */
    private static final int OFFSET_W = -35;
    /** Offset: y-only bullet from outlaw. */
    private static final int OUTLAW_OFFSET_Y = -10;

    /** Whether the bullet was shot from a non-LTR direction. */
    private boolean isDirectional;
    /** Whether the bullet was shot by the outlaw or player. */
    private boolean fromOutlaw;
    /** The mob who shot the bullet, empty if from a different source. */
    private Mob mobSource;

    /**
     * Creates a new instance of the Bullet class (Mob source).
     * @param source the Mob source of this bullet.
     * @param parent the LevelScene object owning this entity.
     * @param activeDirections the active directions when this bullet was shot.
     * @param isDirectional whether this bullet was shot from a non-LTR direction.
     */
    public Bullet(Mob source, LevelScene parent,
            byte activeDirections, boolean isDirectional) {
        super(0, 0, parent);
        this.initialize(source, activeDirections, isDirectional, false);
        this.mobSource = source;
    }

    /**
     * Creates a new instance of the Bullet class (Outlaw source).
     * @param source the Outlaw source of this bullet.
     * @param parent the LevelScene object owning this entity.
     * @param activeDirections the active directions when this bullet was shot.
     * @param isDirectional whether this bullet was shot from a non-LTR direction.
     */
    public Bullet(Outlaw source, LevelScene parent,
            byte activeDirections, boolean isDirectional) {
        super(0, 0, parent);
        this.initialize(source, activeDirections, isDirectional, true);
    }

    /**
     * Initializes this object.
     * @param source the Outlaw source of this bullet.
     * @param activeDirections the active directions when this bullet was shot.
     * @param isDirectional whether this bullet was shot from a non-LTR direction.
     * @param fromOutlaw whether the bullet originated from the outlaw.
     */
    private void initialize(Sprite source, byte activeDirections,
            boolean isDirectional, boolean fromOutlaw) {
        this.setX((int) (source.getBounds().getMaxX()));
        this.setY((int) (source.getBounds().getMinY()
            + (source.getBounds().getHeight() / 2)
            - (this.getBounds().getHeight() / 2)));

        this.setScale(Bullet.BULLET_SCALE);

        this.isDirectional = isDirectional;
        this.mobSource = null;
        this.fromOutlaw = fromOutlaw;

        if (this.fromOutlaw) {
            this.setImage(Bullet.BULLET_IMAGE);
            this.addY(OUTLAW_OFFSET_Y);
        } else {
            this.setImage(BULLET_ALT_IMAGE);
        }

        this.computeDestination(activeDirections);
    }

    @Override
    public void update(long now) {
        super.update(now);

        this.addX(this.dx);
        this.addY(this.dy);

        if (this.getX() > Game.WINDOW_MAX_WIDTH) {
            this.setVisible(false);
            this.remove();
        }

        this.checkCollisions();
    }

    /**
     * Compute for the bullet's destination.
     * @param activeDirections the active directions when this bullet was shot.
     */
    private void computeDestination(byte activeDirections) {
        // Check if we're allowed to have non-LTR directions.
        if (isDirectional) {
            boolean noHorizontalDirections =
                    !Game.isDirectionActive(activeDirections, Game.DIR_RIGHT)
                    && !Game.isDirectionActive(activeDirections, Game.DIR_LEFT);
            // North.
            if (Game.isDirectionActive(activeDirections, Game.DIR_UP)) {
                this.dy = -Bullet.BULLET_SPEED;
                if (noHorizontalDirections) {
                    this.addX(OFFSET_N);
                }
            // South.
            } else if (Game.isDirectionActive(activeDirections, Game.DIR_DOWN)) {
                this.dy = Bullet.BULLET_SPEED;
                if (noHorizontalDirections) {
                    this.addX(OFFSET_S);
                }
            // Fallback: East.
            } else {
                this.dx = Bullet.BULLET_SPEED;
            }

            // East.
            if (Game.isDirectionActive(activeDirections, Game.DIR_RIGHT)) {
                this.dx = Bullet.BULLET_SPEED;
                // North east.
                if (Game.isDirectionActive(activeDirections, Game.DIR_UP)) {
                    this.addX(OFFSET_NE);
                // South east.
                } else if (Game.isDirectionActive(activeDirections, Game.DIR_DOWN)) {
                    this.addX(OFFSET_SE);
                }
            // West.
            } else if (Game.isDirectionActive(activeDirections, Game.DIR_LEFT)) {
                this.dx = -Bullet.BULLET_SPEED;
                // North west.
                if (Game.isDirectionActive(activeDirections, Game.DIR_UP)) {
                    this.addX(OFFSET_NW);
                // South west.
                } else if (Game.isDirectionActive(activeDirections, Game.DIR_DOWN)) {
                    this.addX(OFFSET_SW);
                // West.
                } else {
                    this.addX(OFFSET_W);
                }
            }
        } else {
            // East.
            this.dx = Bullet.BULLET_SPEED;
        }
    }

    /**
     * Checks if this bullet has collided with another entity.
     */
    private void checkCollisions() {
        // Don't bother checking collisions if we're invisible.
        if (!this.getVisible()) {
            return;
        }

        // Props can consume bullets if we're in a higher difficulty.
        boolean propsConsumeBullets = !getParent().getRestrictedMode();
        boolean bulletCaught = false;
        Outlaw outlaw = this.getParent().getOutlaw();

        // Iterate through all entities.
        for (Entity entity : this.getParent().getLevelMap().getEntities()) {
            // This bullet from the outlaw hit a mob.
            if (entity instanceof Mob && this.fromOutlaw) {
                Mob mob = (Mob) entity;
                if (mob.isAlive() && this.intersects(entity)) {
                    mob.reduceHealth(outlaw.getStrength());
                    bulletCaught = true;
                    break;
                }
            }
            // This bullet was consumed by a prop.
            if (entity instanceof Prop && propsConsumeBullets) {
                if (this.intersects(entity, false, false, true)) {
                    bulletCaught = true;
                    break;
                }
            }
        }

        // Play a hit sound effect and reduce the player health.
        if (!bulletCaught && !this.fromOutlaw && outlaw.intersects(this)) {
            Game.playSFX(SFX_HIT);
            outlaw.reduceStrength(this.mobSource.getDamage());
            bulletCaught = true;
        }

        // Hide and remove this bullet.
        if (bulletCaught) {
            this.setVisible(false);
            this.remove();
        }
    }

}