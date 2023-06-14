package game.entities;

import java.util.Arrays;

import game.Game;
import game.UIUtils;
import game.entities.effects.Effect;
import game.entities.effects.ImmortalityEffect;
import game.entities.effects.SmokeEffect;
import game.scenes.LevelScene;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Outlaw extends Entity {

    private static final Image FRAMESET_W = new Image(
            Game.getAsset("player_sheet_w.png"));
    private static final Image FRAMESET_SW = new Image(
            Game.getAsset("player_sheet_sw.png"));
    private static final Image FRAMESET_N = new Image(
            Game.getAsset("player_sheet_n.png"));
    private static final Image FRAMESET_NW = new Image(
            Game.getAsset("player_sheet_nw.png"));
    private static final Image FRAMESET_S = new Image(
            Game.getAsset("player_sheet_s.png"));

    private static final int FRAMESET_ROWS = 5;
    private static final int FRAMESET_COLUMNS = 14;
    private static final int[] FRAMESET_OFFSET =
            new int[] { 15, 17, 9, 3 };
    private static final FrameRange FRAME_RANGE =
            new FrameRange(14, 21,
                    56, 58,
                    56, 58,
                    28, 33,
                    56, 69,
                    0, 5);

    private static final int BASE_SPEED = 10;

    private static final String SFX_DEAD_OUTLAW = "sfx_dead_outlaw.wav";

    private String name;
    private int strength;

    private boolean alive;
    private boolean dying;
    private boolean immortal;
    private boolean blockedFromShooting;

    private byte activeDirections;

    private Effect powerupEffect;
    private Effect immortalityEffect;

    private boolean[] passability;
    private FrameRange frameRange;

    public Outlaw(String name, int x, int y, LevelScene parent) {
        super(x, y, parent);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setFrameRange(FRAME_RANGE);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.frameRange.playIdle(this);

        this.name = name;
        this.strength = Game.DEBUG_MODE
                ? 1
                : Game.RNG.nextInt(151) + 100;

        this.alive = true;
        this.dying = false;
        this.immortal = false;
        this.blockedFromShooting = false;

        this.activeDirections = 0;

        this.powerupEffect = null;
        this.immortalityEffect = null;
        this.passability = new boolean[4];
        Arrays.fill(passability, true);
    }

    @Override
    public void update(long now) {
        super.update(now);

        if (immortalityEffect != null) {
            immortalityEffect.update(now);
        }

        if (powerupEffect != null) {
            powerupEffect.update(now);
        }

        if (this.dying) {
            if (this.isFrameSequenceDone()) {
                this.dying = false;
                this.getParent().markLevelDone();
                return;
            }
        }

        if (!this.isAlive()) {
            return;
        }

        passability = this.getParent().getLevelMap().getPassability(this);

        if (this.getBounds().getMinX() + dx >= 0
                && Game.isDirectionActive(this.activeDirections, Game.DIR_LEFT)
                && passability[SIDE_LEFT]) {
            this.dx = -BASE_SPEED;
        } else if (this.getBounds().getMinX() + this.dx <= Game.WINDOW_MAX_WIDTH - this.getBounds().getWidth()
                && Game.isDirectionActive(this.activeDirections, Game.DIR_RIGHT)
                && passability[SIDE_RIGHT]) {
            this.dx = BASE_SPEED;
        } else {
            this.dx = 0;
        }

        if (this.getBounds().getMinY() + dy >= 0
                && Game.isDirectionActive(this.activeDirections, Game.DIR_UP)
                && passability[SIDE_TOP]) {
            this.dy = -BASE_SPEED;
        } else if (this.getBounds().getMinY() + dy <= Game.WINDOW_MAX_HEIGHT - this.getBounds().getHeight()
                && Game.isDirectionActive(this.activeDirections, Game.DIR_DOWN)
                && passability[SIDE_BOTTOM]) {
            this.dy = BASE_SPEED;
        } else {
            this.dy = 0;
        }

        if (this.dx != 0 || this.dy != 0) {
            this.frameRange.playWalk(this);
        } else {
            this.frameRange.playIdle(this);
        }

        this.addX(this.dx);
        this.addY(this.dy);
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (immortalityEffect != null) {
            immortalityEffect.draw(gc);
        }
        super.draw(gc);
        if (powerupEffect != null) {
            powerupEffect.draw(gc);
        }
        if (Game.DEBUG_MODE && !hideWireframe) {
            UIUtils.drawPassability(gc, this, passability);
        }
    }

    // method called if spacebar is pressed
    public void shoot() {
        if (!this.isAlive()) {
            return;
        }

        Game.playSFX(Bullet.SFX_SHOOT, 0.3);
        Bullet bullet = new Bullet(this, getParent(), activeDirections,
                Game.FLAG_DIRECTIONAL_SHOOTING);
        this.getParent().getLevelMap().addEntity(bullet);
    }

    public void increaseStrength(int value) {
        // This must be a positive integer.
        if (value < 0) {
            return;
        }
        this.strength += value;
        this.getParent().spawnMote(this, value, Mote.TYPE_GOOD);
    }

    public void reduceStrength(int value) {
        // This must be a positive integer.
        if (value < 0 || this.immortal || !this.alive) {
            return;
        }
        // Clamp the strength value to 0.
        if (this.strength - value < 0) {
            this.strength = 0;
        } else {
            this.strength -= value;
        }

        byte moteType = Mote.TYPE_NEUTRAL;
        if (this.strength == 0) {
            moteType = Mote.TYPE_BAD;
            this.prepareDeath();
        } else {
            this.frameRange.playDamage(this);
        }

        this.getParent().spawnMote(this, value, moteType);
    }

    private void prepareDeath() {
        this.alive = false;
        this.dying = true;
        this.setFrameAutoReset(false);
        this.frameRange.playDeath(this);
        Game.playSFX(SFX_DEAD_OUTLAW);
    }

    // method that will listen and handle the key press events
    public void handleKeyPressEvent(LevelScene level) {
        Scene scene = level.getInner();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (level.isLevelDone() || level.isLevelPaused()) {
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
            this.frameRange.playShoot(this,
                    Game.FLAG_DIRECTIONAL_SHOOTING ? null : FRAMESET_W);
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

    public void setImmortal(boolean immortal) {
        if (immortal) {
            this.immortalityEffect = new ImmortalityEffect(this);
            this.immortal = true;
        } else {
            this.immortalityEffect = null;
            this.immortal = false;
        }
    }

    protected void setFrameRange(FrameRange frameRange) {
        this.frameRange = frameRange;
    }

}
