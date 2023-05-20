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
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

public class CreditsScene implements GameScene {

    private static final String PREFIX_SUBHEADING = "-";
    private static final String PREFIX_HEADING = "+";

    private static final double SCROLL_SPEED = 0.02f;

    private Group root;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;

    private TimedActionManager actions;
    private LevelMap levelMap;

    public CreditsScene() {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT, Game.COLOR_MAIN);
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
    private ArrayList<Text> textNodes;
    private double textNodesHeight;
    private double scrollPosition;

    private void addMenuControls() {
        backButton = new Button(Tile.SIZE_MID, Tile.SIZE_MID, 0, this);
        backButton.setText(Button.TEXT_BACK);
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
            // Case 1: heading
            if (textString.startsWith(PREFIX_HEADING)) {
                node.setFont(Game.FONT_ALT_48);
                node.setText(textString.substring(1));
                node.setFill(Game.COLOR_MAIN);
                node.setStroke(Game.COLOR_ACCENT);
                node.setId(PREFIX_HEADING);
            // Case 2: subheading
            } else if (textString.startsWith(PREFIX_SUBHEADING)) {
                node.setFont(Game.FONT_32);
                node.setText(textString.substring(1));
                node.setFill(Game.COLOR_MAIN);
                node.setStroke(Game.COLOR_ACCENT);
                node.setId(PREFIX_SUBHEADING);
            // Case 3: regular text.
            } else {
                node.setFont(Game.FONT_ALT_32);
                node.setFill(Color.WHITE);
                node.setStroke(Color.BLACK);
            }
            node.setStrokeWidth(2);
            node.setStrokeType(StrokeType.OUTSIDE);

            textNodes.add(node);
            textNodesHeight += node.getBoundsInLocal().getHeight();
        }
    }

    private void drawScrollingNodes(GraphicsContext gc) {
        double distanceFromTop = Game.WINDOW_MAX_HEIGHT;

        for (Text node : textNodes) {
            scrollPosition -= SCROLL_SPEED;
            double x = (Game.WINDOW_MAX_WIDTH / 2)
                    - (node.getBoundsInLocal().getWidth() / 2);
            double y = (distanceFromTop + scrollPosition);
            // XXX: Setting the x/y coordinates of text nodes repeatedly
            // seems to cause performance issues, even with a fairly
            // capable PC. To workaround that, we'll just draw the text
            // and its stroke via GraphicsContext and reuse the
            // information that we have from the text nodes.
            gc.setFont(node.getFont());
            gc.setFill(node.getFill());
            gc.setStroke(node.getStroke());
            gc.setLineWidth(node.getStrokeWidth() * 2);
            gc.strokeText(node.getText(), x, y);
            gc.fillText(node.getText(), x, y);
            // Draw a line below headings.
            if (node.getId() != null && node.getId().equals(PREFIX_HEADING)) {
                gc.strokeLine(
                        x, y + 10,
                        x + node.getBoundsInLocal().getWidth(), y + 10);
            }
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
    }

    @Override
    public void draw(long now) {
        this.gc.clearRect(0, 0, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.levelMap.draw(gc);

        drawScrollingNodes(gc);

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
