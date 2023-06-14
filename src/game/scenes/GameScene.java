package game.scenes;

import game.Game;
import game.LevelMap;
import game.TimedActionManager;
import game.UIUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * Base game scene.
 * @author Francis Dominic Fajardo
 */
public abstract class GameScene {

    protected Group root;
    protected Scene scene;
    protected Canvas canvas;
    protected GraphicsContext gc;
    protected TimedActionManager actions;
    protected LevelMap levelMap;

    protected GameScene() {
        this.initialize(false);
    }

    protected void initialize(boolean excludeProps) {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT, UIUtils.COLOR_PRIMARY);
        this.canvas = new Canvas(Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.root.getChildren().add(canvas);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);

        this.actions = new TimedActionManager();
        this.levelMap = new LevelMap(excludeProps);
        this.levelMap.generate();
        this.levelMap.generateProps();
    }

    public abstract void update(long now);
    public abstract void draw(long now);

    public Scene getInner() {
        return this.scene;
    }

    public Group getRoot() {
        return this.root;
    }

    public TimedActionManager getActions() {
        return this.actions;
    }

    public abstract String getBGM();

}
