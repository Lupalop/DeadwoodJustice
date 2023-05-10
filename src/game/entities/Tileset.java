package game.entities;

import game.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Tileset extends Sprite {

    public Tileset(String assetPath) {
        super(0, 0);
        
        Image tilesetImage = new Image(Game.getAsset(assetPath));
        this.setFrameSet(tilesetImage, 3, 4);
        this.setFrameAutoReset(false);
        this.setScale(1);
    }

    public void draw(GraphicsContext gc, int x, int y, int frameId) {
        this.setFrame(frameId);
        this.setX(x);
        this.setY(y);
        super.draw(gc);
    }
}