package game.scenes;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.entities.Bullet;
import game.entities.Fish;
import game.entities.Ship;
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

    private Ship myShip;
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

    private static final int SHIP_INITIAL_X = 100;
    
    public LevelScene(Game manager) {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT, Color.CADETBLUE);
        this.canvas = new Canvas(Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);
        this.root.getChildren().add(canvas);
        Random rand = new Random();
        this.myShip = new Ship(
                "Going merry",
                SHIP_INITIAL_X,
                rand.nextInt(
                        (int) Ship.SHIP_IMAGE.getHeight(),
                        Game.WINDOW_HEIGHT - (int) Ship.SHIP_IMAGE.getHeight()));
        this.myShip.handleKeyPressEvent(scene);
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
        this.myShip.update(currentNanoTime);
        this.updateFishes(currentNanoTime);
        this.updateBullets(currentNanoTime);
    }

    @Override
    public void draw(long currentNanoTime) {
        this.gc.clearRect(0, 0, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);

        this.myShip.draw(this.gc);
        this.drawFishes();
        this.drawBullets();
    }

    // method that will render/draw the fishes to the canvas
    private void drawFishes() {
        for (Fish fish : this.fishes) {
            fish.draw(this.gc);
        }
    }

    // method that will render/draw the bullets to the canvas
    private void drawBullets() {
        for (Bullet bullet : this.myShip.getBullets())
        {
            bullet.draw(this.gc);
        }
    }

    // method that will move the bullets shot by a ship
    private void updateBullets(long currentNanoTime) {
        ArrayList<Bullet> removalList = new ArrayList<Bullet>();

        // Loop through the bullet list and check whether a bullet is still
        // visible.
        for (Bullet bullet : this.myShip.getBullets()) {
            bullet.update(currentNanoTime);
            if (!bullet.getVisible()) {
                removalList.add(bullet);
            }
        }
        
        // It is unsafe to modify a collection while iterating over it,
        // so remove them once we're done with the loop.
        this.myShip.getBullets().removeAll(removalList);
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
            fish.update(currentNanoTime, myShip, isMaxSpeed);
            if (!fish.isAlive()) {
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
            int x = r.nextInt(Game.WINDOW_WIDTH / 2, Game.WINDOW_WIDTH - Fish.FISH_WIDTH);
            int y = r.nextInt(Game.WINDOW_HEIGHT - Fish.FISH_WIDTH);

            Fish fish = new Fish(x, y);
            this.fishes.add(fish);
        }
    }

}
