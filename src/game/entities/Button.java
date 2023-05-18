package game.entities;

import game.Game;
import game.GameScene;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

public class Button extends Sprite {

    private static final Tileset TILESET =
            new Tileset("tilemap_ui.png", 4, 8);
    private static int TILE_SIZE = 32;

    private static final int BUTTON_PARTS_COUNT = 4;
    private static final int BUTTON_NORMAL_PARTS[] = {19, 20, 20, 21};
    private static final int BUTTON_HOVER_PARTS[] = {27, 28, 28, 29};
    private static final int BUTTON_ACTIVE_PARTS[] = {24, 25, 25, 26};

    private boolean isHover;
    private boolean isActive;
    private Runnable clickAction;
    public void setClickAction(Runnable clickAction) {
        this.clickAction = clickAction;
    }

    private Text text;
    
    public Button(int xPos, int yPos, GameScene scene) {
        super(xPos, yPos);
        TILESET.setScale(2);
        this.handleMouseEvent(scene.getInnerScene());
        this.isHover = false;
        this.isActive = false;
        this.setWidth(TILE_SIZE * BUTTON_PARTS_COUNT);
        this.setHeight(TILE_SIZE);
        text = new Text();
        text.setFont(Game.FONT_BTN);
        text.setFill(Paint.valueOf("49276d"));
        scene.getRoot().getChildren().add(text);
    }

    private void doState(double x, double y, boolean hover) {
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
                doState(event.getSceneX(), event.getSceneY(), true);
            }
        });

        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                doState(event.getSceneX(), event.getSceneY(), false);
            }
        });
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isActive) {
                    isActive = false;
                    doState(event.getSceneX(), event.getSceneY(), true);
                    if (isHover && clickAction != null) {
                        clickAction.run();
                    }
                }
            }
        });
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
        for (int i = 0; i < BUTTON_PARTS_COUNT; i++) {
            TILESET.draw(gc, this.getX() + (TILE_SIZE * i), this.getY(), parts[i]);
        }
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.text.setX(this.getBounds().getMinX()
                + (this.getBounds().getWidth() / 2)
                - (this.text.getBoundsInLocal().getWidth() / 2));
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.text.setY(this.getBounds().getMaxY()
                - ((this.getBounds().getHeight() - Game.FONT_SIZE_BTN) / 2));
    }

    public String getText() {
        return text.getText();
    }

    public void setText(String text) {
        this.text.setText(text);
    }
    
}
