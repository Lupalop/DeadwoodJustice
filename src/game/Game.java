package game;

import game.scenes.GameScene;
import game.scenes.LevelScene;
import javafx.stage.Stage;

public class Game {

    public static final String GAME_NAME = "Deadwood Justice";
    public static final String GAME_ASSETS_PATH = "/game/assets/";
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    public static final boolean DEBUG_MODE = false;
    public static final boolean FLAG_SMARTER_MOBS = false;
    public static final boolean FLAG_DELAY_IF_BOSS_IS_ALIVE = false;
    public static final boolean FLAG_FIX_DRAW_ORDER = false;
    public static final boolean FLAG_MOBS_CHECK_PASSABILITY = false;

    private Stage primaryStage;
    private GameScene gameScene;
    private GameTimer gameTimer;
    private boolean initialized;

    public Game(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setResizable(false);
        this.primaryStage.setTitle(GAME_NAME);
        this.gameTimer = new GameTimer(this);
    }

    public void run() {
        if (this.initialized) {
            return;
        }

        this.setGameScene(new LevelScene(this));
        this.gameTimer.start();
        this.primaryStage.show();
        this.initialized = true;
    }

    public GameScene getGameScene() {
        return this.gameScene;
    }

    public void setGameScene(GameScene gameScene) {
        this.gameScene = gameScene;
        this.primaryStage.setScene(this.gameScene.getInnerScene());
    }
    
    public static String getAsset(String path) {
        return Game.class.getResource(GAME_ASSETS_PATH + path).toExternalForm();
    }
    
}
