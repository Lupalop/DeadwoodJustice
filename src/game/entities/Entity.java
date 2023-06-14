package game.entities;

import game.Game;
import game.scenes.LevelScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public abstract class Entity extends Sprite {

    public static final int SIDE_LEFT = 0;
    public static final int SIDE_RIGHT = 1;
    public static final int SIDE_TOP = 2;
    public static final int SIDE_BOTTOM = 3;
    public static final int SIDE_INVALID = -1;

    private static final int BASE_DIVIDER = 4;

    private LevelScene parent;
    private Rectangle2D collider;

    public Entity(int x, int y, LevelScene parent) {
        super(x, y);

        this.parent = parent;
        this.collider = null;
    }

    public Entity(LevelScene parent) {
        super(0, 0);
        this.parent = parent;
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);

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

    public boolean intersects(Entity target,
            boolean xIgnore, boolean yIgnore, boolean forCollider) {
        return intersects(
                forCollider ? this.getCollider() : this.getBounds(),
                forCollider ? target.getCollider() : target.getBounds(),
                xIgnore,
                yIgnore);
    }

    public boolean intersects(Entity target) {
        return intersects(target, false, false, false);
    }

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

    public boolean[] intersectsSide(Entity target, boolean forCollider) {
        return intersectsSide(
                forCollider ? this.getCollider() : this.getBounds(),
                forCollider ? target.getCollider() : target.getBounds());
    }

    @Override
    protected Rectangle2D resizeBounds() {
        Rectangle2D newBounds = super.resizeBounds();

        double baseHeight = (newBounds.getHeight() / BASE_DIVIDER);
        this.collider = new Rectangle2D(
                newBounds.getMinX(),
                newBounds.getMaxY() - baseHeight,
                newBounds.getWidth(),
                baseHeight);

        return newBounds;
    }

    protected LevelScene getParent() {
        return this.parent;
    }

    public Rectangle2D getCollider() {
        if (this.boundsDirty) {
            this.getBounds();
        }
        return this.collider;
    }

}
