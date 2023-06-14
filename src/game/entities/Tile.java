package game.entities;

import game.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * The Tile class is used for drawing tiles with the provided
 * tileset on the game canvas.
 * @author Francis Dominic Fajardo
 */
public final class Tile {

    /** Base size of a tile. */
    public static final int SIZE = 16;
    /** Adjusted size of a tile. */
    public static final int SIZE_MID = SIZE * 2;

    /** Number of tiles on the screen (vertical). */
    public static final int ALL_VERTICAL =
            (Game.WINDOW_MAX_HEIGHT + 8) / Tile.SIZE_MID;
    /** Number of tiles on the screen (horizontal). */
    public static final int ALL_HORIZONTAL =
            (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID;
    /** Number of tiles on the screen. */
    public static final int ALL =
            ALL_VERTICAL * ALL_HORIZONTAL;

    /** A sprite object representing the tileset. */
    private Sprite tileset;

    /**
     * Creates a new instance of the Tile class.
     * @param tilesetPath a String path to the tileset asset.
     * @param rows number of rows.
     * @param columns number of columns.
     * @param scale tile scale.
     */
    public Tile(String tilesetPath, int rows, int columns, int scale) {
        Image tilesetImage = new Image(Game.getAsset(tilesetPath));
        tileset = new Sprite() {};
        tileset.setFrameSet(tilesetImage, rows, columns);
        tileset.setFrameAutoReset(false);
        tileset.setScale(scale);
    }

    /**
     * Creates a new instance of the Tile class.
     * @param tilesetPath a String path to the tileset asset.
     * @param rows number of rows.
     * @param columns number of columns.
     */
    public Tile(String assetPath, int rows, int columns) {
        this(assetPath, rows, columns, Sprite.BASE_SCALE);
    }

    /**
     * Draws the tile.
     * @param gc a GraphicsContext object.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param frameId the frame number in the tileset.
     * @param flip whether to flip the tile.
     * @param flipVertical whether to flip the tile vertically.
     */
    public void draw(GraphicsContext gc, int x, int y, int frameId,
            boolean flip, boolean flipVertical) {
        tileset.setFlip(flip, flipVertical);
        tileset.setFrame(frameId);
        tileset.setX(x);
        tileset.setY(y);
        tileset.draw(gc);
    }

    /**
     * Draws the tile.
     * @param gc a GraphicsContext object.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param frameId the frame number in the tileset.
     */
    public void draw(GraphicsContext gc, int x, int y, int frameId) {
        draw(gc, x, y, frameId, false, false);
    }

}
