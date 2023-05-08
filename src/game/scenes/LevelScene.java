package game.scenes;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.entities.Bullet;
import game.entities.Fish;
import game.entities.Outlaw;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class LevelScene implements GameScene {

    private Scene scene;
    private Group root;
    private Canvas canvas;
    private GraphicsContext gc;

    private Outlaw outlaw;
    private ArrayList<Fish> fishes;
    
    private long spawnTime;
    private long maxSpeedTime;
    private long maxSpeedEndTime;
    private boolean isMaxSpeed;

    public static final int FISH_COUNT_AT_SPAWN = 7;
    public static final int FISH_COUNT_PER_INTERVAL = 3;

    private static final long FISH_SPAWN_INTERVAL =
            TimeUnit.SECONDS.toNanos(5);
    private static final long FISH_MAX_SPEED_INTERVAL =
            TimeUnit.SECONDS.toNanos(15);
    private static final long FISH_MAX_SPEED_END_INTERVAL =
            TimeUnit.SECONDS.toNanos(3);

    private static final int OUTLAW_INITIAL_X = 100;
    
    public LevelScene(Game manager) {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT, Color.valueOf("eeca84"));
        this.canvas = new Canvas(Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);
        this.root.getChildren().add(canvas);
        Random rand = new Random();
        this.outlaw = new Outlaw(
                "Going merry",
                OUTLAW_INITIAL_X, 0);
        this.outlaw.setY(rand.nextInt(
                (int) outlaw.getBounds().getHeight(),
                Game.WINDOW_HEIGHT - (int) outlaw.getBounds().getHeight()));
        this.outlaw.handleKeyPressEvent(scene);
        this.fishes = new ArrayList<Fish>();
        this.spawnTime = System.nanoTime();
        this.maxSpeedTime = System.nanoTime();
        this.maxSpeedEndTime = -1;
        this.isMaxSpeed = false;

        this.spawnFishes(FISH_COUNT_AT_SPAWN);
    }

    @Override
    public Scene getInnerScene() {
        return this.scene;
    }    
    
    @Override
    public void update(long currentNanoTime) {
        this.outlaw.update(currentNanoTime);
        this.updateFishes(currentNanoTime);
        this.updateBullets(currentNanoTime);
    }

    @Override
    public void draw(long currentNanoTime) {
        this.gc.clearRect(0, 0, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);

        this.outlaw.draw(this.gc);
        this.drawFishes();
        this.drawBullets();
    }

    // method that will render/draw the fishes to the canvas
    private void drawFishes() {
        boolean spawnAreaDrawn = false;
        for (Fish fish : this.fishes) {
            fish.draw(this.gc);
            if (Game.DEBUG_MODE && !spawnAreaDrawn) {
                int fishWidth = (int) fish.getBounds().getWidth();
                int fishHeight = (int) fish.getBounds().getHeight();
                gc.strokeRect(
                        (Game.WINDOW_WIDTH / 2) + fishWidth,
                        fishHeight,
                        Game.WINDOW_WIDTH / 2 - fishWidth * 2,
                        Game.WINDOW_HEIGHT - fishHeight * 2);
                spawnAreaDrawn = true;
            }
        }
    }

    // method that will render/draw the bullets to the canvas
    private void drawBullets() {
        for (Bullet bullet : this.outlaw.getBullets())
        {
            bullet.draw(this.gc);
        }

    }

    // method that will move the bullets shot by the outlaw
    private void updateBullets(long currentNanoTime) {
        ArrayList<Bullet> removalList = new ArrayList<Bullet>();

        // Loop through the bullet list and check whether a bullet is still
        // visible.
        for (Bullet bullet : this.outlaw.getBullets()) {
            bullet.update(currentNanoTime);
            if (!bullet.getVisible()) {
                removalList.add(bullet);
            }
        }
        
        // It is unsafe to modify a collection while iterating over it,
        // so remove them once we're done with the loop.
        this.outlaw.getBullets().removeAll(removalList);
    }

    private void updateFishes(long currentNanoTime) {
        // Spawn fish every 3 seconds.
        long deltaTime = (currentNanoTime - spawnTime);
        if (deltaTime >= FISH_SPAWN_INTERVAL) {
            this.spawnFishes(FISH_COUNT_PER_INTERVAL);
            this.spawnTime = currentNanoTime;
        }
        // Speed up fish movement every 15 seconds.
        deltaTime = (currentNanoTime - maxSpeedTime);
        if (deltaTime >= FISH_MAX_SPEED_INTERVAL) {
            this.isMaxSpeed = true;
            this.maxSpeedTime = currentNanoTime + FISH_MAX_SPEED_END_INTERVAL;
            this.maxSpeedEndTime = currentNanoTime;
        }
        // Reset back to normal speed after 3 seconds if we've
        // sped up fish movement.
        if (maxSpeedEndTime != -1) {
            deltaTime = (currentNanoTime - maxSpeedEndTime);
            if (deltaTime >= FISH_MAX_SPEED_END_INTERVAL) {
                this.isMaxSpeed = false;
                this.maxSpeedEndTime = -1;
            }
        }
        
        // Update fish movement.
        ArrayList<Fish> removalList = new ArrayList<Fish>();
        
        for (Fish fish : this.fishes) {
            fish.update(currentNanoTime, outlaw, fishes, isMaxSpeed);
            if (!fish.isAlive() && !fish.isDying()) {
                removalList.add(fish);
            }
        }
        
        // It is unsafe to modify a collection while iterating over it,
        // so remove them once we're done with the loop.
        this.fishes.removeAll(removalList);
    }

    // method that will spawn/instantiate three fishes at a random x,y location
    private void spawnFishes(int fishCount) {
        Random r = new Random();
        for (int i = 0; i < fishCount; i++) {
            Fish fish = new Fish(0, 0);
            
            int fishWidth = (int) fish.getBounds().getWidth();
            int fishHeight = (int) fish.getBounds().getHeight();

            fish.setX(r.nextInt(
                    Game.WINDOW_WIDTH / 2,
                    Game.WINDOW_WIDTH - fishWidth));
            fish.setY(r.nextInt(
                    fishHeight,
                    Game.WINDOW_HEIGHT - fishHeight));

            int index = 0;
            for (Fish otherFish : this.fishes) {
                if (fish.getY() > otherFish.getY()) {
                    index++;
                } else {
                    break;
                }
            }

            this.fishes.add(index, fish);
        }
    }

}
