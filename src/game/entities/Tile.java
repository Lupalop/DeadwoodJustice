package game.entities;

import game.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Tile extends Sprite {

    public static final int SIZE = 16;
    public static final int SIZE_MID = SIZE * 2;

    public static final int ALL_VERTICAL =
            (Game.WINDOW_MAX_HEIGHT + 8) / Tile.SIZE_MID;
    public static final int ALL_HORIZONTAL =
            (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID;
    public static final int ALL =
            ALL_VERTICAL * ALL_HORIZONTAL;

    public Tile(String tilesetPath, int rows, int columns, int scale) {
        super(0, 0);

        Image tilesetImage = new Image(Game.getAsset(tilesetPath));
        this.setFrameSet(tilesetImage, rows, columns);
        this.setFrameAutoReset(false);
        this.setScale(scale);
    }

    public Tile(String assetPath, int rows, int columns) {
        this(assetPath, rows, columns, Sprite.DEFAULT_SCALE);
    }

    public void draw(GraphicsContext gc, int x, int y, int frameId) {
        this.setFrame(frameId);
        this.setX(x);
        this.setY(y);
        super.draw(gc);
    }

}
