package game.scenes;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.UIUtils;
import game.entities.Button;
import game.entities.Tile;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

/**
 * This class handles the Credits scene logic.
 * @author Francis Dominic Fajardo
 */
public final class CreditsScene extends GameScene {

    /** Script: heading prefix. */
    private static final String PREFIX_HEADING = "+";
    /** Script: subheading prefix. */
    private static final String PREFIX_SUBHEADING = "-";
    /** The speed of the rolling credits text. */
    private static final double ROLL_SPEED = 0.02f;

    /** Control: back button. */
    private Button backButton;

    /** Dummy JavaFX Text nodes. */
    private ArrayList<Text> textNodes;
    /** Total height of all Text nodes. */
    private double textNodesHeight;
    /** Current position of the rolling credits text. */
    private double rollPosition;

    /**
     * Constructs an empty instance of CreditsScene.
     */
    public CreditsScene() {
        super();
        this.addMenuControls();
        UIUtils.handleReturnToMainMenu(this);
    }

    /**
     * Adds menu controls, processes the credits text file, and creates
     * dummy text nodes for drawing.
     */
    private void addMenuControls() {
        // Create a back button.
        backButton = new Button(Tile.SIZE_MID, Tile.SIZE_MID,
                Button.SIZE_ARROW_LEFT);
        backButton.attach(this);
        backButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new MainMenuScene());
            }
        });
        this.levelMap.addOverlay(backButton);
        // Read the credits text from file.
        List<String> creditsText;
        try {
            creditsText = Files.readAllLines(Game.getAssetAsPath("credits.txt"));
        } catch (IOException e) {
            if (Game.DEBUG_MODE) {
                e.printStackTrace();
            }
            return;
        }
        // Process the credits text, create Text nodes, and determine
        // their display features based on the prefix.
        textNodesHeight = 0;
        textNodes = new ArrayList<Text>();
        for (String textString : creditsText) {
            Text node = new Text(textString);
            // Case 1: heading
            if (textString.startsWith(PREFIX_HEADING)) {
                node.setFont(UIUtils.FONT_ALT_48);
                node.setText(textString.substring(1));
                node.setFill(UIUtils.COLOR_PRIMARY);
                node.setStroke(UIUtils.COLOR_SECONDARY);
                node.setId(PREFIX_HEADING);
            // Case 2: subheading
            } else if (textString.startsWith(PREFIX_SUBHEADING)) {
                node.setFont(UIUtils.FONT_32);
                node.setText(textString.substring(1));
                node.setFill(UIUtils.COLOR_PRIMARY);
                node.setStroke(UIUtils.COLOR_SECONDARY);
                node.setId(PREFIX_SUBHEADING);
            // Case 3: regular text.
            } else {
                node.setFont(UIUtils.FONT_ALT_32);
                node.setFill(Color.WHITE);
                node.setStroke(Color.BLACK);
            }
            node.setStrokeWidth(2);
            node.setStrokeType(StrokeType.OUTSIDE);

            textNodes.add(node);
            textNodesHeight += node.getBoundsInLocal().getHeight();
        }
    }

    /**
     * Draws the rolling credits text on the specified context.
     * @param gc a GraphicsContext object.
     */
    private void drawScrollingNodes(GraphicsContext gc) {
        double distanceFromTop = Game.WINDOW_MAX_HEIGHT;

        for (Text node : textNodes) {
            rollPosition -= ROLL_SPEED;
            double x = (Game.WINDOW_MAX_WIDTH / 2)
                    - (node.getBoundsInLocal().getWidth() / 2);
            double y = (distanceFromTop + rollPosition);
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

        // Reset the roll position if we've reached the end.
        if (rollPosition <= -(Game.WINDOW_MAX_HEIGHT + textNodesHeight)) {
            rollPosition = 0;
        }
    }

    @Override
    public void update(long now) {
        this.levelMap.update(now);
        this.timers.update(now);

    }

    @Override
    public void draw(long now) {
        this.gc.clearRect(0, 0, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.levelMap.draw(gc);
        drawScrollingNodes(gc);
    }

    @Override
    public String getBGM() {
        return "bgm_01.mp3";
    }

}
