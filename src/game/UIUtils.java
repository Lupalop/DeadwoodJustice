package game;

import game.entities.Entity;
import game.entities.Sprite;
import game.entities.Tile;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public final class UIUtils {

    public static final Paint COLOR_PRIMARY = Paint.valueOf("eeca84");
    public static final Paint COLOR_SECONDARY = Paint.valueOf("49276d");
    public static final Paint COLOR_TERTIARY = Paint.valueOf("a23e47");

    public static final Tile TILE =
            new Tile("tilemap_ui.png", 4, 9);

    public static final int FONT_SIZE_32 = 32;
    public static final int FONT_SIZE_48 = 48;
    public static final int FONT_SIZE_BTN = 21;

    public static final String FONT_PATH =
            Game.getAsset("THALEAHFAT.ttf");
    public static final String FONT_MUP_PATH =
            Game.getAsset("MATCHUPPRO.ttf");

    public static final Font FONT_48 =
            Font.loadFont(FONT_PATH, FONT_SIZE_48);
    public static final Font FONT_32 =
            Font.loadFont(FONT_PATH, FONT_SIZE_32);

    public static final Font FONT_ALT_48 =
            Font.loadFont(FONT_MUP_PATH, FONT_SIZE_48);
    public static final Font FONT_ALT_32 =
            Font.loadFont(FONT_MUP_PATH, FONT_SIZE_32);

    private static final int TX_POP_START = 6;
    private static final int TX_POP_MID = 7;
    private static final int TX_POP_END = 8;

    public static void drawShade(GraphicsContext gc) {
        gc.save();
        gc.setGlobalAlpha(0.5);
        gc.setFill(COLOR_SECONDARY);
        gc.fillRect(0, 0, Game.WINDOW_MAX_WIDTH, Game.WINDOW_MAX_HEIGHT);
        gc.restore();
    }

    public static void drawMenuBackground(GraphicsContext gc, int base, int innerHeight) {
        for (int i = 0; i < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; i++) {
            TILE.draw(gc, Tile.SIZE_MID * i,
                    Tile.SIZE_MID * base, TX_POP_START);
        }

        for (int i = 0; i < innerHeight; i++) {
            base++;
            for (int j = 0; j < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; j++) {
                TILE.draw(gc, Tile.SIZE_MID * j,
                        Tile.SIZE_MID * base, TX_POP_MID);
            }
        }

        base++;
        for (int i = 0; i < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; i++) {
            TILE.draw(gc, Tile.SIZE_MID * i,
                    Tile.SIZE_MID * base, TX_POP_END);
        }
    }

    public static void drawPassability(GraphicsContext gc, Sprite sprite, boolean[] passability) {
        gc.save();
        // Top
        gc.setFill(passability[Entity.SIDE_TOP] ? Color.GREEN : Color.RED);
        gc.fillRoundRect(
                sprite.getBounds().getMinX(),
                sprite.getBounds().getMinY() - 10,
                10, 10, 100, 100);
        // Left
        gc.setFill(passability[Entity.SIDE_LEFT] ? Color.GREEN : Color.RED);
        gc.fillRoundRect(
                sprite.getBounds().getMinX() - 10,
                sprite.getBounds().getMinY(),
                10, 10, 100, 100);
        // Bottom
        gc.setFill(passability[Entity.SIDE_BOTTOM] ? Color.GREEN : Color.RED);
        gc.fillRoundRect(
                sprite.getBounds().getMaxX(),
                sprite.getBounds().getMaxY() + 10,
                10, 10, 100, 100);
        // Right
        gc.setFill(passability[Entity.SIDE_RIGHT] ? Color.GREEN : Color.RED);
        gc.fillRoundRect(
                sprite.getBounds().getMaxX() + 10,
                sprite.getBounds().getMaxY(),
                10, 10, 100, 100);
        gc.restore();
    }

}
