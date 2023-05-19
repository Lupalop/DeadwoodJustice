package game.entities;

import game.Game;
import game.LevelScene;
import javafx.scene.image.Image;

public class Bullet extends LevelSprite {

    public static final Image BULLET_IMAGE = new Image(
            Game.getAsset("bullet.png"),
            Bullet.BULLET_WIDTH, Bullet.BULLET_WIDTH, false, false);

    private static final int BULLET_SPEED = 20;
    private static final int BULLET_WIDTH = 20;
    private static final int BULLET_SCALE = 1;

    private static final int OFFSET_N = -10;
    private static final int OFFSET_NE = -10;
    private static final int OFFSET_NW = -35;
    private static final int OFFSET_S = -35;
    private static final int OFFSET_SE = -30;
    private static final int OFFSET_SW = -20;
    private static final int OFFSET_W = -35;

    private boolean isDirectional;
    private boolean fromOutlaw;
    private Mob mobSource;

    public Bullet(Mob source, LevelScene parent,
            byte activeDirections, boolean isDirectional) {
        super(0, 0, parent);
        this.initialize(source, activeDirections, isDirectional, false);
        this.mobSource = source;
    }

    public Bullet(Outlaw source, LevelScene parent,
            byte activeDirections, boolean isDirectional) {
        super(0, 0, parent);
        this.initialize(source, activeDirections, isDirectional, true);
    }

    private void initialize(Sprite source, byte activeDirections,
            boolean isDirectional, boolean fromOutlaw) {
        this.setX((int) (source.getBounds().getMaxX()));
        this.setY((int) (source.getBounds().getMinY()
            + (source.getBounds().getHeight() / 2)
            - (this.getBounds().getHeight() / 2)));

        this.setImage(Bullet.BULLET_IMAGE);
        this.setScale(Bullet.BULLET_SCALE);

        this.isDirectional = isDirectional;
        this.mobSource = null;
        this.fromOutlaw = fromOutlaw;

        this.computeDestination(activeDirections);
    }

    // method that will move/change the x position of the bullet
    @Override
    public void update(long now) {
        super.update(now);

        this.addX(this.dx);
        this.addY(this.dy);

        if (this.getX() > Game.WINDOW_MAX_WIDTH) {
            this.setVisible(false);
        }

        this.checkCollisions();
    }

    private void computeDestination(byte activeDirections) {
        if (isDirectional) {
            boolean noHorizontalDirections = !Game.isDirectionActive(activeDirections, Game.DIR_RIGHT)
                    && !Game.isDirectionActive(activeDirections, Game.DIR_LEFT);
            if (Game.isDirectionActive(activeDirections, Game.DIR_UP)) {
                this.dy = -Bullet.BULLET_SPEED;
                if (noHorizontalDirections) {
                    this.addX(OFFSET_N);
                }
            } else if (Game.isDirectionActive(activeDirections, Game.DIR_DOWN)) {
                this.dy = Bullet.BULLET_SPEED;
                if (noHorizontalDirections) {
                    this.addX(OFFSET_S);
                }
            } else {
                this.dx = Bullet.BULLET_SPEED;
            }

            if (Game.isDirectionActive(activeDirections, Game.DIR_RIGHT)) {
                this.dx = Bullet.BULLET_SPEED;
                if (Game.isDirectionActive(activeDirections, Game.DIR_UP)) {
                    this.addX(OFFSET_NE);
                } else if (Game.isDirectionActive(activeDirections, Game.DIR_DOWN)) {
                    this.addX(OFFSET_SE);
                }
            } else if (Game.isDirectionActive(activeDirections, Game.DIR_LEFT)) {
                this.dx = -Bullet.BULLET_SPEED;
                if (Game.isDirectionActive(activeDirections, Game.DIR_UP)) {
                    this.addX(OFFSET_NW);
                } else if (Game.isDirectionActive(activeDirections, Game.DIR_DOWN)) {
                    this.addX(OFFSET_SW);
                } else {
                    this.addX(OFFSET_W);
                }
            }
        } else {
            this.dx = Bullet.BULLET_SPEED;
        }
    }

    private void checkCollisions() {
        if (!this.getVisible()) {
            return;
        }

        Outlaw outlaw = this.getParent().getOutlaw();
        if (this.fromOutlaw) {
            for (Sprite sprite : this.getParent().getLevelMap().getSprites()) {
                if (!(sprite instanceof Mob)) {
                    continue;
                }
                Mob mob = (Mob) sprite;
                if (this.intersects(sprite)) {
                    mob.reduceHealth(outlaw.getStrength());
                    this.setVisible(false);
                }
            }
        } else {
            if (outlaw.intersects(this)) {
                outlaw.reduceStrength(this.mobSource.getDamage());
                this.setVisible(false);
            }
        }
    }

}