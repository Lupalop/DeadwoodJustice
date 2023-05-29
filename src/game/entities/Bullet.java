package game.entities;

import game.Game;
import game.entities.mobs.Mob;
import game.entities.props.Prop;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class Bullet extends LevelSprite {

    public static final Image BULLET_IMAGE = new Image(
            Game.getAsset("bullet.png"),
            Bullet.BULLET_WIDTH, Bullet.BULLET_WIDTH, false, false);
    public static final String SFX_SHOOT = "sfx_shoot.wav";

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

    private static final int OUTLAW_OFFSET_Y = -10;

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

        if (this.fromOutlaw) {
            this.addY(OUTLAW_OFFSET_Y);
        }

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

        // Props can consume bullets if either mobs don't ignore prop collision
        // or if we're in a higher difficulty.
        boolean propsConsumeBullets = (Game.FLAG_CHECK_PROP_COLLIDERS
                || getParent().getDifficulty() >= LevelScene.DIFFICULTY_MEDIUM);
        boolean bulletCaught = false;
        Outlaw outlaw = this.getParent().getOutlaw();

        for (Sprite sprite : this.getParent().getLevelMap().getSprites()) {
            if (this.fromOutlaw && sprite instanceof Mob) {
                Mob mob = (Mob) sprite;
                if (mob.isAlive() && this.intersects(sprite)) {
                    mob.reduceHealth(outlaw.getStrength());
                    bulletCaught = true;
                    break;
                }
            }
            if (sprite instanceof Prop && propsConsumeBullets) {
                if (this.intersects(sprite)) {
                    bulletCaught = true;
                    break;
                }
            }
        }

        if (!bulletCaught && !this.fromOutlaw && outlaw.intersects(this)) {
            outlaw.reduceStrength(this.mobSource.getDamage());
            bulletCaught = true;
        }

        if (bulletCaught) {
            this.setVisible(false);
        }
    }

}