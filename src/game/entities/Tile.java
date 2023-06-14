package game.entities;

import game.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public final class Tile {

    public static final int SIZE = 16;
    public static final int SIZE_MID = SIZE * 2;

    public static final int ALL_VERTICAL =
            (Game.WINDOW_MAX_HEIGHT + 8) / Tile.SIZE_MID;
    public static final int ALL_HORIZONTAL =
            (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID;
    public static final int ALL =
            ALL_VERTICAL * ALL_HORIZONTAL;

    private Sprite tileset;

    public Tile(String tilesetPath, int rows, int columns, int scale) {
        Image tilesetImage = new Image(Game.getAsset(tilesetPath));
        tileset = new Sprite() {};
        tileset.setFrameSet(tilesetImage, rows, columns);
        tileset.setFrameAutoReset(false);
        tileset.setScale(scale);
        tileset.hideWireframe = true;
    }

    public Tile(String assetPath, int rows, int columns) {
        this(assetPath, rows, columns, Sprite.BASE_SCALE);
    }

    public void draw(GraphicsContext gc, int x, int y, int frameId,
            boolean flip, boolean flipVertical) {
        tileset.setFlip(flip, flipVertical);
        tileset.setFrame(frameId);
        tileset.setX(x);
        tileset.setY(y);
        tileset.draw(gc);
    }

    public void draw(GraphicsContext gc, int x, int y, int frameId) {
        draw(gc, x, y, frameId, false, false);
    }

}
