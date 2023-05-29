package game.scenes;

import game.Game;
import game.LevelMap;
import game.TimedActionManager;
import game.UIUtils;
import game.entities.Button;
import game.entities.Sprite;
import game.entities.Tile;
import game.entities.props.HeaderProp;
import game.entities.props.Prop;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class HelpScene implements GameScene {

    private Group root;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;

    private TimedActionManager actions;
    private LevelMap levelMap;

    public HelpScene() {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT, UIUtils.COLOR_PRIMARY);
        this.canvas = new Canvas(Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.root.getChildren().add(canvas);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);

        this.actions = new TimedActionManager();
        this.addMenuControls();

        this.levelMap = new LevelMap();
        this.levelMap.generate();
        this.levelMap.generateProps();

        MainMenuScene.handleReturnKeyPressEvent(this);
    }

    private Button backButton;
    private Prop helpImage;

    private void addMenuControls() {
        backButton = new Button(Tile.SIZE_MID, Tile.SIZE_MID, 0);
        backButton.attach(this);
        backButton.setText(Button.TEXT_BACK);
        backButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new MainMenuScene());
            }
        });

        helpImage = new HeaderProp(0, 0, HeaderProp.HOW_TO_PLAY);
        helpImage.setScale(1);
    }

    @Override
    public void update(long now) {
        for (Sprite sprite : this.levelMap.getSprites()) {
            sprite.update(now);
        }
        this.levelMap.update(now);
        this.actions.update(now);

        backButton.update(now);
        helpImage.update(now);
    }

    @Override
    public void draw(long now) {
        this.gc.clearRect(0, 0, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.levelMap.draw(gc);

        backButton.draw(gc);
        backButton.drawSelector(gc);
        helpImage.draw(gc);
    }

    @Override
    public Scene getInner() {
        return this.scene;
    }

    @Override
    public Group getRoot() {
        return root;
    }

    @Override
    public TimedActionManager getActions() {
        return this.actions;
    }

    public LevelMap getLevelMap() {
        return this.levelMap;
    }

    @Override
    public String getBGM() {
        return "bgm_01.mp3";
    }

}
