package game.scenes;

import game.Game;
import game.LevelMap;
import game.PlayerScore;
import game.TimedActionManager;
import game.entities.Button;
import game.entities.Prop;
import game.entities.Sprite;
import game.entities.Tile;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

public class HighScoresScene implements GameScene {

    private Group root;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;
    private GridPane scoresGrid;

    private TimedActionManager actions;
    private LevelMap levelMap;

    public HighScoresScene() {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT, Game.COLOR_PRIMARY);
        this.canvas = new Canvas(Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.root.getChildren().add(canvas);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);

        this.scoresGrid = new GridPane();
        this.scoresGrid.setMaxSize(Game.WINDOW_MAX_WIDTH, Game.WINDOW_MAX_HEIGHT);
        this.scoresGrid.setHgap(10);
        this.scoresGrid.setPadding(new Insets(
                Tile.SIZE_MID * 4,
                Tile.SIZE_MID * 2,
                Tile.SIZE_MID,
                Tile.SIZE_MID * 2));

        ColumnConstraints cc = new ColumnConstraints();
        cc.setMinWidth(150);
        scoresGrid.getColumnConstraints().add(cc);
        cc = new ColumnConstraints();
        cc.setMinWidth(300);
        cc.setHalignment(HPos.RIGHT);
        scoresGrid.getColumnConstraints().add(cc);
        cc = new ColumnConstraints();
        cc.setMinWidth(200);
        cc.setHalignment(HPos.RIGHT);
        scoresGrid.getColumnConstraints().add(cc);

        this.root.getChildren().add(scoresGrid);

        this.actions = new TimedActionManager();
        this.addMenuControls();

        this.levelMap = new LevelMap();
        this.levelMap.generate();
        this.levelMap.generateProps();

        MainMenuScene.handleReturnKeyPressEvent(this);
    }

    private Prop headerProp;
    private Button backButton;

    private void addMenuControls() {
        headerProp = new Prop(0, Tile.SIZE_MID, "ui_highscores.png");
        headerProp.setScale(1);
        headerProp.setX((int) ((Game.WINDOW_MAX_WIDTH / 2) - (headerProp.getWidth() / 2)));

        backButton = new Button(Tile.SIZE_MID, Tile.SIZE_MID, 0);
        backButton.attach(this);
        backButton.setText(Button.TEXT_BACK);
        backButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new MainMenuScene());
            }
        });

        for (int i = 0; i < Game.getHighScores().size(); i++) {
            PlayerScore score = Game.getHighScores().get(i);
            Text nameText = new Text(score.getName());
            nameText.setFill(Game.COLOR_PRIMARY);
            nameText.setStroke(Game.COLOR_TERTIARY);
            nameText.setStrokeType(StrokeType.OUTSIDE);
            nameText.setStrokeWidth(2);
            nameText.setFont(Game.FONT_48);

            Text scoreText = new Text(Integer.toString(score.getScore()));
            scoreText.setFill(Game.COLOR_PRIMARY);
            scoreText.setStroke(Game.COLOR_TERTIARY);
            scoreText.setStrokeType(StrokeType.OUTSIDE);
            scoreText.setStrokeWidth(2);
            scoreText.setFont(Game.FONT_48);

            Text difficultyText = new Text();
            difficultyText.setFill(Game.COLOR_PRIMARY);
            difficultyText.setStroke(Game.COLOR_TERTIARY);
            difficultyText.setStrokeType(StrokeType.OUTSIDE);
            difficultyText.setStrokeWidth(2);
            difficultyText.setFont(Game.FONT_48);

            switch (score.getDifficulty()) {
            case LevelScene.DIFFICULTY_EASY:
                difficultyText.setText("EASY");
                break;
            case LevelScene.DIFFICULTY_MEDIUM:
                difficultyText.setText("MEDIUM");
                break;
            case LevelScene.DIFFICULTY_HARD:
                difficultyText.setText("HARD");
                break;
            default:
                difficultyText.setText("INVALID");
                break;
            }
            this.scoresGrid.add(nameText, 0, i);
            this.scoresGrid.add(scoreText, 1, i);
            this.scoresGrid.add(difficultyText, 2, i);
        }
    }

    @Override
    public void update(long now) {
        for (Sprite sprite : this.levelMap.getSprites()) {
            sprite.update(now);
        }
        this.levelMap.update(now);
        this.actions.update(now);

        headerProp.update(now);
        backButton.update(now);
    }

    @Override
    public void draw(long now) {
        this.gc.clearRect(0, 0, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.levelMap.draw(gc);

        this.headerProp.draw(gc);
        backButton.draw(gc);
        backButton.drawSelector(gc);
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
