package game.entities.props;

import game.Game;
import game.entities.Entity;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * The Prop represents props used in the level map.
 * @author Francis Dominic Fajardo
 */
public abstract class Prop extends Entity {

    private Rectangle2D customCollider;

    public Prop(int xPos, int yPos, String assetPath) {
        super(xPos, yPos, null);

        this.setImage(new Image(Game.getAsset(assetPath)));
        this.customCollider = null;
    }

    @Override
    public Rectangle2D getCollider() {
        if (this.customCollider == null) {
            return super.getCollider();
        }
        return new Rectangle2D(
                super.getBounds().getMinX()
                + (this.customCollider.getMinX() * this.getScale()),
                super.getBounds().getMinY()
                + (this.customCollider.getMinY() * this.getScale()),
                this.customCollider.getWidth() * this.getScale(),
                this.customCollider.getHeight() * this.getScale());
    }

    protected void setCustomCollider(Rectangle2D customCollider) {
        this.customCollider = customCollider;
    }

}
