package game.entities;

import java.util.ArrayList;
import java.util.Random;

import game.Game;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Ship extends Sprite {

    private String name;
    private int strength;
    private boolean isShootBlocked;
    private ArrayList<Bullet> bullets;
    private byte activeDirections;

    public final static Image SHIP_IMAGE = new Image(
            Game.getAsset("ship.png"),
            Ship.SHIP_WIDTH, Ship.SHIP_WIDTH, false, false);
    private final static int SHIP_WIDTH = 50;
    private final static int SHIP_SPEED = 10;

    private final static byte FLAG_DIR_UP = 0x1;
    private final static byte FLAG_DIR_DOWN = 0x2;
    private final static byte FLAG_DIR_LEFT = 0x4;
    private final static byte FLAG_DIR_RIGHT = 0x8;    
    
    public Ship(String name, int x, int y) {
        super(x, y);
        this.name = name;
        Random r = new Random();
        this.strength = r.nextInt(151) + 100;
        this.isShootBlocked = false;
        this.bullets = new ArrayList<Bullet>();
        this.loadImage(Ship.SHIP_IMAGE);
    }

    public boolean isAlive() {
        return this.strength > 0;
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
        // compute for the x and y initial position of the bullet
        int x = (int) (this.x + this.width + 20);
        int y = (int) (this.y + this.height / 2);

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
    }
    
    public void draw(GraphicsContext gc) {
        if (!this.isAlive()) {
            return;
        }
        
        super.draw(gc);
    }
    
    // method called if up/down/left/right arrow key is pressed.
    public void update() {
        if (!this.isAlive()) {
            return;
        }
        
        if (x + dx >= 0 &&
                (this.activeDirections & FLAG_DIR_LEFT) == FLAG_DIR_LEFT) {
            this.dx = -SHIP_SPEED;
        } else if (this.x + this.dx <= Game.WINDOW_WIDTH - this.width &&
                (this.activeDirections & FLAG_DIR_RIGHT) == FLAG_DIR_RIGHT){
            this.dx = SHIP_SPEED;
        } else {
            this.dx = 0;
        }

        if (y + dy >= 0 &&
                (this.activeDirections & FLAG_DIR_UP) == FLAG_DIR_UP) {
            this.dy = -SHIP_SPEED;
        } else if (y + dy <= Game.WINDOW_HEIGHT - this.height &&
                (this.activeDirections & FLAG_DIR_DOWN) == FLAG_DIR_DOWN){
            this.dy = SHIP_SPEED;
        } else {
            this.dy = 0;
        }

        this.x += this.dx;
        this.y += this.dy;
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
        switch (keyCode) {
        case UP:
            this.activeDirections |= FLAG_DIR_UP;
            break;
        case DOWN:
            this.activeDirections |= FLAG_DIR_DOWN;
            break;
        case LEFT:
            this.activeDirections |= FLAG_DIR_LEFT;
            break;
        case RIGHT:
            this.activeDirections |= FLAG_DIR_RIGHT;
            break;
        case SPACE:
            if (this.isShootBlocked) {
                break;
            }
            this.shoot();
            this.isShootBlocked = true;
        default:
            break;
        }
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
            this.isShootBlocked = false;
        default:
            break;
        }
    }

}
