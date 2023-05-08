package game.entities;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Ship extends Sprite {

    private String name;
    private int strength;
    private boolean isShootBlocked;
    private boolean isAlive;
    private boolean isDying;
    private ArrayList<Bullet> bullets;
    private byte activeDirections;

    public final static Image FRAMESET_W = new Image(
            Game.getAsset("player_sheet_w.png"));
    public final static Image FRAMESET_SW = new Image(
            Game.getAsset("player_sheet_sw.png"));
    public final static Image FRAMESET_N = new Image(
            Game.getAsset("player_sheet_n.png"));
    public final static Image FRAMESET_NW = new Image(
            Game.getAsset("player_sheet_nw.png"));
    public final static Image FRAMESET_S = new Image(
            Game.getAsset("player_sheet_s.png"));

    public final static int FRAMESET_ROWS = 5;
    public final static int FRAMESET_COLUMNS = 14;
    public final static int[] FRAMESET_OFFSET = 
            new int[] { 15, 17, 9, 3 };

    private final static int SHIP_SPEED = 5;

    private final static byte FLAG_DIR_UP = 0x1;
    private final static byte FLAG_DIR_DOWN = 0x2;
    private final static byte FLAG_DIR_LEFT = 0x4;
    private final static byte FLAG_DIR_RIGHT = 0x8;    
    
    public Ship(String name, int x, int y) {
        super(x, y);
        
        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 5);
        this.setScale(2);
        this.setBoundsOffset(FRAMESET_OFFSET);

        this.name = name;
        Random r = new Random();
        this.strength = r.nextInt(151) + 100;
        if (Game.DEBUG_MODE) {
            this.strength = 1;
        }
        this.isShootBlocked = false;
        this.isAlive = true;
        this.isDying = false;
        this.bullets = new ArrayList<Bullet>();
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public boolean isDying() {
        return this.isDying;
    }
    
    public String getName() {
        return this.name;
    }

    // method that will get the bullets 'shot' by the ship
    public ArrayList<Bullet> getBullets() {
        return this.bullets;
    }

    // method called if spacebar is pressed
    public void shoot() {
        if (!this.isAlive()) {
            return;
        }

        // compute for the x and y initial position of the bullet
        int x = (int) (this.getBounds().getMaxX());
        int y = (int) (this.getBounds().getMinY()
                + (this.getBounds().getHeight() / 2)
                - (Bullet.BULLET_IMAGE.getHeight() / 2));
        Bullet bullet = new Bullet(x, y);
        this.bullets.add(bullet);
    }

    public void increaseStrength(int value) {
        // This must be a positive integer.
        if (value < 0) {
            return;
        }
        this.strength += value;
    }

    public void reduceStrength(int value) {
        // This must be a positive integer.
        if (value < 0) {
            return;
        }
        this.strength -= value;
        if (this.strength < 0) {
            this.isAlive = false;
            this.isDying = true;
            this.setFrameAutoReset(false);
            this.setMinMaxFrame(56, 69);
        } else {
            this.playFrames(56, 58, null, TimeUnit.MILLISECONDS.toNanos(200));
        }
    }
    
    private boolean isDirectionActive(byte directionFlag) {
        return ((this.activeDirections & directionFlag) == directionFlag);
    }
    
    // method called if up/down/left/right arrow key is pressed.
    public void update(long currentNanoTime) {
        super.update(currentNanoTime);
        
        if (this.isDying) {
            if (this.isFrameSequenceDone()) {
                this.isDying = false;
                // TODO: trigger game over screen.
                System.out.println("Game over.");
            }
        }

        if (!this.isAlive()) {
            return;
        }
        
        if (this.getBounds().getMinX() + dx >= 0 &&
                isDirectionActive(FLAG_DIR_LEFT)) {
            this.dx = -SHIP_SPEED;
        } else if (this.getBounds().getMinX() + this.dx <= Game.WINDOW_WIDTH - this.getBounds().getWidth()
                && isDirectionActive(FLAG_DIR_RIGHT)) {
            this.dx = SHIP_SPEED;
        } else {
            this.dx = 0;
        }

        if (this.getBounds().getMinY() + dy >= 0 &&
                isDirectionActive(FLAG_DIR_UP)) {
            this.dy = -SHIP_SPEED;
        } else if (this.getBounds().getMinY() + dy <= Game.WINDOW_HEIGHT - this.getBounds().getHeight()
                && isDirectionActive(FLAG_DIR_DOWN)) {
            this.dy = SHIP_SPEED;
        } else {
            this.dy = 0;
        }

        if (this.dx != 0 || this.dy != 0) {
            this.setMinMaxFrame(14, 21);
        } else {
            this.setMinMaxFrame(0,  5);
        }
        
        this.addX(this.dx);
        this.addY(this.dy);
    }

    // method that will listen and handle the key press events
    public void handleKeyPressEvent(Scene scene) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                startMoving(code);
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                stopMoving(code);
            }
        });
    }

    // method that will move the ship depending on the key pressed
    private void startMoving(KeyCode keyCode) {
        if (!this.isAlive()) {
            return;
        }
        
        switch (keyCode) {
        case UP:
            this.activeDirections |= FLAG_DIR_UP;
            break;
        case DOWN:
            this.activeDirections |= FLAG_DIR_DOWN;
            break;
        case LEFT:
            this.activeDirections |= FLAG_DIR_LEFT;
            this.isShootBlocked = true;
            break;
        case RIGHT:
            this.activeDirections |= FLAG_DIR_RIGHT;
            break;
        case SPACE:
            if (this.isShootBlocked) {
                break;
            }
            // We can't shoot in other directions, following a limitation
            // imposed by the problem domain.
            this.isShootBlocked = true;
            this.playFrames(28, 33, FRAMESET_W, TimeUnit.MILLISECONDS.toNanos(50));
            this.shoot();
            break;
        default:
            break;
        }
        this.updateFrameSet();
    }

    // method that will stop the ship's movement; set the ship's DX and DY to 0
    private void stopMoving(KeyCode keyCode) {
        switch (keyCode) {
        case UP:
            this.activeDirections &= ~FLAG_DIR_UP;
            break;
        case DOWN:
            this.activeDirections &= ~FLAG_DIR_DOWN;
            break;
        case LEFT:
            this.activeDirections &= ~FLAG_DIR_LEFT;
            break;
        case RIGHT:
            this.activeDirections &= ~FLAG_DIR_RIGHT;
            break;
        case SPACE:
            if (isDirectionActive(FLAG_DIR_LEFT)) {
                break;
            }
            this.isShootBlocked = false;
        default:
            break;
        }
        this.updateFrameSet();
    }

    private void updateFrameSet() {
        this.flipHorizontal(false);
        if (isDirectionActive(FLAG_DIR_UP)) {
            if (isDirectionActive(FLAG_DIR_RIGHT)) {
                this.setFrameSet(FRAMESET_NW);
            } if (isDirectionActive(FLAG_DIR_LEFT)) {
                this.setFrameSet(FRAMESET_NW);
                this.flipHorizontal(true);
            } else {
                this.setFrameSet(FRAMESET_N);
            }
        } else if (isDirectionActive(FLAG_DIR_DOWN)) {
            if (isDirectionActive(FLAG_DIR_RIGHT)) {
                this.setFrameSet(FRAMESET_SW);
            } else if (isDirectionActive(FLAG_DIR_LEFT)) {
                this.setFrameSet(FRAMESET_SW);
                this.flipHorizontal(true);
            } else {
                this.setFrameSet(FRAMESET_S);
            }
        } else {
            if (isDirectionActive(FLAG_DIR_LEFT)) {
                this.flipHorizontal(true);
            }
            this.setFrameSet(FRAMESET_W);
        }
    }
    
}
