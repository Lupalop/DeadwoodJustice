package game.scenes;

import game.ActionTimerManager;
import game.Game;
import game.LevelMap;
import game.UIUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * This abstract class provides common features for handling
 * custom game scene logic, such as automatic creation of the
 * inner JavaFX scene and canvas.
 * @author Francis Dominic Fajardo
 */
public abstract class GameScene {

    /** JFX: Inner scene. */
    protected Scene scene;
    /** JFX: root group node. */
    protected Group root;
    /** JFX: main canvas. */
    protected Canvas canvas;
    /** JFX: graphics context from the main canvas. */
    protected GraphicsContext gc;
    /** Action timer manager for timers associated with this scene. */
    protected ActionTimerManager timers;
    /** The game level map. */
    protected LevelMap levelMap;

    /**
     * Constructs an empty instance of GameScene.
     */
    protected GameScene() {
        this.initialize(false);
    }

    /**
     * Initializes this game scene.
     * @param excludeProps whether props should not be generated
     *        in the associated level map.
     */
    protected void initialize(boolean excludeProps) {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT, UIUtils.COLOR_PRIMARY);
        this.canvas = new Canvas(Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.root.getChildren().add(canvas);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);

        this.timers = new ActionTimerManager();
        this.levelMap = new LevelMap(excludeProps);
        this.levelMap.generate();
        this.levelMap.generateProps();
    }

    /**
     * Updates this scene's state.
     * @param now The timestamp of the current frame given in nanoseconds.
     */
    public abstract void update(long now);

    /**
     * Draws this scene.
     * @param now
     */
    public abstract void draw(long now);

    /**
     * Retrieves the inner JavaFX scene.
     * @return a Scene object.
     */
    public Scene getInner() {
        return this.scene;
    }

    /**
     * Retrieves the root node of the inner JavaFX scene.
     * @return a Group object.
     */
    public Group getRoot() {
        return this.root;
    }

    /**
     * Retrieves the action timer manager associated with this scene.
     * @return an ActionTimerManager object.
     */
    public ActionTimerManager getTimers() {
        return this.timers;
    }

    /**
     * Retrieves the background music to be played when the game
     * switches to this scene.
     * @return a String object.
     */
    public abstract String getBGM();

}
