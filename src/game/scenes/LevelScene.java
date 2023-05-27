package game.scenes;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.LevelMap;
import game.TimedActionManager;
import game.entities.Bullet;
import game.entities.CactusMob;
import game.entities.CoffinMob;
import game.entities.CowboyMob;
import game.entities.CoyoteMob;
import game.entities.HayPowerup;
import game.entities.LampPowerup;
import game.entities.Mob;
import game.entities.Outlaw;
import game.entities.Powerup;
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

public class LevelScene implements GameScene {

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

    private Group root;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;

    private TimedActionManager actions;

    private Outlaw outlaw;
    private Mob bossMob;
    private StatusHUD statusHud;

    private int mobKillCount;
    private int powerupsCount[];

    private long levelTimeLeft;
    private long levelStartTime;

    private boolean maxSpeed;
    private boolean slowSpeed;
    private boolean zeroSpeed;
    private boolean levelDone;
    private boolean levelPaused;

    private LevelMap levelMap;

    public LevelScene() {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT, Game.COLOR_MAIN);
        this.canvas = new Canvas(Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.root.getChildren().add(canvas);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);

        this.actions = new TimedActionManager();
        this.initializeActions();

        this.outlaw = new Outlaw(
                "Going merry",
                OUTLAW_INITIAL_X, 0, this);
        this.getOutlaw().setY(Game.RNG.nextInt(
                (int) getOutlaw().getBounds().getHeight(),
                Game.WINDOW_MAX_HEIGHT - (int) getOutlaw().getBounds().getHeight()));
        this.getOutlaw().handleKeyPressEvent(this);
        this.bossMob = null;
        this.statusHud = new StatusHUD(this);

        this.levelTimeLeft = 0;
        this.levelStartTime = System.nanoTime();

        this.mobKillCount = 0;
        this.powerupsCount = new int[Powerup.TOTAL_POWERUPS];

        this.maxSpeed = false;
        this.slowSpeed = false;
        this.zeroSpeed = false;
        this.levelDone = false;
        this.levelPaused = false;

        this.levelMap = new LevelMap();
        this.levelMap.generate();
        this.levelMap.generateProps();
        this.levelMap.addSpriteOnUpdate(getOutlaw());

