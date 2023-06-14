package game.entities;

import game.Game;
import game.scenes.LevelScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * The abstract Entity class is a sprite that is linked to a level scene
 * and provides methods for handling collisions.
 * @author Francis Dominic Fajardo
 */
public abstract class Entity extends Sprite {

    /** Passability side: left. */
    public static final int SIDE_LEFT = 0;
    /** Passability side: right. */
    public static final int SIDE_RIGHT = 1;
    /** Passability side: top. */
    public static final int SIDE_TOP = 2;
    /** Passability side: bottom. */
    public static final int SIDE_BOTTOM = 3;
    /** Passability side: invalid. */
    public static final int SIDE_INVALID = -1;
    /** Fixed constant used in determining the base collider height. */
    private static final int BASE_DIVIDER = 4;

    /** The parent level scene. */
    private LevelScene parent;
    /** The collider rectangle. */
    private Rectangle2D collider;
    /** Whether to hide the wireframe in debug mode. */
    protected boolean hideWireframe;

    /**
     * Creates an instance of the Entity class.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param parent the LevelScene object owning this entity.
     */
    public Entity(int x, int y, LevelScene parent) {
        super(x, y);
        this.parent = parent;
        this.collider = null;
        this.hideWireframe = false;
    }

    /**
     * Creates an instance of the Entity class at position (0, 0).
     * @param parent the LevelScene object owning this entity.
     */
    public Entity(LevelScene parent) {
        this(0, 0, parent);
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        // Debug only: Draw a wireframe for this entity.
        if (Game.DEBUG_MODE && !hideWireframe) {
            gc.save();
            gc.setStroke(javafx.scene.paint.Color.RED);
            gc.strokeRect(
                    this.getBounds().getMinX(), this.getBounds().getMinY(),
                    this.getBounds().getWidth(), this.getBounds().getHeight());
            gc.setStroke(javafx.scene.paint.Color.GREEN);
            gc.strokeRect(
                    this.getCollider().getMinX(), this.getCollider().getMinY(),
                    this.getCollider().getWidth(), this.getCollider().getHeight());
            gc.restore();
        }
    }

    /**
     * Checks for intersection between two rectangles.
     * @param r1 the first rectangle.
     * @param r2 the second rectangle.
     * @param xIgnore whether to ignore the x-coordinate difference.
     * @param yIgnore whether to ignore the y-coordinate difference.
     * @return a boolean value.
     */
    private boolean intersects(Rectangle2D r1, Rectangle2D r2,
            boolean xIgnore, boolean yIgnore) {
        if (xIgnore || yIgnore) {
            r2 = new Rectangle2D(
                    xIgnore ? r1.getMinX() : r2.getMinX(),
                    yIgnore ? r1.getMinY() : r2.getMinY(),
                    r2.getWidth(),
                    r2.getHeight());
        }

        return r1.intersects(r2);
    }

    /**
     * Checks for intersection with another entity.
     * @param target the entity to be examined for intersection.
     * @param xIgnore whether to ignore the x-coordinate difference.
     * @param yIgnore whether to ignore the y-coordinate difference.
     * @param forCollider whether to check against the collider rectangle.
     * @return a boolean value.
     */
    public boolean intersects(Entity target,
            boolean xIgnore, boolean yIgnore, boolean forCollider) {
        return intersects(
                forCollider ? this.getCollider() : this.getBounds(),
                forCollider ? target.getCollider() : target.getBounds(),
                xIgnore,
                yIgnore);
    }

    /**
     * Checks for intersection with another entity.
     * @param target the entity to be examined for intersection.
     * @return a boolean value.
     */
    public boolean intersects(Entity target) {
        return intersects(target, false, false, false);
    }

    /**
     * Checks the sides where two rectangles intersect.
     * @param r1 the first rectangle.
     * @param r2 the second rectangle.
     * @return a boolean array containing intersection sides.
     */
    private boolean[] intersectsSide(Rectangle2D r1, Rectangle2D r2) {
        // Check if the rectangles intersect.
        if (!r1.intersects(r2)) {
            return null;
        }

        // Check which sides of the rectangles intersect.
        boolean[] sides = new boolean[4];

        // Left side of r1 intersects right side of r2.
        if (r1.getMinX() < r2.getMaxX() && r1.getMinX() > r2.getMinX()) {
            sides[SIDE_LEFT] = true;
        }
        // Right side of r1 intersects left side of r2.
        if (r2.getMinX() < r1.getMaxX() && r2.getMinX() > r1.getMinX()) {
            sides[SIDE_RIGHT] = true;
        }
        // Top side of r1 intersects bottom side of r2.
        if (r1.getMinY() < r2.getMaxY() && r1.getMinY() > r2.getMinY()) {
            sides[SIDE_TOP] = true;
        }
        // Bottom side of r1 intersects top side of r2.
        if (r2.getMinY() < r1.getMaxY() && r2.getMinY() > r1.getMinY()) {
            sides[SIDE_BOTTOM] = true;
        }

        return sides;
    }

    /**
     * Checks for intersection (sides) with another entity.
     * @param target the entity to be examined for intersection.
     * @param forCollider whether to check against the collider rectangle.
     * @return a boolean array containing intersection sides.
     */
    public boolean[] intersectsSide(Entity target, boolean forCollider) {
        return intersectsSide(
                forCollider ? this.getCollider() : this.getBounds(),
                forCollider ? target.getCollider() : target.getBounds());
    }

    @Override
    protected Rectangle2D resizeBounds() {
        Rectangle2D newBounds = super.resizeBounds();

        // Update the collider rectangle.
        double baseHeight = (newBounds.getHeight() / BASE_DIVIDER);
        this.collider = new Rectangle2D(
                newBounds.getMinX(),
                newBounds.getMaxY() - baseHeight,
                newBounds.getWidth(),
                baseHeight);

        return newBounds;
    }

    /**
     * Gets the level scene owning this entity.
     * @return a LevelScene object.
     */
    protected LevelScene getParent() {
        return this.parent;
    }

    /**
     * Gets the value of the collider property.
     */
    public Rectangle2D getCollider() {
        if (this.boundsDirty) {
            this.getBounds();
        }
        return this.collider;
    }

}
