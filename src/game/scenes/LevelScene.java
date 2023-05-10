package game.scenes;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.entities.Bullet;
import game.entities.CactusMob;
import game.entities.CoffinMob;
import game.entities.CowboyMob;
import game.entities.CoyoteMob;
import game.entities.Mob;
import game.entities.Outlaw;
import game.entities.Tileset;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class LevelScene implements GameScene {

    private static final int TILEGEN_MATCH = 1;
    private static final int TILEGEN_FREQ_GRASS_OR_ROCK = 6;
    private static final int TILEGEN_FREQ_CACTUS = 50;
    private static final int TILEGEN_FREQ_PROPS = 150;
    
    private static final int TILE_SIZE = 32;
    private static final int TILES_VERTICAL =
            (Game.WINDOW_HEIGHT + 8) / TILE_SIZE;
    private static final int TILES_HORIZONTAL =
            (Game.WINDOW_WIDTH) / TILE_SIZE;
    private static final int TILES_TOTAL =
            TILES_VERTICAL * TILES_HORIZONTAL;
    
    private Scene scene;
    private Group root;
    private Canvas canvas;
    private GraphicsContext gc;

    private int[] tileLayer1;
    private int[] tileLayer2;
    private boolean generateTiles;

    private Outlaw outlaw;
    private Mob bossMob;
    private ArrayList<Mob> mobs;
    
    private long spawnTime;
    private long maxSpeedTime;
    private long maxSpeedEndTime;
    private long levelStartTime;
    private boolean isMaxSpeed;
    private boolean isLevelDone;

    public static final int MOB_COUNT_AT_SPAWN = 7;
    public static final int MOB_COUNT_PER_INTERVAL = 3;

    private static final long MOB_SPAWN_INTERVAL =
            TimeUnit.SECONDS.toNanos(5);
    private static final long MOB_MAX_SPEED_INTERVAL =
            TimeUnit.SECONDS.toNanos(15);
    private static final long MOB_MAX_SPEED_END_INTERVAL =
            TimeUnit.SECONDS.toNanos(3);

    private static final long LEVEL_BOSS_TIME =
            TimeUnit.SECONDS.toNanos(30);
    private static final long LEVEL_END_TIME =
            TimeUnit.SECONDS.toNanos(60);

    private static final int OUTLAW_INITIAL_X = 100;

    private static final Tileset TILESET_DESERT =
            new Tileset("tilemap_desert.png");
    
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
        this.mobs = new ArrayList<Mob>();
        this.levelStartTime = System.nanoTime();
        this.spawnTime = System.nanoTime();
        this.maxSpeedTime = System.nanoTime();
        this.maxSpeedEndTime = -1;
        this.isMaxSpeed = false;
        this.generateTiles = true;
        this.isLevelDone = false;

        if (Game.DEBUG_MODE) {
            scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                public void handle(KeyEvent e) {
                    KeyCode code = e.getCode();
                    if (code == KeyCode.A) {
                        generateTiles = true;
                    }
                }
            });
        }
        
        this.spawnMobs(MOB_COUNT_AT_SPAWN);
    }

    @Override
    public Scene getInnerScene() {
        return this.scene;
    }    
    
    @Override
    public void update(long currentNanoTime) {
        if (this.isLevelDone) {
            return;
        }
        this.outlaw.update(currentNanoTime);
        this.updateMobs(currentNanoTime);
        this.updateBullets(currentNanoTime);
        this.updateLevelTime(currentNanoTime);
    }

    private void updateLevelTime(long currentNanoTime) {
        long deltaTime = (currentNanoTime - levelStartTime);
        // Detect if the game should've ended by now.
        if (deltaTime >= LEVEL_END_TIME
                && (!Game.FLAG_DELAY_IF_BOSS_IS_ALIVE
                        || (this.bossMob != null
                        && !this.bossMob.isAlive()
                        && !this.bossMob.isDying()))) {
            this.isLevelDone = true;
            System.out.println("Game Done.");
        // Spawn the boss mob if the time is right.
        } else if (deltaTime >= LEVEL_BOSS_TIME && this.bossMob == null) {
            this.bossMob = new CowboyMob(Game.WINDOW_WIDTH, (Game.WINDOW_HEIGHT / 2));
            this.bossMob.addY((int) -this.bossMob.getBounds().getHeight() / 2);
            this.bossMob.addX((int) -this.bossMob.getBounds().getWidth());
            this.mobs.add(bossMob);
            System.out.println("boss spawned");
        }
    }

    @Override
    public void draw(long currentNanoTime) {
        this.gc.clearRect(0, 0, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);

        this.drawTiles();
        
        this.outlaw.draw(this.gc);
        this.drawMobs();
        this.drawBullets();
    }

    private void drawTiles() {
        // Initialize tile layers.
        if (this.generateTiles) {
            tileLayer1 = new int[TILES_TOTAL];
            tileLayer2 = new int[TILES_TOTAL];
        }
        // Holds the current tile ID.
        int tileId = 0;
        // Iterate through each tile.
        for (int i = 0; i < TILES_VERTICAL; i++) {
            for (int j = 0; j < TILES_HORIZONTAL; j++) {
                // The tile generator.
                if (this.generateTiles) {
                    Random rand = new Random();
                    // Generate: land tile.
                    tileLayer1[tileId] = rand.nextInt(0, 4);
                    // Generate: grass.
                    if (rand.nextInt(TILEGEN_FREQ_GRASS_OR_ROCK) == TILEGEN_MATCH) {
                        tileLayer2[tileId] = rand.nextInt(6, 8);
                    // Generate: rocks.
                    } else if (rand.nextInt(TILEGEN_FREQ_GRASS_OR_ROCK) == TILEGEN_MATCH) {
                        tileLayer2[tileId] = rand.nextInt(8, 10);
                    // Generate: cactus.
                    } else if (rand.nextInt(TILEGEN_FREQ_CACTUS) == TILEGEN_MATCH) {
                        tileLayer2[tileId] = rand.nextInt(4, 6);
                    // Generate: sign.
                    } else if (rand.nextInt(TILEGEN_FREQ_PROPS) == TILEGEN_MATCH) {
                        tileLayer2[tileId] = 10;
                    // Generate: fossil.
                    } else if (rand.nextInt(TILEGEN_FREQ_PROPS) == TILEGEN_MATCH) {
                        tileLayer2[tileId] = 11;
                    }
                }
                // Draw from the desert tileset.
                TILESET_DESERT.draw(
                        gc, TILE_SIZE * j, TILE_SIZE * i, tileLayer1[tileId]);
                TILESET_DESERT.draw(
                        gc, TILE_SIZE * j, TILE_SIZE * i, tileLayer2[tileId]);
                tileId++;
            }
        }
        // Mark as done with tile generation.
        if (this.generateTiles) {
            this.generateTiles = false;
        }
    }
    
    // method that will render/draw the mobs to the canvas
    private void drawMobs() {
        boolean spawnAreaDrawn = false;
        for (Mob mob : this.mobs) {
            mob.draw(this.gc);
            if (Game.DEBUG_MODE && !spawnAreaDrawn) {
                int mobWidth = (int) mob.getBounds().getWidth();
                int mobHeight = (int) mob.getBounds().getHeight();
                gc.strokeRect(
                        (Game.WINDOW_WIDTH / 2) + mobWidth,
                        mobHeight,
                        Game.WINDOW_WIDTH / 2 - mobWidth * 2,
                        Game.WINDOW_HEIGHT - mobHeight * 2);
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

    private void updateMobs(long currentNanoTime) {
        // Spawn mobs every 3 seconds.
        long deltaTime = (currentNanoTime - spawnTime);
        if (deltaTime >= MOB_SPAWN_INTERVAL) {
            this.spawnMobs(MOB_COUNT_PER_INTERVAL);
            this.spawnTime = currentNanoTime;
        }
        // Speed up mob movement every 15 seconds.
        deltaTime = (currentNanoTime - maxSpeedTime);
        if (deltaTime >= MOB_MAX_SPEED_INTERVAL) {
            this.isMaxSpeed = true;
            this.maxSpeedTime = currentNanoTime + MOB_MAX_SPEED_END_INTERVAL;
            this.maxSpeedEndTime = currentNanoTime;
        }
        // Reset back to normal speed after 3 seconds if we've
        // sped up mob movement.
        if (maxSpeedEndTime != -1) {
            deltaTime = (currentNanoTime - maxSpeedEndTime);
            if (deltaTime >= MOB_MAX_SPEED_END_INTERVAL) {
                this.isMaxSpeed = false;
                this.maxSpeedEndTime = -1;
            }
        }
        
        // Update mob movement.
        ArrayList<Mob> removalList = new ArrayList<Mob>();
        
        for (Mob mob : this.mobs) {
            mob.update(currentNanoTime, outlaw, mobs, isMaxSpeed);
            if (!mob.isAlive() && !mob.isDying()) {
                removalList.add(mob);
            }
        }
        
        // It is unsafe to modify a collection while iterating over it,
        // so remove them once we're done with the loop.
        this.mobs.removeAll(removalList);
    }

    private Mob randomizeMob() {
        Random rand = new Random();
        switch (rand.nextInt(0, Mob.TOTAL_MOBS)) {
        case 0:
            return new CactusMob(0, 0);
        case 1:
            return new CoyoteMob(0, 0);
        case 2:
            return new CoffinMob(0, 0);
        default:
            // This should not be reached.
            return null;
        }
    }
    
    // method that will spawn/instantiate three mobs at a random x,y location
    private void spawnMobs(int mobCount) {
        Random r = new Random();
        for (int i = 0; i < mobCount; i++) {
            Mob mob = randomizeMob();
            
            int mobWidth = (int) mob.getBounds().getWidth();
            int mobHeight = (int) mob.getBounds().getHeight();

            mob.setX(r.nextInt(
                    Game.WINDOW_WIDTH / 2,
                    Game.WINDOW_WIDTH - mobWidth));
            mob.setY(r.nextInt(
                    mobHeight,
                    Game.WINDOW_HEIGHT - mobHeight * 2));

            int index = 0;
            for (Mob otherMob : this.mobs) {
                if (mob.getY() > otherMob.getY()) {
                    index++;
                } else {
                    break;
                }
            }

            this.mobs.add(index, mob);
        }
    }

}
