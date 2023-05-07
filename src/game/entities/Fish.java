package game.entities;

import java.util.Random;

import game.Game;
import javafx.scene.image.Image;

public class Fish extends Sprite {

    public static final int MAX_FISH_SPEED = 5;
    public final static Image FISH_IMAGE = new Image(
            Fish.class.getResource("/game/assets/fish.png").toExternalForm(),
            Fish.FISH_WIDTH, Fish.FISH_WIDTH, false, false);
    public final static int FISH_WIDTH = 50;
    public final static int MAX_DAMAGE = 40;
    public final static int MIN_DAMAGE = 30;
    
    private boolean alive;
    // attribute that will determine if a fish will initially move to the right
    private boolean moveRight;
    private int speed;
    private int damage;

    public Fish(int x, int y) {
        super(x, y);
        this.alive = true;
        this.loadImage(Fish.FISH_IMAGE);

        Random rand = new Random();
        this.speed = rand.nextInt(1, MAX_FISH_SPEED);
        this.moveRight = rand.nextBoolean();
        this.damage = rand.nextInt(MIN_DAMAGE, MAX_DAMAGE + 1);
    }

    // method that changes the x position of the fish
    public void update() {
        this.dx = this.moveRight ? this.speed : -this.speed;
        this.x += this.dx;

        if (x + dx > 0 && this.x + this.dx < Game.WINDOW_WIDTH - this.width) { 
            this.x += this.dx;
        } else {
            this.moveRight = !this.moveRight;
        }
    }

    public void update(Ship ship) {
        this.update();

        if (this.collidesWith(ship) && ship.isAlive()) {
            ship.reduceStrength(this.damage);
            this.alive = false;
            return;
        }

        for (Bullet bullet : ship.getBullets()) {
            if (this.collidesWith(bullet)) {
                this.alive = false;
                bullet.setVisible(false);
            }
        }
    }
    
    // getter
    public boolean isAlive() {
        return this.alive;
    }

}
