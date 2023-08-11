package game.entities;

import game.Game;
import game.UIUtils;
import game.scenes.GameScene;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

/**
 * This class represents a button with a sprite selection
 * and configurable mouse click actions.
 * @author Francis Dominic Fajardo
 */
public final class Button extends Sprite {

    /** Path to the button clicked sound effect. */
    public static final String SFX_BUTTON = "sfx_button.wav";

    /** Special size: left arrow. */
    public static final int SIZE_ARROW_LEFT = -100;
    /** Special size: right arrow. */
    public static final int SIZE_ARROW_RIGHT = -101;

    /** Button parts: normal. */
    private static final int BUTTON_NORMAL_PARTS[] = {18, 19, 20};
    /** Button parts: hover. */
    private static final int BUTTON_HOVER_PARTS[] = {21, 22, 23};
    /** Button parts: active. */
    private static final int BUTTON_ACTIVE_PARTS[] = {24, 25, 26};
    /** Button parts: arrow tri-state. */
    private static final int BUTTON_ARROW_PARTS[] = {27, 28, 29};

    /** Width of the button selector outline. */
    private static final int SELECTOR_STROKE_WIDTH = 3;
    /** Roundness of the button selector outline. */
    private static final int SELECTOR_STROKE_RADIUS = 0;

    /** State: is hovered? */
    private boolean isHover;
    /** State: is active? */
    private boolean isActive;
    /** State: is attached to a scene? */
    private boolean isAttached;
    /** Button size. */
    private int size;
    /** Button text. */
    private Text text;
    /** Button action on click. */
    private Runnable clickAction;

    /** Event handler: mouse move. */
    private EventHandler<MouseEvent> mouseMoveEventHandler;
    /** Event handler: mouse pressed. */
    private EventHandler<MouseEvent> mousePressedEventHandler;
    /** Event handler: mouse released. */
    private EventHandler<MouseEvent> mouseReleasedEventHandler;

