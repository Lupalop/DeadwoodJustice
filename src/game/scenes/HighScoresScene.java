package game.scenes;

import game.Game;
import game.PlayerScore;
import game.UIUtils;
import game.entities.Button;
import game.entities.HeaderSprite;
import game.entities.Sprite;
import game.entities.Tile;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public final class HighScoresScene extends GameScene {

    private GridPane scoresGrid;
    private Sprite headerProp;
    private Button backButton;

    public HighScoresScene() {
        super();
        this.addScoresGrid();
        this.addMenuControls();
        UIUtils.handleReturnToMainMenu(this);
    }

    private void addScoresGrid() {
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
    }

    private void addMenuControls() {
        headerProp = new HeaderSprite(0, Tile.SIZE_MID, HeaderSprite.HIGH_SCORES);
        headerProp.setScale(1);
        headerProp.setX((int) ((Game.WINDOW_MAX_WIDTH / 2) - (headerProp.getWidth() / 2)));

        backButton = new Button(Tile.SIZE_MID, Tile.SIZE_MID,
                Button.SIZE_ARROW_LEFT);
        backButton.attach(this);
        backButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new MainMenuScene());
            }
        });

        for (int i = 0; i < Game.getHighScores().size(); i++) {
            PlayerScore score = Game.getHighScores().get(i);
            Text nameText = new Text(score.getName());
            nameText.setFill(UIUtils.COLOR_PRIMARY);
            nameText.setFont(UIUtils.FONT_48);

            Text scoreText = new Text(Integer.toString(score.getScore()));
            scoreText.setFill(UIUtils.COLOR_PRIMARY);
            scoreText.setFont(UIUtils.FONT_48);

            Text difficultyText = new Text();
            difficultyText.setFill(UIUtils.COLOR_PRIMARY);
            difficultyText.setFont(UIUtils.FONT_48);

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

        UIUtils.drawMenuBackground(gc, 2, Tile.ALL_VERTICAL);
        this.headerProp.draw(gc);
        backButton.draw(gc);
    }

    @Override
    public String getBGM() {
        return "bgm_01.mp3";
    }

}
