package game;

import java.util.HashMap;

import javafx.scene.text.Font;
import javafx.stage.Stage;

public final class Game {

    private static HashMap<String, String> cachedAssetUrls =
            new HashMap<String, String>();

    public static final String GAME_NAME = "Deadwood Justice";
    public static final String GAME_ASSETS_PATH = "/game/assets/";

    private static final String FONT_PATH =
            Game.getAsset("THALEAHFAT.ttf");
    private static final String FONT_MUP_PATH =
            Game.getAsset("MATCHUPPRO.ttf");

    public static final int FONT_SIZE_32 = 32;
    public static final Font FONT_32 =
            Font.loadFont(FONT_PATH, FONT_SIZE_32);

    public static final int FONT_SIZE_BTN = 21;
    public static final Font FONT_BTN =
            Font.loadFont(FONT_MUP_PATH, 16 * 3);

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    public final static byte KEY_DIR_RIGHT = 0x8;
    public final static byte KEY_DIR_LEFT = 0x4;
    public final static byte KEY_DIR_DOWN = 0x2;
    public final static byte KEY_DIR_UP = 0x1;
    
    public static final boolean DEBUG_MODE = false;
    public static final boolean FLAG_DIRECTIONAL_SHOOTING = false;
    public static final boolean FLAG_SMARTER_MOBS = true;
    public static final boolean FLAG_DELAY_IF_BOSS_IS_ALIVE = false;
    public static final boolean FLAG_FIX_DRAW_ORDER = true;
    public static final boolean FLAG_MOBS_CHECK_PASSABILITY = true;
    public static final boolean FLAG_IGNORE_PROP_COLLISION = true;
    public static final boolean FLAG_MOBS_CAN_SHOOT = false;

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
        Game.setGameScene(new LevelScene());
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
        Game.primaryStage.setScene(Game.gameScene.getInnerScene());
    }
    
    public static String getAsset(String path) {
        String finalPath = cachedAssetUrls.get(path);
        if (finalPath == null) {
            finalPath = Game.class.getResource(
                    GAME_ASSETS_PATH + path).toExternalForm();
            cachedAssetUrls.put(path, finalPath);
        }
        return finalPath;
    }
    
    public static boolean isDirectionActive(byte activeDirections, byte directionFlag) {
        return ((activeDirections & directionFlag) == directionFlag);
    }
    
}
