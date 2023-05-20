package game.entities;

import game.Game;
import game.GameScene;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class Button extends Sprite {

    private static final Tile TILE =
            new Tile("tilemap_ui.png", 4, 8);

    private static final int BUTTON_NORMAL_PARTS[] = {19, 20, 21};
    private static final int BUTTON_HOVER_PARTS[] = {27, 28, 29};
    private static final int BUTTON_ACTIVE_PARTS[] = {24, 25, 26};

    private boolean isHover;
    private boolean isActive;
    private int size;

    private Text text;
    private Runnable clickAction;

    public Button(int x, int y, int size, GameScene scene) {
        super(x, y);
        this.handleMouseEvent(scene.getInner());
        this.isHover = false;
        this.isActive = false;
        this.size = size;
        this.setWidth(Tile.SIZE * (2 + size));
        this.setHeight(Tile.SIZE);
        text = new Text();
        text.setFont(Game.FONT_ALT_48);
        text.setFill(Game.COLOR_ACCENT);
        scene.getRoot().getChildren().add(text);
        updateTextX();
        updateTextY();
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

    private void handleMouseEvent(Scene scene) {
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                updateState(event.getSceneX(), event.getSceneY(), true);
            }
        });

        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                updateState(event.getSceneX(), event.getSceneY(), false);
            }
        });
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isActive) {
                    isActive = false;
                    updateState(event.getSceneX(), event.getSceneY(), true);
                    if (isHover && clickAction != null) {
                        clickAction.run();
                    }
                }
            }
        });
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

}
