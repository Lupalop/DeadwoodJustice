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

    private ArrayList<Bullet> bullets;
    public final static Image SHIP_IMAGE = new Image(
            Game.getAsset("ship.png"),
            Ship.SHIP_WIDTH, Ship.SHIP_WIDTH, false, false);
    private final static int SHIP_WIDTH = 50;

    public Ship(String name, int x, int y) {
        super(x, y);
        this.name = name;
        Random r = new Random();
        this.strength = r.nextInt(151) + 100;
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
        
        if (x + dx > 0 && this.x + this.dx < Game.WINDOW_WIDTH - this.width) { 
            this.x += this.dx;
        }
        if (y + dy > 0 && y + dy < Game.WINDOW_HEIGHT - this.height) {
            this.y += this.dy;
        }
    }

    // method that will listen and handle the key press events
    public void handleKeyPressEvent(Scene scene) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                moveMyShip(code);
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                stopMyShip(code);
            }
        });
    }

    // method that will move the ship depending on the key pressed
    private void moveMyShip(KeyCode ke) {
        if (ke == KeyCode.UP)
            this.setDY(-3);

        if (ke == KeyCode.LEFT)
            this.setDX(-3);

        if (ke == KeyCode.DOWN)
            this.setDY(3);

        if (ke == KeyCode.RIGHT)
            this.setDX(3);

        if (ke == KeyCode.SPACE)
            this.shoot();

        System.out.println(ke + " key pressed.");
    }

    // method that will stop the ship's movement; set the ship's DX and DY to 0
    private void stopMyShip(KeyCode ke) {
        this.setDX(0);
        this.setDY(0);
    }

}
