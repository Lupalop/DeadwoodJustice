package game;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import game.entities.Button;
import game.entities.Sprite;
import game.entities.Tile;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

public class CreditsScene implements GameScene {

    private static final double SCROLL_SPEED = 0.2f;

    private Group root;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;

    private TimedActionManager actions;
    private LevelMap levelMap;

    public CreditsScene() {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT, Color.valueOf("eeca84"));
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
    }

    private Button backButton;
    private ArrayList<Text> textNodes;
    private double textNodesHeight;
    private double scrollPosition;

    private void addMenuControls() {
        backButton = new Button(Tile.SIZE_MID, Tile.SIZE_MID, 0, this);
        backButton.setText("<-");
        backButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new MainMenuScene());
            }
        });

        List<String> creditsText;
        try {
            creditsText = Files.readAllLines(Game.getAssetAsPath("credits.txt"));
        } catch (IOException e) {
            if (Game.DEBUG_MODE) {
                e.printStackTrace();
            }
            return;
        }

        textNodesHeight = 0;
        textNodes = new ArrayList<Text>();
        for (String textString : creditsText) {
            Text node = new Text(textString);
            if (textString.startsWith("+")) {
                node.setFont(Game.FONT_BTN);
                node.setText(textString.substring(1));
                node.setFill(Paint.valueOf("eeca84"));
                node.setStroke(Paint.valueOf("49276d"));
            } else {
                node.setFont(Game.FONT_32);
                node.setFill(Color.WHITE);
                node.setStroke(Color.BLACK);
            }
            node.setStrokeWidth(2);
            node.setStrokeType(StrokeType.OUTSIDE);

            textNodes.add(node);
            textNodesHeight += node.getBoundsInLocal().getHeight();
            this.root.getChildren().add(node);
        }
    }

    private void updateScrollingNodes() {
        double distanceFromTop = Game.WINDOW_MAX_HEIGHT;

        for (Text node : textNodes) {
            scrollPosition -= SCROLL_SPEED;
            node.setX((Game.WINDOW_MAX_WIDTH / 2)
                    - (node.getBoundsInLocal().getWidth() / 2));
            node.setY(distanceFromTop + scrollPosition);
            distanceFromTop += node.getBoundsInLocal().getHeight();
        }

        if (scrollPosition <= -(Game.WINDOW_MAX_HEIGHT + textNodesHeight)) {
            scrollPosition = 0;
        }
    }

    @Override
    public void update(long now) {
        for (Sprite sprite : this.levelMap.getSprites()) {
            sprite.update(now);
        }
        this.levelMap.update(now);
        this.actions.update(now);

        backButton.update(now);
        updateScrollingNodes();
    }

    @Override
    public void draw(long now) {
        this.gc.clearRect(0, 0, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.levelMap.draw(gc);

        backButton.draw(gc);
    }

    @Override
    public Scene getInner() {
        return this.scene;
    }

    @Override
    public Group getRoot() {
        return root;
    }

    public TimedActionManager getActions() {
        return this.actions;
    }

    public LevelMap getLevelMap() {
        return this.levelMap;
    }

}
