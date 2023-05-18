package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class Bullet extends Sprite {

    public final static int BULLET_SPEED = 20;
    public final static Image BULLET_IMAGE = new Image(
            Game.getAsset("bullet.png"),
            Bullet.BULLET_WIDTH, Bullet.BULLET_WIDTH, false, false);
    public final static int BULLET_WIDTH = 20;

    public Bullet(int x, int y, byte activeDirections, boolean isDirectional) {
        super(x, y);
        this.isDirectional = isDirectional;
        this.setImage(Bullet.BULLET_IMAGE);
        this.computeDestination(activeDirections);
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

    private static final int OFFSET_N = -10;
    private static final int OFFSET_NE = -10;
    private static final int OFFSET_NW = -35;
    private static final int OFFSET_S = -35;
    private static final int OFFSET_SE = -30;
    private static final int OFFSET_SW = -20;
    private static final int OFFSET_W = -35;
    private boolean isDirectional;

    private void computeDestination(byte activeDirections) {
        if (isDirectional) {
            boolean noHorizontalDirections = !Game.isDirectionActive(activeDirections, Game.KEY_DIR_RIGHT)
                    && !Game.isDirectionActive(activeDirections, Game.KEY_DIR_LEFT);
            if (Game.isDirectionActive(activeDirections, Game.KEY_DIR_UP)) {
                this.dy = -Bullet.BULLET_SPEED;
                if (noHorizontalDirections) {
                    this.addX(OFFSET_N);
                }
            } else if (Game.isDirectionActive(activeDirections, Game.KEY_DIR_DOWN)) {
                this.dy = Bullet.BULLET_SPEED;
                if (noHorizontalDirections) {
                    this.addX(OFFSET_S);
                }
            } else {
                this.dx = Bullet.BULLET_SPEED;
            }
            
            if (Game.isDirectionActive(activeDirections, Game.KEY_DIR_RIGHT)) {
                this.dx = Bullet.BULLET_SPEED;
                if (Game.isDirectionActive(activeDirections, Game.KEY_DIR_UP)) {
                    this.addX(OFFSET_NE);
                } else if (Game.isDirectionActive(activeDirections, Game.KEY_DIR_DOWN)) {
                    this.addX(OFFSET_SE);
                }
            } else if (Game.isDirectionActive(activeDirections, Game.KEY_DIR_LEFT)) {
                this.dx = -Bullet.BULLET_SPEED;
                if (Game.isDirectionActive(activeDirections, Game.KEY_DIR_UP)) {
                    this.addX(OFFSET_NW);
                } else if (Game.isDirectionActive(activeDirections, Game.KEY_DIR_DOWN)) {
                    this.addX(OFFSET_SW);
                } else {
                    this.addX(OFFSET_W);
                }
            }
        } else {
            this.dx = Bullet.BULLET_SPEED;
        }
    }
}