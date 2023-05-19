package game.entities;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.LevelScene;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Outlaw extends Sprite implements LevelUpdatable {

    public static final Image FRAMESET_W = new Image(
            Game.getAsset("player_sheet_w.png"));
    public static final Image FRAMESET_SW = new Image(
            Game.getAsset("player_sheet_sw.png"));
    public static final Image FRAMESET_N = new Image(
            Game.getAsset("player_sheet_n.png"));
    public static final Image FRAMESET_NW = new Image(
            Game.getAsset("player_sheet_nw.png"));
    public static final Image FRAMESET_S = new Image(
            Game.getAsset("player_sheet_s.png"));

    public static final int FRAMESET_ROWS = 5;
    public static final int FRAMESET_COLUMNS = 14;
    public static final int[] FRAMESET_OFFSET =
            new int[] { 15, 17, 9, 3 };

    public static final int OUTLAW_SPEED = 10;

    private String name;
    private int strength;

    private boolean alive;
    private boolean dying;
    private boolean immortal;
    private boolean blockedFromShooting;

    private ArrayList<Bullet> bullets;
    private byte activeDirections;

    private long immortalityStartTime;
    private long immortalityEndTime;

    private Effect powerupEffect;
    private Effect immortalityEffect;

    public Outlaw(String name, int x, int y) {
        super(x, y);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 5);
        this.setBoundsOffset(FRAMESET_OFFSET);

        this.name = name;
        Random r = new Random();
        this.strength = Game.DEBUG_MODE
                ? 1
                : r.nextInt(151) + 100;

        this.alive = true;
        this.dying = false;
        this.immortal = false;
        this.blockedFromShooting = false;

        this.bullets = new ArrayList<Bullet>();
        this.activeDirections = 0;

        this.immortalityStartTime = -1;
        this.immortalityEndTime = -1;

        this.powerupEffect = null;
        this.immortalityEffect = null;
    }

    // method that will get the bullets 'shot' by the outlaw
    public ArrayList<Bullet> getBullets() {
        return this.bullets;
    }

    // method called if spacebar is pressed
    public void shoot() {
        if (!this.isAlive()) {
            return;
        }

        int x = (int) (this.getBounds().getMaxX());
        int y = (int) (this.getBounds().getMinY()
            + (this.getBounds().getHeight() / 2)
            - (Bullet.BULLET_IMAGE.getHeight() / 2));

        // compute for the x and y initial position of the bullet
        Bullet bullet = new Bullet(x, y, activeDirections, Game.FLAG_DIRECTIONAL_SHOOTING);
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
        if (value < 0 || this.immortal) {
            return;
        }
        // Clamp the strength value to 0.
        if (this.strength - value < 0) {
            this.strength = 0;
        } else {
            this.strength -= value;
        }
        if (this.strength == 0) {
            this.alive = false;
            this.dying = true;
            this.setFrameAutoReset(false);
            this.setMinMaxFrame(56, 69);
        } else {
            this.playFrames(56, 58, null, TimeUnit.MILLISECONDS.toNanos(200));
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (immortalityEffect != null) {
            immortalityEffect.draw(gc);
        }

        super.draw(gc);
        for (Bullet bullet : this.getBullets()) {
            bullet.draw(gc);
        }

        if (powerupEffect != null) {
            powerupEffect.draw(gc);
        }
    }

    // method called if up/down/left/right arrow key is pressed.
    @Override
    public void update(long now, LevelScene level) {
        super.update(now);

        if (immortalityEffect != null) {
            immortalityEffect.update(now);
        }

        if (powerupEffect != null) {
            powerupEffect.update(now);
        }

        // Keep a list containing bullets to be removed.
        ArrayList<Bullet> removalList = new ArrayList<Bullet>();

        // Loop through the bullet list and remove used bullets.
        for (Bullet bullet : this.getBullets()) {
            bullet.update(now);
            if (!bullet.getVisible()) {
                removalList.add(bullet);
            }
        }

        this.getBullets().removeAll(removalList);

        if (this.dying) {
            if (this.isFrameSequenceDone()) {
                this.dying = false;
                level.markLevelDone();
                return;
            }
        }

        if (!this.isAlive()) {
            return;
        }

        if (this.getBounds().getMinX() + dx >= 0 &&
                Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT)) {
            this.dx = -OUTLAW_SPEED;
        } else if (this.getBounds().getMinX() + this.dx <= Game.WINDOW_MAX_WIDTH - this.getBounds().getWidth()
                && Game.isDirectionActive(this.activeDirections, Game.DIR_RIGHT)) {
            this.dx = OUTLAW_SPEED;
        } else {
            this.dx = 0;
        }

        if (this.getBounds().getMinY() + dy >= 0 &&
                Game.isDirectionActive(this.activeDirections, Game.DIR_UP)) {
            this.dy = -OUTLAW_SPEED;
        } else if (this.getBounds().getMinY() + dy <= Game.WINDOW_MAX_HEIGHT - this.getBounds().getHeight()
                && Game.isDirectionActive(this.activeDirections, Game.DIR_DOWN)) {
            this.dy = OUTLAW_SPEED;
        } else {
            this.dy = 0;
        }

        if (this.dx != 0 || this.dy != 0) {
            this.setMinMaxFrame(14, 21);
        } else {
            this.setMinMaxFrame(0,  5);
        }

        this.addX(this.dx);
        this.addY(this.dy);

        long deltaTime = (now - this.immortalityStartTime);
        if (deltaTime >= this.immortalityEndTime) {
            this.immortalityEffect = null;
            this.immortalityStartTime = -1;
            this.immortalityEndTime = -1;
            this.immortal = false;
        }
    }

    // method that will listen and handle the key press events
    public void handleKeyPressEvent(LevelScene level) {
        Scene scene = level.getInner();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (level.isLevelDone()) {
                    return;
                }
                KeyCode code = e.getCode();
                startMoving(code);
            }
        });

        scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                stopMoving(code);
            }
        });
    }

    // method that will move the outlaw depending on the key pressed
    private void startMoving(KeyCode keyCode) {
        if (!this.isAlive()) {
            return;
        }

        switch (keyCode) {
        case UP:
        case W:
            this.activeDirections |= Game.DIR_UP;
            break;
        case DOWN:
        case S:
            this.activeDirections |= Game.DIR_DOWN;
            break;
        case LEFT:
        case A:
            this.activeDirections |= Game.DIR_LEFT;
            if (!Game.FLAG_DIRECTIONAL_SHOOTING) {
                this.blockedFromShooting = true;
            }
            break;
        case RIGHT:
        case D:
            this.activeDirections |= Game.DIR_RIGHT;
            break;
        case SPACE:
        case ENTER:
            if (this.blockedFromShooting) {
                break;
            }
            // We can't shoot in other directions, following a limitation
            // imposed by the problem domain.
            this.blockedFromShooting = true;
            if (Game.FLAG_DIRECTIONAL_SHOOTING) {
                this.playFrames(28, 33, null, TimeUnit.MILLISECONDS.toNanos(50));
            } else {
                this.playFrames(28, 33, FRAMESET_W, TimeUnit.MILLISECONDS.toNanos(50));
            }
            this.shoot();
            break;
        default:
            break;
        }
        this.updateFrameSet();
    }

    // method that will stop the outlaw's movement; set the outlaw's DX and DY to 0
    private void stopMoving(KeyCode keyCode) {
        switch (keyCode) {
        case UP:
        case W:
            this.activeDirections &= ~Game.DIR_UP;
            break;
        case DOWN:
        case S:
            this.activeDirections &= ~Game.DIR_DOWN;
            break;
        case LEFT:
        case A:
            this.activeDirections &= ~Game.DIR_LEFT;
            this.blockedFromShooting = false;
            break;
        case RIGHT:
        case D:
            this.activeDirections &= ~Game.DIR_RIGHT;
            break;
        case SPACE:
        case ENTER:
            if (Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT) && !Game.FLAG_DIRECTIONAL_SHOOTING) {
                break;
            }
            this.blockedFromShooting = false;
            break;
        default:
            break;
        }

        this.updateFrameSet();
    }

    private void updateFrameSet() {
        this.setFlip(false, false);
        if (Game.isDirectionActive(this.activeDirections, Game.DIR_UP)) {
            if (Game.isDirectionActive(this.activeDirections, Game.DIR_RIGHT)) {
                this.setFrameSet(FRAMESET_NW);
            } else if (Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT)) {
                this.setFrameSet(FRAMESET_NW);
                this.setFlip(true, false);
            } else {
                this.setFrameSet(FRAMESET_N);
            }
        } else if (Game.isDirectionActive(this.activeDirections, Game.DIR_DOWN)) {
            if (Game.isDirectionActive(this.activeDirections, Game.DIR_RIGHT)) {
                this.setFrameSet(FRAMESET_SW);
            } else if (Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT)) {
                this.setFrameSet(FRAMESET_SW);
                this.setFlip(true, false);
            } else {
                this.setFrameSet(FRAMESET_S);
            }
        } else {
            if (Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT)) {
                this.setFlip(true, false);
            }
            this.setFrameSet(FRAMESET_W);
        }
    }

    public void applyImmortality(long endTime) {
        this.immortalityEffect = new ImmortalityEffect(this);
        this.immortal = true;
        this.immortalityStartTime = System.nanoTime();
        this.immortalityEndTime = endTime;
    }

    // TODO: move to effects manager
    public void spawnPowerupEffect() {
        this.powerupEffect = new SmokeEffect(this);
    }

    public String getName() {
        return this.name;
    }

    public int getStrength() {
        return this.strength;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public boolean isDying() {
        return this.dying;
    }

    public boolean isImmortal() {
        return this.immortal;
    }

}
