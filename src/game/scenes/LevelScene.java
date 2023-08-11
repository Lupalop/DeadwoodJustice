package game.scenes;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.ActionTimer;
import game.Game;
import game.LevelMap;
import game.UIUtils;
import game.entities.Button;
import game.entities.Mote;
import game.entities.Outlaw;
import game.entities.Sprite;
import game.entities.mobs.CactusMob;
import game.entities.mobs.CoffinMob;
import game.entities.mobs.CowboyMob;
import game.entities.mobs.CoyoteMob;
import game.entities.mobs.Mob;
import game.entities.powerups.HayPowerup;
import game.entities.powerups.LampPowerup;
import game.entities.powerups.Powerup;
import game.entities.powerups.SnakeOilPowerup;
import game.entities.powerups.WheelPowerup;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * This class represents the Level scene logic.
 * @author Francis Dominic Fajardo
 */
public final class LevelScene extends GameScene {

    /** Difficulty: easy. */
    public static final int DIFFICULTY_EASY = 0;
    /** Difficulty: medium. */
    public static final int DIFFICULTY_MEDIUM = 1;
    /** Difficulty: hard. */
    public static final int DIFFICULTY_HARD = 2;

    /** Tuning: number to divide the added score. */
    private static final int SCORE_DIVIDER = 3;
    /** Tuning: number to divide the actual base score. */
    private static final int SCORE_BASE_DIVIDER = 2;
    /** Tuning: maximum number of characters for the player name. */
    public static final int NAME_MAX_LEN = 10;
    /** Tuning: number of mobs to spawn at the start of the level. */
    private static final int MOB_COUNT_AT_START = 7;
    /** Tuning: number of mobs to spawn at the end of the spawn interval. */
    private static final int MOB_COUNT_PER_INTERVAL = 3;
    /** Tuning: interval before mobs are spawned. */
    private static final long MOB_SPAWN_INTERVAL =
            TimeUnit.SECONDS.toNanos(5);
    /** Tuning: interval before a max mob speed event is applied. */
    private static final long MOB_MAX_SPEED_INTERVAL =
            TimeUnit.SECONDS.toNanos(15);
    /** Tuning: duration of the max mob speed event. */
    private static final long MOB_MAX_SPEED_END_INTERVAL =
            TimeUnit.SECONDS.toNanos(3);
    /** Tuning: interval before power-ups are spawned. */
    private static final long POWERUP_SPAWN_INTERVAL =
            TimeUnit.SECONDS.toNanos(10);
    /** Tuning: seconds to wait before the boss mob is spawned. */
    private static final long LEVEL_BOSS_TIME =
            TimeUnit.SECONDS.toNanos(30);
    /** Tuning: seconds to wait before the level is completed. */
    private static final long LEVEL_END_TIME =
            TimeUnit.SECONDS.toNanos(60);
    /** Tuning: player character's initial x-coordinate position. */
    private static final int OUTLAW_INITIAL_X = 100;

    /** Path to available background music tracks. */
    private static final String[] MUSIC_REGULAR =
            { "bgm_01.mp3", "bgm_02.mp3", "bgm_04.mp3" };
    /** Path to available background music tracks in hard difficulty. */
    private static final String[] MUSIC_HARD =
            { "bgm_03.mp3", "bgm_05.mp3" };

    /** The player character. */
    private Outlaw outlaw;
    /** The boss mob. */
    private Mob bossMob;
    /** The status overlay. */
    private StatusOverlay statusOverlay;

    /** State: level difficulty. */
    private int difficulty;
    /** State: number of mobs to spawn at the start of the level. */
    private int mobCountAtStart;
    /** State: number of mobs to spawn at the end of the spawn interval. */
    private int mobCountPerInterval;
    /** State: seconds to wait before the level is completed. */
    private long levelEndTime;
    /** State: interval before power-ups are spawned. */
    private long powerupSpawnInterval;

    /** State: base (not actual) player score. */
    private int score;
    /** State: number of killed mobs. */
    private int mobKillCount;
    /** State: number of collected power-ups. */
    private int powerupCount[];
    /** State: action timer array for keeping track of active power-ups. */
    private ActionTimer[] powerupTimers;
    /** State: action timer handling how long a level is. */
    private ActionTimer levelTimer;

