package game.entities;

import game.Game;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

public class Prop extends Sprite {

    private Rectangle2D customCollider;

    public Prop(int xPos, int yPos, String assetPath) {
        super(xPos, yPos);

        this.setImage(new Image(Game.getAsset(assetPath)));
        this.customCollider = null;
    }

    @Override
    public Rectangle2D getCollider() {
        if (this.customCollider == null) {
            return super.getCollider();
        }
        return new Rectangle2D(
                super.getBounds().getMinX(),
                super.getBounds().getMinY(),
                this.customCollider.getWidth(),
                this.customCollider.getHeight());
    }

    public void setCustomCollider(Rectangle2D customCollider) {
        this.customCollider = customCollider;
    }

}
