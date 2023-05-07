package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class Bullet extends Sprite {

    private final int BULLET_SPEED = 20;
    public final static Image BULLET_IMAGE = new Image(
            Bullet.class.getResource("/game/assets/bullet.png").toExternalForm(),
            Bullet.BULLET_WIDTH, Bullet.BULLET_WIDTH, false, false);
    public final static int BULLET_WIDTH = 20;

    public Bullet(int x, int y) {
        super(x, y);
        this.loadImage(Bullet.BULLET_IMAGE);
        this.setDX(BULLET_SPEED);
    }

    // method that will move/change the x position of the bullet
    public void update() {
        this.x += this.dx;
        this.y += this.dy;

        if (this.x > Game.WINDOW_WIDTH) {
            this.setVisible(false);
        }
    }

}