    /** Event: mobs move at maximum speed. */
    private boolean maxSpeed;
    /** Event: mobs move at a reduced speed. */
    private boolean slowSpeed;
    /** Event: mobs move at zero speed (can't move). */
    private boolean zeroSpeed;
    /** Event: level is done. */
    private boolean levelDone;
    /** Event: level is paused. */
    private boolean levelPaused;

    /**
     * Constructs an instance of LevelScene.
     * @param difficulty an integer indicating the starting difficulty.
     */
    public LevelScene(int difficulty) {
        this.difficulty = difficulty;
        switch (this.difficulty) {
        default:
        case DIFFICULTY_EASY:
            this.levelEndTime = LEVEL_END_TIME;
            this.mobCountAtStart = MOB_COUNT_AT_START;
            this.mobCountPerInterval = MOB_COUNT_PER_INTERVAL;
            this.powerupSpawnInterval = POWERUP_SPAWN_INTERVAL;
            break;
        case DIFFICULTY_MEDIUM:
            this.levelEndTime = LEVEL_END_TIME * 2;
            this.mobCountAtStart = MOB_COUNT_AT_START * 2;
            this.mobCountPerInterval = MOB_COUNT_PER_INTERVAL * 2;
            this.powerupSpawnInterval = (long) (POWERUP_SPAWN_INTERVAL / 1.5);
            break;
        case DIFFICULTY_HARD:
            this.levelEndTime = LEVEL_END_TIME * 5;
            this.mobCountAtStart = MOB_COUNT_AT_START * 3;
            this.mobCountPerInterval = MOB_COUNT_PER_INTERVAL * 3;
            this.powerupSpawnInterval = POWERUP_SPAWN_INTERVAL / 2;
            break;
        }

        // XXX: Hide props if we're checking for prop colliders and
        // the difficulty is set to easy. Keep it this way until
        // mob pathfinding becomes "acceptable".
        this.initialize(this.getRestrictedMode());
        this.initializeActions();

        this.outlaw = new Outlaw(OUTLAW_INITIAL_X, 0, this);
        this.getOutlaw().setY(Game.RNG.nextInt(
                (int) getOutlaw().getBounds().getHeight(),
                Game.WINDOW_MAX_HEIGHT - (int) getOutlaw().getBounds().getHeight()));
        this.getOutlaw().handleKeyPressEvent(this);
        this.bossMob = null;
        this.statusOverlay = new StatusOverlay(this);

        this.mobKillCount = 0;
        this.powerupCount = new int[Powerup.TOTAL_POWERUPS];
        this.powerupTimers = new ActionTimer[Powerup.TOTAL_POWERUPS];

        this.maxSpeed = false;
        this.slowSpeed = false;
        this.zeroSpeed = false;
        this.levelDone = false;
        this.levelPaused = false;

        this.levelMap.addEntity(getOutlaw());

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

        this.spawnMobs(this.mobCountAtStart);
        this.addPauseHandler();
    }

