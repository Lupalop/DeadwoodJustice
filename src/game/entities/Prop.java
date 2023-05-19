package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class Prop extends Sprite {

    public Prop(int xPos, int yPos, String assetPath) {
        super(xPos, yPos);

        this.setImage(new Image(Game.getAsset(assetPath)));
        this.setScale(2);
    }

}
