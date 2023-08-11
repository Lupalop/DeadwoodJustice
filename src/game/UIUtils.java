package game;

import game.entities.Button;
import game.entities.Entity;
import game.entities.Sprite;
import game.entities.Tile;
import game.scenes.GameScene;
import game.scenes.MainMenuScene;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * This utility class contains static features that are useful for
 * managing the game's user interface, from standard colors to fonts.
 * @author Francis Dominic Fajardo
 */
public final class UIUtils {

    /** The primary color. */
    public static final Paint COLOR_PRIMARY = Paint.valueOf("eeca84");
    /** The secondary color. */
    public static final Paint COLOR_SECONDARY = Paint.valueOf("49276d");
    /** The tertiary color. */
    public static final Paint COLOR_TERTIARY = Paint.valueOf("a23e47");

    /** The UI tilemap. */
    public static final Tile TILE =
            new Tile("tilemap_ui.png", 4, 9);

    /** Font size: 32. */
    public static final int FONT_SIZE_32 = 32;
    /** Font size: 48. */
    public static final int FONT_SIZE_48 = 48;
    /** Font size: 21 (button). */
    public static final int FONT_SIZE_BTN = 21;

    /** Font path: standard. */
    private static final String FONT_PATH =
            Game.getAsset("THALEAHFAT.ttf");
    /** Font path: alternate. */
    private static final String FONT_MUP_PATH =
            Game.getAsset("MATCHUPPRO.ttf");

    /** Font: standard 48. */
    public static final Font FONT_48 =
            Font.loadFont(FONT_PATH, FONT_SIZE_48);
    /** Font: standard 32. */
    public static final Font FONT_32 =
            Font.loadFont(FONT_PATH, FONT_SIZE_32);

    /** Font: alternate 48. */
    public static final Font FONT_ALT_48 =
            Font.loadFont(FONT_MUP_PATH, FONT_SIZE_48);
    /** Font: alternate 32. */
    public static final Font FONT_ALT_32 =
            Font.loadFont(FONT_MUP_PATH, FONT_SIZE_32);

    /** Texture: menu overlay (start). */
    private static final int TX_POP_START = 6;
    /** Texture: menu overlay (middle). */
    private static final int TX_POP_MID = 7;
    /** Texture: menu overlay (end). */
    private static final int TX_POP_END = 8;

    /**
     * Draws a shade behind an overlay.
     * @param gc a GraphicsContext object.
     */
    public static void drawShade(GraphicsContext gc) {
        gc.save();
        gc.setGlobalAlpha(0.5);
        gc.setFill(COLOR_SECONDARY);
        gc.fillRect(0, 0, Game.WINDOW_MAX_WIDTH, Game.WINDOW_MAX_HEIGHT);
        gc.restore();
    }

    /**
     * Draws a menu background (usually for an overlay).
     * @param gc a GraphicsContext object.
     * @param base the tile from which the top should be drawn (vertical).
     * @param innerHeight the height of the middle part of the menu.
     */
    public static void drawMenuBackground(GraphicsContext gc, int base, int innerHeight) {
        // Draw top tiles.
        for (int i = 0; i < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; i++) {
            TILE.draw(gc, Tile.SIZE_MID * i,
                    Tile.SIZE_MID * base, TX_POP_START);
        }
        // Draw middle tiles.
        for (int i = 0; i < innerHeight; i++) {
            base++;
            for (int j = 0; j < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; j++) {
                TILE.draw(gc, Tile.SIZE_MID * j,
                        Tile.SIZE_MID * base, TX_POP_MID);
            }
        }
        // Draw bottom tiles.
        base++;
        for (int i = 0; i < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; i++) {
            TILE.draw(gc, Tile.SIZE_MID * i,
                    Tile.SIZE_MID * base, TX_POP_END);
        }
    }

    /**
     * Debug only: draws passability indicators.
     * @param gc a GraphicsContext object.
     * @param sprite a Sprite object.
     * @param passability a boolean array indicating passability.
     */
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

    /**
     * Handles the escape/backspace key and makes them set the scene
     * to the main menu.
     * @param scene a GameScene object.
     */
    public static void handleReturnToMainMenu(GameScene scene) {
        scene.getInner().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                switch (code) {
                case BACK_SPACE:
                case ESCAPE:
                    Game.setGameScene(new MainMenuScene());
                    Game.playSFX(Button.SFX_BUTTON);
                    break;
                default:
                    break;
                }
            }
        });
    }

}
