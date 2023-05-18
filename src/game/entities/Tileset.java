package game.entities;

import game.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Tileset extends Sprite {

    public static final int TILE_SIZE = 32;

    public Tileset(String assetPath, int rows, int columns) {
        super(0, 0);
        
        Image tilesetImage = new Image(Game.getAsset(assetPath));
        this.setFrameSet(tilesetImage, rows, columns);
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
