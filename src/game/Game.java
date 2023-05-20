package game;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Random;

import game.scenes.GameScene;
import game.scenes.MainMenuScene;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public final class Game {

    public static final String GAME_NAME = "Deadwood Justice";
    public static final String GAME_ASSETS_PATH = "/game/assets/";

    public static final String FONT_PATH =
            Game.getAsset("THALEAHFAT.ttf");
    public static final String FONT_MUP_PATH =
            Game.getAsset("MATCHUPPRO.ttf");

    public static final int FONT_SIZE_32 = 32;
    public static final int FONT_SIZE_48 = 48;
    public static final int FONT_SIZE_BTN = 21;

    public static final Font FONT_32 =
            Font.loadFont(FONT_PATH, FONT_SIZE_32);

    public static final Font FONT_ALT_48 =
            Font.loadFont(FONT_MUP_PATH, FONT_SIZE_48);
    public static final Font FONT_ALT_32 =
            Font.loadFont(FONT_MUP_PATH, FONT_SIZE_32);

    public static final int WINDOW_MIN_WIDTH = 0;
    public static final int WINDOW_MAX_WIDTH = 800;
    public static final int WINDOW_MIN_HEIGHT = 0;
    public static final int WINDOW_MAX_HEIGHT = 600;

    public static final byte DIR_UP = 0x1;
    public static final byte DIR_DOWN = 0x2;
    public static final byte DIR_LEFT = 0x4;
    public static final byte DIR_RIGHT = 0x8;

    public static final Paint COLOR_MAIN = Paint.valueOf("eeca84");
    public static final Paint COLOR_ACCENT = Paint.valueOf("49276d");

    public static final boolean DEBUG_MODE = false;
    public static final boolean FLAG_DIRECTIONAL_SHOOTING = false;
    public static final boolean FLAG_SMARTER_MOBS = true;
    public static final boolean FLAG_DELAY_IF_BOSS_IS_ALIVE = false;
    public static final boolean FLAG_FIX_DRAW_ORDER = true;
    public static final boolean FLAG_MOBS_CHECK_PASSABILITY = true;
    public static final boolean FLAG_IGNORE_PROP_COLLISION = true;
    public static final boolean FLAG_MOBS_CAN_SHOOT = false;
    public static final boolean FLAG_SHOW_MOTES = true;

    public static final Random RNG = new Random();

    private static HashMap<String, String> cachedAssetUrls;
    private static Stage primaryStage;
    private static GameScene gameScene;
    private static GameTimer gameTimer = new GameTimer();
    private static boolean initialized;

    public static void run(Stage primaryStage) {
        if (Game.initialized) {
            return;
        }

        Game.primaryStage = primaryStage;
        Game.primaryStage.setResizable(false);
        Game.primaryStage.setTitle(GAME_NAME);
        Game.setGameScene(new MainMenuScene());
        Game.gameTimer.start();
        Game.primaryStage.show();
        Game.initialized = true;
    }

    public static Stage getPrimaryStage() {
        return Game.primaryStage;
    }

    public static GameScene getGameScene() {
        return Game.gameScene;
    }

    public static void setGameScene(GameScene gameScene) {
        Game.gameScene = gameScene;
        Game.primaryStage.setScene(Game.gameScene.getInner());
    }

    public synchronized static String getAsset(String path) {
        if (cachedAssetUrls == null) {
            cachedAssetUrls = new HashMap<String, String>();
        }

        String finalPath = cachedAssetUrls.get(path);
        if (finalPath == null) {
            finalPath = Game.class.getResource(
                    GAME_ASSETS_PATH + path).toExternalForm();
            cachedAssetUrls.put(path, finalPath);
        }

        return finalPath;
    }

    public static Path getAssetAsPath(String path) {
        try {
            return Path.of(Game.class.getResource(GAME_ASSETS_PATH + path).toURI());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static boolean isDirectionActive(byte activeDirections, byte directionFlag) {
        return ((activeDirections & directionFlag) == directionFlag);
    }

}
