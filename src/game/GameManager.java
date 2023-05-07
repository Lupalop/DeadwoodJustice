package game;

import game.scenes.GameScene;
import game.scenes.LevelScene;
import javafx.stage.Stage;

public class GameManager {

    public static final String GAME_NAME = "Mini Ship Shooting Game";
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    private Stage primaryStage;
    private GameScene gameScene;
    private GameTimer gameTimer;
    private boolean initialized;

    public GameManager(Stage primaryStage) {
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
    
}
