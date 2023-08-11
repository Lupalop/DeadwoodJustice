package game.entities.props;

import game.Game;
import game.entities.Entity;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * This class represents props used in the level map.
 * @author Francis Dominic Fajardo
 */
public abstract class Prop extends Entity {

    /** Custom collider rectangle. */
    private Rectangle2D customCollider;

    /**
     * Constructs an instance of Prop.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param assetPath a String containing the asset path.
     */
    public Prop(int xPos, int yPos, String assetPath) {
        super(xPos, yPos, null);

        this.setImage(new Image(Game.getAsset(assetPath)));
        this.customCollider = null;
    }

    @Override
    public Rectangle2D getCollider() {
        // Revert to the dynamically generated collider if we
        // don't have a custom collider.
        if (this.customCollider == null) {
            return super.getCollider();
        }
        // Rescale and reposition the custom collider.
        return new Rectangle2D(
                super.getBounds().getMinX()
                    + (this.customCollider.getMinX() * this.getScale()),
                super.getBounds().getMinY()
                    + (this.customCollider.getMinY() * this.getScale()),
                this.customCollider.getWidth() * this.getScale(),
                this.customCollider.getHeight() * this.getScale());
    }

    /**
     * Specifies the custom collider.
     * @param customCollider a Rectangle.
     */
    protected void setCustomCollider(Rectangle2D customCollider) {
        this.customCollider = customCollider;
    }

}
