package game.scenes;

import java.util.ArrayList;
import java.util.Random;

import game.GameManager;
import game.entities.Bullet;
import game.entities.Fish;
import game.entities.Ship;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class LevelScene implements GameScene {

    private Scene scene;
    private Group root;
    private Canvas canvas;
    private GraphicsContext gc;

    private Ship myShip;
    private ArrayList<Fish> fishes;

    public static final int MAX_NUM_FISHES = 3;

    public LevelScene(GameManager manager) {
        this.root = new Group();
        this.scene = new Scene(root, GameManager.WINDOW_WIDTH,
                GameManager.WINDOW_HEIGHT, Color.CADETBLUE);
        this.canvas = new Canvas(GameManager.WINDOW_WIDTH,
                GameManager.WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.root.getChildren().add(canvas);
        this.myShip = new Ship("Going merry", 100, 100);
        this.fishes = new ArrayList<Fish>();

        this.spawnFishes();
        this.handleKeyPressEvent();
    }

    @Override
    public Scene getInnerScene() {
        return this.scene;
    }    
    
    @Override
    public void update(long currentNanoTime) {
        this.myShip.move();
        /*
         * TODO: Call the moveBullets and moveFishes methods
         */
    }

    @Override
    public void draw(long currentNanoTime) {
        this.gc.clearRect(0, 0, GameManager.WINDOW_WIDTH,
                GameManager.WINDOW_HEIGHT);

        // render the ship
        this.myShip.draw(this.gc);

        /*
         * TODO: Call the renderFishes and renderBullets methods
         */
    }

    // method that will render/draw the fishes to the canvas
    private void drawFishes() {
        for (Fish f : this.fishes) {
            f.draw(this.gc);
        }
    }

    // method that will render/draw the bullets to the canvas
    private void drawBullets() {
        /*
         * TODO: Loop through the bullets arraylist of myShip and render each
         * bullet to the canvas
         */
    }

    // method that will spawn/instantiate three fishes at a random x,y location
    private void spawnFishes() {
        Random r = new Random();
        for (int i = 0; i < MAX_NUM_FISHES; i++) {
            int x = r.nextInt(GameManager.WINDOW_WIDTH);
            int y = r.nextInt(GameManager.WINDOW_HEIGHT);
            /*
             * TODO: Add a new object Fish to the fishes arraylist
             */
        }

    }

    // method that will move the bullets shot by a ship
    private void moveBullets() {
        // create a local arraylist of Bullets for the bullets 'shot' by the
        // ship
        ArrayList<Bullet> bList = this.myShip.getBullets();

        // Loop through the bullet list and check whether a bullet is still
        // visible.
        for (int i = 0; i < bList.size(); i++) {
            Bullet b = bList.get(i);
            /*
             * TODO: If a bullet is visible, move the bullet, else, remove the
             * bullet from the bullet array list.
             */
        }
    }

    // method that will move the fishes
    private void moveFishes() {
        // Loop through the fishes arraylist
        for (int i = 0; i < this.fishes.size(); i++) {
            Fish f = this.fishes.get(i);
            /*
             * TODO: *If a fish is alive, move the fish. Else, remove the fish
             * from the fishes arraylist.
             */
        }
    }

    // method that will listen and handle the key press events
    private void handleKeyPressEvent() {
        this.scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                moveMyShip(code);
            }
        });

        this.scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                stopMyShip(code);
            }
        });
    }

    // method that will move the ship depending on the key pressed
    private void moveMyShip(KeyCode ke) {
        if (ke == KeyCode.UP)
            this.myShip.setDY(-3);

        if (ke == KeyCode.LEFT)
            this.myShip.setDX(-3);

        if (ke == KeyCode.DOWN)
            this.myShip.setDY(3);

        if (ke == KeyCode.RIGHT)
            this.myShip.setDX(3);

        if (ke == KeyCode.SPACE)
            this.myShip.shoot();

        System.out.println(ke + " key pressed.");
    }

    // method that will stop the ship's movement; set the ship's DX and DY to 0
    private void stopMyShip(KeyCode ke) {
        this.myShip.setDX(0);
        this.myShip.setDY(0);
    }

}
