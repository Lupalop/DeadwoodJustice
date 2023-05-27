package game.entities;

import game.Game;
import game.scenes.GameScene;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class Button extends Sprite {

    public static final String TEXT_BACK = "<-";
    public static final String TEXT_FORWARD = "->";

    private static final Tile TILE =
            new Tile("tilemap_ui.png", 4, 8);

    private static final int BUTTON_NORMAL_PARTS[] = {19, 20, 21};
    private static final int BUTTON_HOVER_PARTS[] = {27, 28, 29};
    private static final int BUTTON_ACTIVE_PARTS[] = {24, 25, 26};

    private boolean isHover;
    private boolean isActive;
    private boolean isAttached;
    private int size;

    private Text text;
    private Runnable clickAction;

    private EventHandler<MouseEvent> mouseMoveEventHandler;
    private EventHandler<MouseEvent> mousePressedEventHandler;
    private EventHandler<MouseEvent> mouseReleasedEventHandler;

    public Button(int x, int y, int size) {
        super(x, y);
        this.isHover = false;
        this.isActive = false;
        this.size = size;
        this.setWidth(Tile.SIZE * (2 + size));
        this.setHeight(Tile.SIZE);
        text = new Text();
        text.setFont(Game.FONT_ALT_48);
        text.setFill(Game.COLOR_ACCENT);
        updateTextX();
        updateTextY();

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
        int[] parts = BUTTON_NORMAL_PARTS;
        if (this.isHover) {
            parts = BUTTON_HOVER_PARTS;
        }
        if (this.isActive) {
            parts = BUTTON_ACTIVE_PARTS;
        }
        TILE.draw(gc, this.getX() + (Tile.SIZE_MID * 0), this.getY(), parts[0]);
        for (int i = 1; i <= size; i++) {
            TILE.draw(gc, this.getX() + (Tile.SIZE_MID * i), this.getY(), parts[1]);
        }
        TILE.draw(gc, this.getX() + (Tile.SIZE_MID * (size + 1)), this.getY(), parts[2]);
    }


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

    private void updateTextX() {
        this.text.setX(this.getBounds().getMinX()
                + (this.getBounds().getWidth() / 2)
                - (this.text.getBoundsInLocal().getWidth() / 2));
    }

    private void updateTextY() {
        this.text.setY(this.getBounds().getMaxY()
                - ((this.getBounds().getHeight() - Game.FONT_SIZE_BTN) / 2));
    }

    public String getText() {
        return text.getText();
    }

    public void setText(String text) {
        this.text.setText(text);
        updateTextX();
        updateTextY();
    }

    public void setClickAction(Runnable clickAction) {
        this.clickAction = clickAction;
    }

    public void click() {
        this.clickAction.run();
    }

}
