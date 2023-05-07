package miniprojtemplate;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.image.Image;

public class Ship extends Sprite {
    private String name;
    private int strength;
    private boolean alive;

    private ArrayList<Bullet> bullets;
    public final static Image SHIP_IMAGE = new Image("images/ship.png",
            Ship.SHIP_WIDTH, Ship.SHIP_WIDTH, false, false);
    private final static int SHIP_WIDTH = 50;

    public Ship(String name, int x, int y) {
        super(x, y);
        this.name = name;
        Random r = new Random();
        this.strength = r.nextInt(151) + 100;
        this.alive = true;
        this.bullets = new ArrayList<Bullet>();
        this.loadImage(Ship.SHIP_IMAGE);
    }

    public boolean isAlive() {
        if (this.alive)
            return true;
        return false;
    }
    public String getName() {
        return this.name;
    }

    public void die() {
        this.alive = false;
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
        /*
         * TODO: Instantiate a new bullet and add it to the bullets arraylist of
         * ship
         */
    }

    // method called if up/down/left/right arrow key is pressed.
    public void move() {
        /*
         * TODO: Only change the x and y position of the ship if the current x,y
         * position is within the gamestage width and height so that the ship
         * won't exit the screen
         */
        this.x += this.dx;
        this.y += this.dy;
    }

}
