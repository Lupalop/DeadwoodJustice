package game.scenes;

import java.util.ArrayList;
import java.util.Random;

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

    public static final int MAX_NUM_FISHES = 3;

    public LevelScene(Game manager) {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT, Color.CADETBLUE);
        this.canvas = new Canvas(Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.root.getChildren().add(canvas);
        this.myShip = new Ship("Going merry", 100, 100);
        this.myShip.handleKeyPressEvent(scene);
        this.fishes = new ArrayList<Fish>();

        this.spawnFishes();
    }

    @Override
    public Scene getInnerScene() {
        return this.scene;
    }    
    
    @Override
    public void update(long currentNanoTime) {
        this.myShip.update();
        this.updateFishes();
        this.updateBullets();
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
    private void updateBullets() {
        ArrayList<Bullet> removalList = new ArrayList<Bullet>();

        // Loop through the bullet list and check whether a bullet is still
        // visible.
        for (Bullet bullet : this.myShip.getBullets()) {
            bullet.update();
            if (!bullet.isVisible()) {
                removalList.add(bullet);
            }
        }
        
        // It is unsafe to modify a collection while iterating over it,
        // so remove them once we're done with the loop.
        this.myShip.getBullets().removeAll(removalList);
    }

    // method that will move the fishes
    private void updateFishes() {
        ArrayList<Fish> removalList = new ArrayList<Fish>();
        
        for (Fish fish : this.fishes) {
            fish.update(myShip);
            if (!fish.isAlive()) {
                removalList.add(fish);
            }
        }
        
        // It is unsafe to modify a collection while iterating over it,
        // so remove them once we're done with the loop.
        this.fishes.removeAll(removalList);
    }

    // method that will spawn/instantiate three fishes at a random x,y location
    private void spawnFishes() {
        Random r = new Random();
        for (int i = 0; i < MAX_NUM_FISHES; i++) {
            int x = r.nextInt(Game.WINDOW_WIDTH / 2, Game.WINDOW_WIDTH - Fish.FISH_WIDTH);
            int y = r.nextInt(Game.WINDOW_HEIGHT);

            Fish fish = new Fish(x, y);
            this.fishes.add(fish);
        }
    }

}
