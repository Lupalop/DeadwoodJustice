package game.entities;

import javafx.scene.image.Image;

public class Fish extends Sprite {

    public static final int MAX_FISH_SPEED = 5;
    public final static Image FISH_IMAGE = new Image(
            Fish.class.getResource("/game/assets/fish.png").toExternalForm(),
            Fish.FISH_WIDTH, Fish.FISH_WIDTH, false, false);
    public final static int FISH_WIDTH = 50;
    private boolean alive;
    // attribute that will determine if a fish will initially move to the right
    private boolean moveRight;
    private int speed;

    Fish(int x, int y) {
        super(x, y);
        this.alive = true;
        this.loadImage(Fish.FISH_IMAGE);
        /*
         * TODO: Randomize speed of fish and moveRight's initial value
         */

    }

    // method that changes the x position of the fish
    void move() {
        /*
         * TODO: If moveRight is true and if the fish hasn't reached the right
         * boundary yet, move the fish to the right by changing the x position
         * of the fish depending on its speed else if it has reached the
         * boundary, change the moveRight value / move to the left Else, if
         * moveRight is false and if the fish hasn't reached the left boundary
         * yet, move the fish to the left by changing the x position of the fish
         * depending on its speed. else if it has reached the boundary, change
         * the moveRight value / move to the right
         */
    }

    // getter
    public boolean isAlive() {
        return this.alive;
    }

}