    /**
     * Constructs an instance of Button.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param size the size of this button (horizontal or special).
     */
    public Button(int x, int y, int size) {
        super(x, y);
        this.isHover = false;
        this.isActive = false;
        this.size = size;
        // Override sizing for special button sizes.
        if (size == SIZE_ARROW_LEFT || size == SIZE_ARROW_RIGHT) {
            this.setWidth(Tile.SIZE);
        } else {
            this.setWidth(Tile.SIZE * (2 + size));
        }
        this.setHeight(Tile.SIZE);
        // Set-up text control and its position.
        text = new Text();
        text.setFont(UIUtils.FONT_ALT_48);
        text.setFill(UIUtils.COLOR_SECONDARY);
        updateTextX();
        updateTextY();
        // Attach mouse event handlers.
        this.mouseMoveEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                updateState(event.getSceneX(), event.getSceneY(), true);
            }
        };
        this.mousePressedEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                updateState(event.getSceneX(), event.getSceneY(), false);
            }
        };
        this.mouseReleasedEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isActive) {
                    isActive = false;
                    updateState(event.getSceneX(), event.getSceneY(), true);
                    if (isHover && clickAction != null) {
                        click();
                    }
                }
            }
        };
    }

    /**
     * Attaches this button to a game scene.
     * @param scene a GameScene object.
     */
    public void attach(GameScene scene) {
        if (this.isAttached) {
            return;
        }
        scene.getRoot().getChildren().add(text);
        Scene innerScene = scene.getInner();
        innerScene.addEventHandler(MouseEvent.MOUSE_MOVED, this.mouseMoveEventHandler);
        innerScene.addEventHandler(MouseEvent.MOUSE_PRESSED, this.mousePressedEventHandler);
        innerScene.addEventHandler(MouseEvent.MOUSE_RELEASED, this.mouseReleasedEventHandler);
        this.isAttached = true;
    }

    /**
     * Detaches this button to a game scene.
     * @param scene a GameScene object.
     */
    public void detach(GameScene scene) {
        if (!this.isAttached) {
            return;
        }
        scene.getRoot().getChildren().remove(text);
        Scene innerScene = scene.getInner();
        innerScene.removeEventHandler(MouseEvent.MOUSE_MOVED, this.mouseMoveEventHandler);
        innerScene.removeEventHandler(MouseEvent.MOUSE_PRESSED, this.mousePressedEventHandler);
        innerScene.removeEventHandler(MouseEvent.MOUSE_RELEASED, this.mouseReleasedEventHandler);
        this.isAttached = false;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (this.size == SIZE_ARROW_LEFT || this.size == SIZE_ARROW_RIGHT) {
            this.drawArrow(gc);
        } else {
            this.drawBase(gc);
        }
    }

    /**
     * Draws an arrow button.
     * @param gc a GraphicsContext object.
     */
    private void drawArrow(GraphicsContext gc) {
        int partId = BUTTON_ARROW_PARTS[0];
        if (this.isHover) {
            partId = BUTTON_ARROW_PARTS[1];
        }
        if (this.isActive) {
            partId = BUTTON_ARROW_PARTS[2];
        }
        UIUtils.TILE.draw(gc, this.getX(), this.getY(), partId,
                this.size == SIZE_ARROW_RIGHT, false);
    }

    /**
     * Draws a base button.
     * @param gc a GraphicsContext object.
     */
    private void drawBase(GraphicsContext gc) {
        int[] parts = BUTTON_NORMAL_PARTS;
        if (this.isHover) {
            parts = BUTTON_HOVER_PARTS;
        }
        if (this.isActive) {
            parts = BUTTON_ACTIVE_PARTS;
        }

        UIUtils.TILE.draw(gc, this.getX() + (Tile.SIZE_MID * 0), this.getY(), parts[0]);
        for (int i = 1; i <= size; i++) {
            UIUtils.TILE.draw(gc, this.getX() + (Tile.SIZE_MID * i), this.getY(), parts[1]);
        }
        UIUtils.TILE.draw(gc, this.getX() + (Tile.SIZE_MID * (size + 1)), this.getY(), parts[2]);
    }

    /**
     * Draws the selector around a button (base button only).
     * @param gc a GraphicsContext object.
     */
    public void drawSelector(GraphicsContext gc) {
        gc.setStroke(UIUtils.COLOR_SECONDARY);
        gc.setLineWidth(SELECTOR_STROKE_WIDTH);
        gc.strokeRoundRect(
                this.getBounds().getMinX(),
                this.getBounds().getMinY(),
                this.getBounds().getWidth(),
                this.getBounds().getHeight(),
                SELECTOR_STROKE_RADIUS,
                SELECTOR_STROKE_RADIUS);
    }

    /**
     * Updates the state of this button.
     * @param x mouse x-coordinate position.
     * @param y mouse y-coordinate position.
     * @param hover whether the button is being hovered.
     */
    private void updateState(double x, double y, boolean hover) {
        boolean newState = getBounds().contains(x, y);
        if (hover) {
            this.isHover = newState;
        } else {
            this.isActive = newState;
        }
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        updateTextX();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        updateTextY();
    }

    /**
     * Updates the x-coordinate position of the text control.
     */
    private void updateTextX() {
        this.text.setX(this.getBounds().getMinX()
                + (this.getBounds().getWidth() / 2)
                - (this.text.getBoundsInLocal().getWidth() / 2));
    }

    /**
     * Updates the y-coordinate position of the text control.
     */
    private void updateTextY() {
        this.text.setY(this.getBounds().getMaxY()
                - ((this.getBounds().getHeight() - UIUtils.FONT_SIZE_BTN) / 2));
    }

    /**
     * Retrieves the text displayed.
     * @return a String.
     */
    public String getText() {
        return text.getText();
    }

    /**
     * Specifies the text displayed.
     * @param text a String.
     */
    public void setText(String text) {
        this.text.setText(text);
        updateTextX();
        updateTextY();
    }

    /**
     * Specifies the click action.
     * @param clickAction a Runnable object.
     */
    public void setClickAction(Runnable clickAction) {
        this.clickAction = clickAction;
    }

    /**
     * Invokes the click action.
     */
    public void click() {
        this.clickAction.run();
        Game.playSFX(SFX_BUTTON);
    }

}
