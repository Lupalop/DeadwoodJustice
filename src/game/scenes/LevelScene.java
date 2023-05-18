package game.scenes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.entities.CactusMob;
import game.entities.CoffinMob;
import game.entities.CowboyMob;
import game.entities.CoyoteMob;
import game.entities.HayPowerup;
import game.entities.LampPowerup;
import game.entities.LevelUpdatable;
import game.entities.Mob;
import game.entities.Outlaw;
import game.entities.Powerup;
import game.entities.Prop;
import game.entities.Sprite;
import game.entities.StatusHUD;
import game.entities.Tileset;
import game.entities.WheelPowerup;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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
    public Group getRoot() {
        return root;
    }

    private Canvas canvas;
    private GraphicsContext gc;

    private int[] tileLayer1;
    private int[] tileLayer2;
    private boolean generateTiles;

    private Outlaw outlaw;
    private Mob bossMob;
    private StatusHUD statusHud;
    private ArrayList<Sprite> sprites;

    private int mobKillCount;
    private int powerupsCount[];
    
    private long levelTimeLeft;
    private long mobSpawnTime;
    private long powerupSpawnTime;
    private long maxSpeedTime;
    private long maxSpeedEndTime;
    private long levelStartTime;
    private long slowSpeedTime;
    private long slowSpeedEndTime;
    private boolean isMaxSpeed;
    private boolean isSlowSpeed;
    private boolean isLevelDone;

    public static final int MOB_COUNT_AT_SPAWN = 7;
    public static final int MOB_COUNT_PER_INTERVAL = 3;

    private static final long MOB_SPAWN_INTERVAL =
            TimeUnit.SECONDS.toNanos(5);
    private static final long MOB_MAX_SPEED_INTERVAL =
            TimeUnit.SECONDS.toNanos(15);
    private static final long MOB_MAX_SPEED_END_INTERVAL =
            TimeUnit.SECONDS.toNanos(3);

    private static final long POWERUP_SPAWN_INTERVAL =
            TimeUnit.SECONDS.toNanos(10);
    
    private static final long LEVEL_BOSS_TIME =
            TimeUnit.SECONDS.toNanos(30);
    private static final long LEVEL_END_TIME =
            TimeUnit.SECONDS.toNanos(60);

    private static final int OUTLAW_INITIAL_X = 100;

    private static final Tileset TILESET_DESERT =
            new Tileset("tilemap_desert.png", 3, 4);
    
    public LevelScene() {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT, Color.valueOf("eeca84"));
        this.canvas = new Canvas(Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);
        this.root.getChildren().add(canvas);
        this.sprites = new ArrayList<Sprite>();
        Random rand = new Random();
        this.outlaw = new Outlaw(
                "Going merry",
                OUTLAW_INITIAL_X, 0);
        this.getOutlaw().setY(rand.nextInt(
                (int) getOutlaw().getBounds().getHeight(),
                Game.WINDOW_HEIGHT - (int) getOutlaw().getBounds().getHeight()));
        this.sprites.add(getOutlaw());
        this.getOutlaw().handleKeyPressEvent(this);
        this.statusHud = new StatusHUD(this);
        this.levelStartTime = System.nanoTime();
        this.mobSpawnTime = System.nanoTime();
        this.powerupSpawnTime = System.nanoTime();
        this.maxSpeedTime = System.nanoTime();
        this.maxSpeedEndTime = -1;
        this.slowSpeedTime = -1;
        this.slowSpeedEndTime = -1;
        this.isMaxSpeed = false;
        this.isSlowSpeed = false;
        this.generateTiles = true;
        this.isLevelDone = false;
        this.mobKillCount = 0;
        this.powerupsCount = new int[Powerup.TOTAL_POWERUPS];

        if (Game.DEBUG_MODE) {
            scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                public void handle(KeyEvent e) {
                    KeyCode code = e.getCode();
                    if (code == KeyCode.F1) {
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
        this.updateSprites(currentNanoTime);
        this.updateLevelTime(currentNanoTime);
        this.statusHud.update(currentNanoTime, this);
    }

    private void updateLevelTime(long currentNanoTime) {
        long deltaTime = (currentNanoTime - levelStartTime);
        // Detect if the game should've ended by now.
        if (deltaTime >= LEVEL_END_TIME
                && (!Game.FLAG_DELAY_IF_BOSS_IS_ALIVE
                        || (this.bossMob != null
                        && !this.bossMob.isAlive()
                        && !this.bossMob.isDying()))) {
            this.markLevelDone();
        // Spawn the boss mob if the time is right.
        } else if (deltaTime >= LEVEL_BOSS_TIME && this.bossMob == null) {
            this.bossMob = new CowboyMob(Game.WINDOW_WIDTH, (Game.WINDOW_HEIGHT / 2));
            this.bossMob.addY((int) -this.bossMob.getBounds().getHeight() / 2);
            this.bossMob.addX((int) -this.bossMob.getBounds().getWidth());
            this.sprites.add(bossMob);
            System.out.println("boss spawned");
        }

        this.levelTimeLeft = TimeUnit.NANOSECONDS.toSeconds(LEVEL_END_TIME - deltaTime);
        
        // Spawn mobs every 3 seconds.
        deltaTime = (currentNanoTime - mobSpawnTime);
        if (deltaTime >= MOB_SPAWN_INTERVAL) {
            this.spawnMobs(MOB_COUNT_PER_INTERVAL);
            this.mobSpawnTime = currentNanoTime;
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

        // Slow down after provided timeout.
        deltaTime = (currentNanoTime - slowSpeedTime);
        if (deltaTime >= slowSpeedEndTime) {
            this.isSlowSpeed = false;
            this.slowSpeedTime = -1;
            this.slowSpeedEndTime = -1;
        }

        // Spawn power-ups every 10 seconds.
        deltaTime = (currentNanoTime - powerupSpawnTime);
        if (deltaTime >= POWERUP_SPAWN_INTERVAL) {
            this.spawnPowerups();
            this.powerupSpawnTime = currentNanoTime;
        }
    }

    @Override
    public void draw(long currentNanoTime) {
        this.gc.clearRect(0, 0, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);

        this.drawTiles();
        this.drawSprites();
        this.statusHud.draw(gc);
    }

    private void drawTiles() {
        // Initialize tile layers.
        if (this.generateTiles) {
            tileLayer1 = new int[TILES_TOTAL];
            tileLayer2 = new int[TILES_TOTAL];

            Random rand = new Random();
            for (int i = 0; i < 3; i++) {
                Prop tree = new Prop(
                        rand.nextInt(1, 8) * rand.nextInt(1, 3) * 60,
                        rand.nextInt(2, 6) * rand.nextInt(1, 3) * 60,
                        "a_tree.png");
                System.out.println(tree.getX() + ":" +  tree.getY());
                sprites.add(tree);
            }

            Prop wagon = new Prop(
                    rand.nextInt(50, Game.WINDOW_WIDTH / 2),
                    rand.nextInt(3, 6) * 100,   
                    "a_coveredwagon.png");
            sprites.add(wagon);

            Prop house = new Prop(
                    rand.nextInt(Game.WINDOW_WIDTH / 2, Game.WINDOW_WIDTH),
                    -100,
                    "a_house.png");
            sprites.add(house);
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
                gc.save();
                gc.setGlobalAlpha(0.5);
                TILESET_DESERT.draw(
                        gc, TILE_SIZE * j, TILE_SIZE * i, tileLayer1[tileId]);
                gc.restore();
                if (tileLayer2[tileId] != 0) {
                    TILESET_DESERT.draw(
                            gc, TILE_SIZE * j, TILE_SIZE * i, tileLayer2[tileId]);
                }
                tileId++;
            }
        }
        // Mark as done with tile generation.
        if (this.generateTiles) {
            this.generateTiles = false;
        }
    }
    
    // method that will render/draw the mobs to the canvas
    private void drawSprites() {
        for (Sprite sprite : this.sprites) {
            sprite.draw(this.gc);
        }
    }
    
    private void updateSprites(long currentNanoTime) {
        // Keep a list containing mobs to be removed. 
        ArrayList<Sprite> removalList = new ArrayList<Sprite>();
        
        for (Sprite sprite : this.sprites) {
            if (sprite instanceof Mob) {
                Mob mob = (Mob)sprite;
                if (!mob.isAlive() && !mob.isDying()) {
                    removalList.add(mob);
                    this.mobKillCount++;
                }
            }
            
            if (sprite instanceof LevelUpdatable) {
                LevelUpdatable levelSprite = (LevelUpdatable)sprite;
                levelSprite.update(currentNanoTime, this);
            } else {
                sprite.update(currentNanoTime);
            }
        }
        
        this.sprites.removeAll(removalList);

        if (Game.FLAG_FIX_DRAW_ORDER) {
            Collections.sort(this.sprites);
        }
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
            
            this.sprites.add(mob);
        }
    }

    private void spawnPowerups() {
        Random r = new Random();
        Powerup powerup = null;
        switch (r.nextInt(0, Powerup.TOTAL_POWERUPS)) {
        case LampPowerup.ID:
            powerup = new LampPowerup(0, 0);
            break;
        case HayPowerup.ID:
            powerup = new HayPowerup(0, 0);
            break;
        case WheelPowerup.ID:
            powerup = new WheelPowerup(0, 0);
            break;
        default:
            // This should not be reached.
            powerup = null;
            break;
        }
        int powerupWidth = (int) powerup.getBounds().getWidth();
        int powerupHeight = (int) powerup.getBounds().getHeight();

        powerup.setX(r.nextInt(
                powerupWidth,
                Game.WINDOW_WIDTH / 2));
        powerup.setY(r.nextInt(
                powerupHeight,
                Game.WINDOW_HEIGHT - powerupHeight * 2));
        
        this.sprites.add(powerup);
    }

    public Outlaw getOutlaw() {
        return outlaw;
    }

    public void triggerSlowMobSpeed(long powerupTimeout) {
        this.isSlowSpeed = true;
        this.slowSpeedTime = System.nanoTime();
        this.slowSpeedEndTime = powerupTimeout;
    }
    
    public int getMobKillCount() {
        return this.mobKillCount;
    }
    
    public String getTimeLeftDisplayString() {
        String formatString = "%s:%s";
        if (this.levelTimeLeft < 0) {
            return String.format(formatString, "0", "00");
        }
        long levelTimeLeftMinutes = 
                TimeUnit.SECONDS.toMinutes(levelTimeLeft);
        long levelTimeLeftSeconds =
                levelTimeLeft - (60 * levelTimeLeftMinutes);
        
        if (levelTimeLeftSeconds <= 9) {
            formatString = "%s:0%s";
        }
        
        return String.format(
                formatString,
                levelTimeLeftMinutes,
                levelTimeLeftSeconds);
    }

    public void notifyPowerupConsumed(int id) {
        if (id < 0 || id >= Powerup.TOTAL_POWERUPS) {
            return;
        }
        
        this.powerupsCount[id]++;
    }
    
    public int getPowerupCount(int id) {
        if (id < 0 || id >= Powerup.TOTAL_POWERUPS) {
            return -1;
        }
        
        return this.powerupsCount[id];
    }
    
    public boolean isSlowSpeed() {
        return this.isSlowSpeed;
    }

    public boolean isMaxSpeed() {
        return this.isMaxSpeed;
    }
    
    public void markLevelDone() {
        this.isLevelDone = true;
    }

    public boolean isLevelDone() {
        return this.isLevelDone;
    }
    
    // FIXME: This will go away soon.
    public ArrayList<Sprite> getSprites() {
        return this.sprites;
    }

}