        if (Game.DEBUG_MODE) {
            scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent e) {
                    KeyCode code = e.getCode();
                    if (code == KeyCode.F1) {
                        levelMap.generate(true);
                    }
                }
            });
        }

        this.spawnMobs(MOB_COUNT_AT_SPAWN);
        this.addPauseHandler();
    }

    private void initializeActions() {
        // Action: mark level done if time's up.
        actions.add(LEVEL_END_TIME, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (!Game.FLAG_DELAY_IF_BOSS_IS_ALIVE || (bossMob != null
                        && !bossMob.isAlive() && !bossMob.isDying())) {
                    markLevelDone();
                    return true;
                }
                return false;
            }
        });
        // Action: spawn boss.
        actions.add(LEVEL_BOSS_TIME, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (bossMob == null) {
                    bossMob = new CowboyMob(
                            Game.WINDOW_MAX_WIDTH,
                            Game.WINDOW_MAX_HEIGHT / 2,
                            LevelScene.this);
                    bossMob.addY((int) -bossMob.getBounds().getHeight() / 2);
                    bossMob.addX((int) -bossMob.getBounds().getWidth());
                    levelMap.addSpriteOnUpdate(bossMob);
                }
                return true;
            }
        });
        // Action: spawn mobs every 3 seconds.
        actions.add(MOB_SPAWN_INTERVAL, true, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                spawnMobs(MOB_COUNT_PER_INTERVAL);
                return true;
            }
        });
        // Speed up mob movement every 15 seconds.
        actions.add(MOB_MAX_SPEED_INTERVAL, true, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                maxSpeed = true;
                // Reset back to normal speed after 3 seconds if we've
                // sped up mob movement.
                actions.add(MOB_MAX_SPEED_END_INTERVAL, false, new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        maxSpeed = false;
                        return true;
                    }
                });
                return true;
            }
        });
        // Spawn power-ups every 10 seconds.
        actions.add(POWERUP_SPAWN_INTERVAL, true, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                spawnPowerups();
                return true;
            }
        });
    }

    @Override
    public void update(long now) {
        this.actions.update(now);
        this.statusHud.update(now);
        if (this.levelDone || this.levelPaused) {
            return;
        }
        this.updateSprites(now);
        this.levelMap.update(now);
        this.levelTimeLeft = TimeUnit.NANOSECONDS.toSeconds(
                LEVEL_END_TIME - (now - this.levelStartTime));
    }

    @Override
    public void draw(long now) {
        this.gc.clearRect(0, 0, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);

        this.levelMap.draw(gc);
        this.statusHud.draw(gc);
    }

    private void updateSprites(long now) {
        for (Sprite sprite : this.levelMap.getSprites()) {
            if (sprite instanceof Mob) {
                Mob mob = (Mob)sprite;
                if (!mob.isAlive() && !mob.isDying()) {
                    this.levelMap.removeSpriteOnUpdate(mob);
                    this.mobKillCount++;
                }
            } else if (sprite instanceof Bullet) {
                Bullet bullet = (Bullet) sprite;
                if (!bullet.getVisible()) {
                    this.levelMap.removeSpriteOnUpdate(bullet);
                    continue;
                }
            }
            sprite.update(now);
        }
    }

    private void spawnMobs(int mobCount) {
        for (int i = 0; i < mobCount; i++) {
            Mob mob = null;
            switch (Game.RNG.nextInt(0, Mob.TOTAL_MOBS)) {
            case 0:
                mob = new CactusMob(0, 0, this);
                break;
            case 1:
                mob = new CoyoteMob(0, 0, this);
                break;
            case 2:
                mob = new CoffinMob(0, 0, this);
                break;
            default:
                // This should not be reached.
                break;
            }

            int mobWidth = (int) mob.getBounds().getWidth();
            int mobHeight = (int) mob.getBounds().getHeight();

            mob.setX(Game.RNG.nextInt(
                    Game.WINDOW_MAX_WIDTH / 2,
                    Game.WINDOW_MAX_WIDTH - mobWidth));
            mob.setY(Game.RNG.nextInt(
                    mobHeight,
                    Game.WINDOW_MAX_HEIGHT - mobHeight * 2));

            this.levelMap.addSpriteOnUpdate(mob);
        }
    }

    private void spawnPowerups() {
        Powerup powerup = null;
        switch (Game.RNG.nextInt(0, Powerup.TOTAL_POWERUPS)) {
        case LampPowerup.ID:
            powerup = new LampPowerup(0, 0, this);
            break;
        case HayPowerup.ID:
            powerup = new HayPowerup(0, 0, this);
            break;
        case WheelPowerup.ID:
            powerup = new WheelPowerup(0, 0, this);
            break;
        case SnakeOilPowerup.ID:
            powerup = new SnakeOilPowerup(0, 0, this);
            break;
        default:
            // This should not be reached.
            powerup = null;
            break;
        }
        int powerupWidth = (int) powerup.getBounds().getWidth();
        int powerupHeight = (int) powerup.getBounds().getHeight();

        powerup.setX(Game.RNG.nextInt(
                powerupWidth,
                Game.WINDOW_MAX_WIDTH / 2));
        powerup.setY(Game.RNG.nextInt(
                powerupHeight,
                Game.WINDOW_MAX_HEIGHT - powerupHeight * 2));

        this.levelMap.addSpriteOnUpdate(powerup);
    }

    public void applySlowMobSpeed(long powerupTimeout) {
        this.slowSpeed = true;
        actions.add(powerupTimeout, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                slowSpeed = false;
                return true;
            }
        });
    }

    public void applyZeroMobSpeed(long powerupTimeout) {
        this.zeroSpeed = true;
        actions.add(powerupTimeout, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                zeroSpeed = false;
                return true;
            }
        });
    }

    public void consumePowerup(int id) {
        if (id < 0 || id >= Powerup.TOTAL_POWERUPS) {
            return;
        }

        this.powerupsCount[id]++;
    }

    private final EventHandler<KeyEvent> pauseEventHandler =
            new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent e) {
                    KeyCode code = e.getCode();
                    switch (code) {
                    case BACK_SPACE:
                    case ESCAPE:
                        if (!levelPaused) {
                            getActions().stopAll();
                            System.out.println("stop");
                        } else {
                            getActions().startAll();
                            System.out.println("start");
                        }
                        levelPaused = !levelPaused;
                        break;
                    default:
                        break;
                    }
                }
            };

    private void addPauseHandler() {
        getInner().addEventHandler(
                KeyEvent.KEY_RELEASED, this.pauseEventHandler);
    }

    private void removePauseHandler() {
        getInner().removeEventHandler(
                KeyEvent.KEY_RELEASED, this.pauseEventHandler);
    }

    @Override
    public Scene getInner() {
        return this.scene;
    }

    @Override
    public Group getRoot() {
        return root;
    }

    public Outlaw getOutlaw() {
        return outlaw;
    }

    @Override
    public TimedActionManager getActions() {
        return this.actions;
    }

    public int getMobKillCount() {
        return this.mobKillCount;
    }

    public int getPowerupCount(int id) {
        if (id < 0 || id >= Powerup.TOTAL_POWERUPS) {
            return -1;
        }

        return this.powerupsCount[id];
    }

    public long getLevelTimeLeft() {
        return this.levelTimeLeft;
    }

    public boolean isMaxSpeed() {
        return this.maxSpeed;
    }

    public boolean isSlowSpeed() {
        return this.slowSpeed;
    }

    public boolean isZeroSpeed() {
        return this.zeroSpeed;
    }

    public boolean isLevelDone() {
        return this.levelDone;
    }

    public boolean isLevelPaused() {
        return this.levelPaused;
    }

    public void markLevelDone() {
        if (this.levelDone) {
            return;
        }
        this.getActions().removeAll();
        this.removePauseHandler();
        MainMenuScene.handleReturnKeyPressEvent(this);
        this.levelDone = true;
    }

    public LevelMap getLevelMap() {
        return this.levelMap;
    }

}
