package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class Bullet extends Sprite {

    private final int BULLET_SPEED = 20;
    public final static Image BULLET_IMAGE = new Image(
            Game.getAsset("bullet.png"),
            Bullet.BULLET_WIDTH, Bullet.BULLET_WIDTH, false, false);
    public final static int BULLET_WIDTH = 20;

    public Bullet(int x, int y) {
        super(x, y);
        this.setImage(Bullet.BULLET_IMAGE);
        this.dx = BULLET_SPEED;
    }

    // method that will move/change the x position of the bullet
    public void update(long currentNanoTime) {
        super.update(currentNanoTime);
        
        this.addX(this.dx);
        this.addY(this.dy);

        if (this.getX() > Game.WINDOW_WIDTH) {
            this.setVisible(false);
        }
    }

}