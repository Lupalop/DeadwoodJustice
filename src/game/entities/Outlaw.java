package game.entities;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.scenes.LevelScene;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Outlaw extends Sprite implements LevelUpdatable {

    private String name;
    private int strength;
    private boolean isShootBlocked;
    private boolean isAlive;
    private boolean isDying;
    private boolean isImmortal;
    private ArrayList<Bullet> bullets;
    private byte activeDirections;
    
    private long immortalityStartTime;
    private long immortalityEndTime;

    public final static Image FRAMESET_W = new Image(
            Game.getAsset("player_sheet_w.png"));
    public final static Image FRAMESET_SW = new Image(
            Game.getAsset("player_sheet_sw.png"));
    public final static Image FRAMESET_N = new Image(
            Game.getAsset("player_sheet_n.png"));
    public final static Image FRAMESET_NW = new Image(
            Game.getAsset("player_sheet_nw.png"));
    public final static Image FRAMESET_S = new Image(
            Game.getAsset("player_sheet_s.png"));

    public final static int FRAMESET_ROWS = 5;
    public final static int FRAMESET_COLUMNS = 14;
    public final static int[] FRAMESET_OFFSET = 
            new int[] { 15, 17, 9, 3 };

    public final static int OUTLAW_SPEED = 10;

    public Outlaw(String name, int x, int y) {
        super(x, y);
        
        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 5);
        this.setScale(2);
        this.setBoundsOffset(FRAMESET_OFFSET);

        this.name = name;
        Random r = new Random();
        this.strength = r.nextInt(151) + 100;
        if (Game.DEBUG_MODE) {
            this.strength = 1;
        }
        this.isShootBlocked = false;
        this.isAlive = true;
        this.isDying = false;
        this.isImmortal = false;
        this.bullets = new ArrayList<Bullet>();
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public boolean isDying() {
        return this.isDying;
    }

    public int getStrength() {
        return this.strength;
    }
    
    public String getName() {
        return this.name;
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
        Bullet bullet = new Bullet(x, y, activeDirections);
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
        if (value < 0 || this.isImmortal) {
            return;
        }
        // Clamp the strength value to 0.
        if (this.strength - value < 0) {
            this.strength = 0;
        } else {
            this.strength -= value;
        }
        if (this.strength == 0) {
            this.isAlive = false;
            this.isDying = true;
            this.setFrameAutoReset(false);
            this.setMinMaxFrame(56, 69);
        } else {
            this.playFrames(56, 58, null, TimeUnit.MILLISECONDS.toNanos(200));
        }
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        for (Bullet bullet : this.getBullets()) {
            bullet.draw(gc);
        }
    }
    
    // method called if up/down/left/right arrow key is pressed.
    public void update(long currentNanoTime, LevelScene level) {
        super.update(currentNanoTime);

        // Keep a list containing bullets to be removed.
        ArrayList<Bullet> removalList = new ArrayList<Bullet>();

        // Loop through the bullet list and remove used bullets.
        for (Bullet bullet : this.getBullets()) {
            bullet.update(currentNanoTime);
            if (!bullet.getVisible()) {
                removalList.add(bullet);
            }
        }
        
        this.getBullets().removeAll(removalList);
        
        if (this.isDying) {
            if (this.isFrameSequenceDone()) {
                this.isDying = false;
                // TODO: trigger game over screen.
                System.out.println("Game over.");
            }
        }

        if (!this.isAlive()) {
            return;
        }
        
        if (this.getBounds().getMinX() + dx >= 0 &&
                Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_LEFT)) {
            this.dx = -OUTLAW_SPEED;
        } else if (this.getBounds().getMinX() + this.dx <= Game.WINDOW_WIDTH - this.getBounds().getWidth()
                && Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_RIGHT)) {
            this.dx = OUTLAW_SPEED;
        } else {
            this.dx = 0;
        }

        if (this.getBounds().getMinY() + dy >= 0 &&
                Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_UP)) {
            this.dy = -OUTLAW_SPEED;
        } else if (this.getBounds().getMinY() + dy <= Game.WINDOW_HEIGHT - this.getBounds().getHeight()
                && Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_DOWN)) {
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
        
        long deltaTime = (currentNanoTime - this.immortalityStartTime);
        if (deltaTime >= this.immortalityEndTime) {
            this.immortalityStartTime = -1;
            this.immortalityEndTime = -1;
            this.isImmortal = false;
        }
    }

    // method that will listen and handle the key press events
    public void handleKeyPressEvent(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                startMoving(code);
            }
        });

        scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
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
            this.activeDirections |= Game.KEY_DIR_UP;
            break;
        case DOWN:
        case S:
            this.activeDirections |= Game.KEY_DIR_DOWN;
            break;
        case LEFT:
        case A:
            this.activeDirections |= Game.KEY_DIR_LEFT;
            if (!Game.FLAG_DIRECTIONAL_SHOOTING) {
                this.isShootBlocked = true;
            }
            break;
        case RIGHT:
        case D:
            this.activeDirections |= Game.KEY_DIR_RIGHT;
            break;
        case SPACE:
        case ENTER:
            if (this.isShootBlocked) {
                break;
            }
            // We can't shoot in other directions, following a limitation
            // imposed by the problem domain.
            this.isShootBlocked = true;
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
            this.activeDirections &= ~Game.KEY_DIR_UP;
            break;
        case DOWN:
        case S:
            this.activeDirections &= ~Game.KEY_DIR_DOWN;
            break;
        case LEFT:
        case A:
            this.activeDirections &= ~Game.KEY_DIR_LEFT;
            this.isShootBlocked = false;
            break;
        case RIGHT:
        case D:
            this.activeDirections &= ~Game.KEY_DIR_RIGHT;
            break;
        case SPACE:
        case ENTER:
            if (Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_LEFT) && !Game.FLAG_DIRECTIONAL_SHOOTING) {
                break;
            }
            this.isShootBlocked = false;
        default:
            break;
        }
        this.updateFrameSet();
    }

    private void updateFrameSet() {
        this.flipHorizontal(false);
        if (Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_UP)) {
            if (Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_RIGHT)) {
                this.setFrameSet(FRAMESET_NW);
            } else if (Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_LEFT)) {
                this.setFrameSet(FRAMESET_NW);
                this.flipHorizontal(true);
            } else {
                this.setFrameSet(FRAMESET_N);
            }
        } else if (Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_DOWN)) {
            if (Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_RIGHT)) {
                this.setFrameSet(FRAMESET_SW);
            } else if (Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_LEFT)) {
                this.setFrameSet(FRAMESET_SW);
                this.flipHorizontal(true);
            } else {
                this.setFrameSet(FRAMESET_S);
            }
        } else {
            if (Game.isDirectionActive(this.activeDirections, Game.KEY_DIR_LEFT)) {
                this.flipHorizontal(true);
            }
            this.setFrameSet(FRAMESET_W);
        }
    }

    public void triggerImmortality(long endTime) {
        this.isImmortal = true;
        this.immortalityStartTime = System.nanoTime();
        this.immortalityEndTime = endTime;
    }
    
    public boolean isImmortal() {
        return this.isImmortal;
    }
    
}
