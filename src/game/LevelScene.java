package game;

import java.util.Random;
import java.util.concurrent.TimeUnit;

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
import game.entities.SnakeOilPowerup;
import game.entities.Sprite;
import game.entities.StatusHUD;
import game.entities.WheelPowerup;
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
    public Group getRoot() {
        return root;
    }

    private Canvas canvas;
    private GraphicsContext gc;

    private Outlaw outlaw;
    private Mob bossMob;
    private StatusHUD statusHud;
    private TimedActionManager actions;

    private int mobKillCount;
    private int powerupsCount[];
    
    private long levelTimeLeft;
    private long levelStartTime;

    private boolean maxSpeed;
    private boolean slowSpeed;
    private boolean zeroSpeed;
    private boolean levelDone;

    private LevelMap levelMap;
    private Random random;

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

    public LevelScene() {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT, Color.valueOf("eeca84"));
        this.canvas = new Canvas(Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);
        this.root.getChildren().add(canvas);
        this.random = new Random();
        this.outlaw = new Outlaw(
                "Going merry",
                OUTLAW_INITIAL_X, 0);
        this.getOutlaw().setY(this.random.nextInt(
                (int) getOutlaw().getBounds().getHeight(),
                Game.WINDOW_HEIGHT - (int) getOutlaw().getBounds().getHeight()));
        this.getOutlaw().handleKeyPressEvent(this);
        this.statusHud = new StatusHUD(this);
        this.levelStartTime = System.nanoTime();
        this.actions = new TimedActionManager();
        this.initializeActions();
        this.maxSpeed = false;
        this.slowSpeed = false;
        this.zeroSpeed = false;
        this.levelDone = false;
        this.mobKillCount = 0;
        this.powerupsCount = new int[Powerup.TOTAL_POWERUPS];

        this.levelMap = new LevelMap();
        this.levelMap.generate();
        this.levelMap.generateProps();
        this.levelMap.getSprites().add(getOutlaw());
        
        if (Game.DEBUG_MODE) {
            scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                public void handle(KeyEvent e) {
                    KeyCode code = e.getCode();
                    if (code == KeyCode.F1) {
                        levelMap.generate(true);
                    }
                }
            });
        }
        
        this.spawnMobs(MOB_COUNT_AT_SPAWN);
    }

    public void initializeActions() {
        // Action: mark level done if time's up.
        actions.add(LEVEL_END_TIME, false, new Runnable() {
            @Override
            public void run() {
                if (!Game.FLAG_DELAY_IF_BOSS_IS_ALIVE
                        || (bossMob != null
                        && !bossMob.isAlive()
                        && !bossMob.isDying())) {
                    markLevelDone();
                }
            }
        });
        // Action: spawn boss.
        actions.add(LEVEL_BOSS_TIME, false, new Runnable() {
            @Override
            public void run() {
                if (bossMob != null) {
                    return;
                }
                bossMob = new CowboyMob(Game.WINDOW_WIDTH, (Game.WINDOW_HEIGHT / 2));
                bossMob.addY((int) -bossMob.getBounds().getHeight() / 2);
                bossMob.addX((int) -bossMob.getBounds().getWidth());
                levelMap.getSprites().add(bossMob);
            }
        });
        // Action: spawn mobs every 3 seconds.
        actions.add(MOB_SPAWN_INTERVAL, true, new Runnable() {
            @Override
            public void run() {
                spawnMobs(MOB_COUNT_PER_INTERVAL);
            }
        });
        // Speed up mob movement every 15 seconds.
        actions.add(MOB_MAX_SPEED_INTERVAL, true, new Runnable() {
            @Override
            public void run() {
                maxSpeed = true;
                // Reset back to normal speed after 3 seconds if we've
                // sped up mob movement.
                actions.add(MOB_MAX_SPEED_END_INTERVAL, false, new Runnable() {
                    @Override
                    public void run() {
                        maxSpeed = false;
                    }
                });
            }
        });
        // Spawn power-ups every 10 seconds.
        actions.add(POWERUP_SPAWN_INTERVAL, true, new Runnable() {
            @Override
            public void run() {
                spawnPowerups();
            }
        });
    }
    
    @Override
    public Scene getInnerScene() {
        return this.scene;
    }    
    
    @Override
    public void update(long currentNanoTime) {
        this.statusHud.update(currentNanoTime, this);
        if (this.levelDone) {
            return;
        }
        this.updateSprites(currentNanoTime);
        this.updateLevelTime(currentNanoTime);
    }

    private void updateLevelTime(long currentNanoTime) {
        long deltaTime = (currentNanoTime - this.levelStartTime);
        this.levelTimeLeft = TimeUnit.NANOSECONDS.toSeconds(LEVEL_END_TIME - deltaTime);
        this.actions.update(currentNanoTime);
    }

    @Override
    public void draw(long currentNanoTime) {
        this.gc.clearRect(0, 0, Game.WINDOW_WIDTH,
                Game.WINDOW_HEIGHT);

        this.levelMap.draw(gc);
        this.statusHud.draw(gc);
    }
    
    private void updateSprites(long currentNanoTime) {
        for (Sprite sprite : this.levelMap.getSprites()) {
            if (sprite instanceof Mob) {
                Mob mob = (Mob)sprite;
                if (!mob.isAlive() && !mob.isDying()) {
                    this.levelMap.removeSpriteOnUpdate(mob);
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

        this.levelMap.update(currentNanoTime);
    }

    private Mob randomizeMob() {
        switch (this.random.nextInt(0, Mob.TOTAL_MOBS)) {
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
        for (int i = 0; i < mobCount; i++) {
            Mob mob = randomizeMob();
            
            int mobWidth = (int) mob.getBounds().getWidth();
            int mobHeight = (int) mob.getBounds().getHeight();

            mob.setX(this.random.nextInt(
                    Game.WINDOW_WIDTH / 2,
                    Game.WINDOW_WIDTH - mobWidth));
            mob.setY(this.random.nextInt(
                    mobHeight,
                    Game.WINDOW_HEIGHT - mobHeight * 2));
            
            this.levelMap.addSpriteOnUpdate(mob);
        }
    }

    private void spawnPowerups() {
        Powerup powerup = null;
        switch (this.random.nextInt(0, Powerup.TOTAL_POWERUPS)) {
        case LampPowerup.ID:
            powerup = new LampPowerup(0, 0);
            break;
        case HayPowerup.ID:
            powerup = new HayPowerup(0, 0);
            break;
        case WheelPowerup.ID:
            powerup = new WheelPowerup(0, 0);
            break;
        case SnakeOilPowerup.ID:
            powerup = new SnakeOilPowerup(0, 0);
            break;
        default:
            // This should not be reached.
            powerup = null;
            break;
        }
        int powerupWidth = (int) powerup.getBounds().getWidth();
        int powerupHeight = (int) powerup.getBounds().getHeight();

        powerup.setX(this.random.nextInt(
                powerupWidth,
                Game.WINDOW_WIDTH / 2));
        powerup.setY(this.random.nextInt(
                powerupHeight,
                Game.WINDOW_HEIGHT - powerupHeight * 2));

        this.levelMap.addSpriteOnUpdate(powerup);
    }

    public Outlaw getOutlaw() {
        return outlaw;
    }

    public void applySlowMobSpeed(long powerupTimeout) {
        this.slowSpeed = true;
        actions.add(powerupTimeout, false, new Runnable() {
            @Override
            public void run() {
                slowSpeed = false;
            }            
        });
    }

    public void applyZeroMobSpeed(long powerupTimeout) {
        this.zeroSpeed = true;
        actions.add(powerupTimeout, false, new Runnable() {
            @Override
            public void run() {
                zeroSpeed = false;
            }            
        });
    }
    
    public int getMobKillCount() {
        return this.mobKillCount;
    }
    
    public String getTimeLeftDisplayText() {
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
        return this.slowSpeed;
    }

    public boolean isZeroSpeed() {
        return this.zeroSpeed;
    }

    public boolean isMaxSpeed() {
        return this.maxSpeed;
    }

    public boolean isLevelDone() {
        return this.levelDone;
    }

    public void markLevelDone() {
        this.levelDone = true;
    }

    public LevelMap getLevelMap() {
        return this.levelMap;
    }

}