    /**
     * Initializes level-related actions.
     */
    private void initializeActions() {
        // Action: mark level done if time's up.
        this.levelTimer = timers.add(this.levelEndTime, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (bossMob != null
                        && !bossMob.isAlive()
                        && !bossMob.isDying()) {
                    markLevelDone();
                    return true;
                }
                return false;
            }
        });
        // Action: spawn boss.
        boolean spawnBossMultiple = (this.difficulty == DIFFICULTY_HARD);
        timers.add(LEVEL_BOSS_TIME, spawnBossMultiple, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (bossMob == null || !bossMob.isAlive()) {
                    bossMob = new CowboyMob(
                            Game.WINDOW_MAX_WIDTH,
                            Game.WINDOW_MAX_HEIGHT / 2,
                            LevelScene.this);
                    bossMob.addY((int) -bossMob.getBounds().getHeight() / 2);
                    bossMob.addX((int) -bossMob.getBounds().getWidth());
                    levelMap.addEntity(bossMob);
                }
                return true;
            }
        });
        // Action: spawn mobs every 3 seconds.
        timers.add(MOB_SPAWN_INTERVAL, true, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                spawnMobs(mobCountPerInterval);
                return true;
            }
        });
        // Speed up mob movement every 15 seconds.
        timers.add(MOB_MAX_SPEED_INTERVAL, true, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                maxSpeed = true;
                // Reset back to normal speed after 3 seconds if we've
                // sped up mob movement.
                timers.add(MOB_MAX_SPEED_END_INTERVAL, false, new Callable<Boolean>() {
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
        timers.add(this.powerupSpawnInterval, true, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                spawnPowerups();
                return true;
            }
        });
    }

    @Override
    public void update(long now) {
        this.timers.update(now);
        this.statusOverlay.update(now);
        if (this.levelDone || this.levelPaused) {
            return;
        }
        this.levelMap.update(now);
    }

    @Override
    public void draw(long now) {
        this.gc.clearRect(0, 0, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);

        this.levelMap.draw(gc);
        this.statusOverlay.draw(gc);
    }

    /**
     * Spawns a mote at the specified target sprite.
     * @param target the Sprite from which the mote originated.
     * @param value an integer.
     * @param moteType the type of mote (constant).
     */
    public void spawnMote(Sprite target, int value, byte moteType) {
        Mote mote = new Mote(target, value, moteType);
        this.getLevelMap().addOverlay(mote);
        mote.show(this);
    }

    /**
     * Spawns one or more mobs at a random location past
     * the middle of the viewport.
     * @param mobCount the number of mobs to be spawned.
     */
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
                    0,
                    Game.WINDOW_MAX_HEIGHT - mobHeight));

            this.levelMap.addEntity(mob);
        }
    }

    /**
     * Spawns a single power-up at a random location past
     * the middle of the viewport.
     */
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

        this.levelMap.addEntity(powerup);
    }

    /**
     * Applies the specified power-up action and replaces the existing
     * tracked power-up action if it already exists to avoid conflicts.
     * @param id the power-up ID (constant).
     * @param timer an ActionTimer from a power-up.
     */
    private void replacePowerupTimer(int id, ActionTimer timer) {
        ActionTimer previousAction = this.powerupTimers[id];
        // Close the previous same power-up action if it still exists.
        if (previousAction != null) {
            previousAction.close();
        }
        this.powerupTimers[id] = timer;
    }

    /**
     * Applies the immortality power-up event.
     * @param powerupTimeout duration of the power-up's effect.
     */
    public void applyImmortality(long powerupTimeout) {
        replacePowerupTimer(HayPowerup.ID,
                timers.add(powerupTimeout, false, new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        getOutlaw().setImmortal(false);
                        return true;
                    }
                }));
        getOutlaw().setImmortal(true);
    }

    /**
     * Applies the slow all mobs speed power-up event.
     * @param powerupTimeout duration of the power-up's effect.
     */
    public void applySlowMobSpeed(long powerupTimeout) {
        replacePowerupTimer(WheelPowerup.ID,
                timers.add(powerupTimeout, false, new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        slowSpeed = false;
                        return true;
                    }
                }));
        this.slowSpeed = true;
    }

    /**
     * Applies the zero all mobs speed power-up event.
     * @param powerupTimeout duration of the power-up's effect.
     */
    public void applyZeroMobSpeed(long powerupTimeout) {
        replacePowerupTimer(SnakeOilPowerup.ID,
                timers.add(powerupTimeout, false, new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        zeroSpeed = false;
                        return true;
                    }
                }));
        this.zeroSpeed = true;
    }

    /**
     * Increments the counter for the specified power-up.
     * @param id the power-up ID (constant).
     */
    public void trackCollectedPowerup(int id) {
        if (id < 0 || id >= Powerup.TOTAL_POWERUPS) {
            return;
        }

        this.powerupCount[id]++;
    }

    /**
     * Toggles the paused state event of this level.
     */
    public void togglePaused() {
        if (!levelPaused) {
            getTimers().stopAll();
        } else {
            getTimers().startAll();
        }
        statusOverlay.togglePausedVisibility();
        levelPaused = !levelPaused;
        Game.playSFX(Button.SFX_BUTTON);
    }

    /** Paused state event handler. */
    private final EventHandler<KeyEvent> pauseEventHandler =
            new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent e) {
                    KeyCode code = e.getCode();
                    switch (code) {
                    case BACK_SPACE:
                    case ESCAPE:
                        togglePaused();
                        break;
                    default:
                        break;
                    }
                }
            };

    /**
     * Adds the paused state event handler to the inner JavaFX scene.
     */
    private void addPauseHandler() {
        getInner().addEventHandler(
                KeyEvent.KEY_RELEASED, this.pauseEventHandler);
    }

    /**
     * Removes the paused state event handler from the inner JavaFX scene.
     */
    private void removePauseHandler() {
        getInner().removeEventHandler(
                KeyEvent.KEY_RELEASED, this.pauseEventHandler);
    }

    /**
     * Retrieves the player character entity.
     * @return an Outlaw object.
     */
    public Outlaw getOutlaw() {
        return outlaw;
    }

    /**
     * Retrieves the current level difficulty.
     * @return an integer.
     */
    public int getDifficulty() {
        return this.difficulty;
    }

    /**
     * Retrieves whether the current level is in restricted mode.
     * @return a boolean.
     */
    public boolean getRestrictedMode() {
        return this.difficulty == DIFFICULTY_EASY;
    }

    /**
     * Retrieves the player's current score.
     * @return an integer.
     */
    public int getScore() {
        int difficultyMultiplier = this.getDifficulty() + 1;
        int baseScore = (this.score > 0 ? (this.getOutlaw().getStrength() / SCORE_BASE_DIVIDER) : 0);
        int computedScore = (this.score + baseScore) * difficultyMultiplier;
        return computedScore;
    }

    /**
     * Adds the specified value to the player's current score.
     * @param value a non-negative number.
     */
    public void addScore(int value) {
        // This must be a non-negative number.
        if (value < 0) {
            return;
        }

        // Further reduce the added score by dividing it.
        value /= SCORE_DIVIDER;

        // Fallback: Catch cases where the value is probably fractional
        // and was rounded to 0. Default them to 1.
        if (value == 0) {
            value = 1;
        }

        this.score += value;
    }

    /**
     * Retrieves the total number of killed mobs.
     * @return an integer.
     */
    public int getMobKillCount() {
        return this.mobKillCount;
    }

    /**
     * Retrieves the number of collected power-ups.
     * @param id the power-up to be checked (constant).
     * @return an integer.
     */
    public int getPowerupCount(int id) {
        if (id < 0 || id >= Powerup.TOTAL_POWERUPS) {
            return -1;
        }

        return this.powerupCount[id];
    }

    /**
     * Retrieves the remaining time in this level.
     * @return a long containing the remaining time.
     */
    public long getLevelTimeLeft() {
        return (this.levelEndTime - this.levelTimer.getElapsedTime());
    }

    /**
     * Retrieves whether mobs are moving at maximum speed.
     * @return a boolean.
     */
    public boolean isMaxSpeed() {
        return this.maxSpeed;
    }

    /**
     * Retrieves whether mobs are moving at a reduced speed.
     * @return a boolean.
     */
    public boolean isSlowSpeed() {
        return this.slowSpeed;
    }

    /**
     * Retrieves whether mobs are moving at zero speed.
     * @return a boolean.
     */
    public boolean isZeroSpeed() {
        return this.zeroSpeed;
    }

    /**
     * Retrieves whether the level is done.
     * @return a boolean.
     */
    public boolean isLevelDone() {
        return this.levelDone;
    }

    /**
     * Retrieves whether the level state is paused.
     * @return a boolean.
     */
    public boolean isLevelPaused() {
        return this.levelPaused;
    }

    /**
     * Marks the level as done.
     */
    public void markLevelDone() {
        if (this.levelDone) {
            return;
        }
        this.getTimers().removeAll();
        this.removePauseHandler();
        if (Game.getHighScoreIndex(score) != -1) {
            this.statusOverlay.toggleNameInputVisibility();
        } else {
            UIUtils.handleReturnToMainMenu(this);
            this.statusOverlay.toggleGameEndVisibility();
        }
        this.levelDone = true;
    }

    /**
     * Retrieves the level map associated with this scene.
     * @return a LevelMap.
     */
    public LevelMap getLevelMap() {
        return this.levelMap;
    }

    @Override
    public String getBGM() {
        int index = Game.RNG.nextInt(0, MUSIC_REGULAR.length);
        if (this.getDifficulty() == DIFFICULTY_HARD) {
            index = Game.RNG.nextInt(0, MUSIC_HARD.length);
            return MUSIC_HARD[index];
        }
        return MUSIC_REGULAR[index];
    }

    /**
     * Handles and receives the specified name from the player, which will
     * be used to save their high score in this level.
     * @param value the player name.
     */
    public void handleHighScore(String value) {
        Game.addHighScore(value, this.getScore(), this.getDifficulty());
        UIUtils.handleReturnToMainMenu(this);
        this.statusOverlay.toggleGameEndVisibility();
    }

    /**
     * Increments the number of killed mobs.
     */
    public void incrementMobKillCount() {
        if (this.levelDone) {
            return;
        }
        this.mobKillCount++;
    }

}
