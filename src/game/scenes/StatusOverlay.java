package game.scenes;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.entities.Button;
import game.entities.Tile;
import game.entities.powerups.HayPowerup;
import game.entities.powerups.LampPowerup;
import game.entities.powerups.SnakeOilPowerup;
import game.entities.powerups.WheelPowerup;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;

public class StatusOverlay {

    private static final Tile TILE =
            new Tile("tilemap_ui.png", 4, 8);

    private static final Image GAME_PAUSED =
            new Image(Game.getAsset("ui_paused.png"));
    private static final Image GAME_END_BAD =
            new Image(Game.getAsset("ui_game_end_bad.png"));
    private static final Image GAME_END_GOOD =
            new Image(Game.getAsset("ui_game_end_good.png"));
    private static final Image STANDEE_PLAY =
            new Image(Game.getAsset("ui_game_end_standee_play.png"));
    private static final Image STANDEE_EXIT =
            new Image(Game.getAsset("ui_game_end_standee_exit.png"));

    private static final int HUD_BASE_SIZE = 9;
    private static final int HUD_BASE_POS = 1;
    private static final int HUD_BASE_POS_HP = 2;
    private static final int HUD_BASE_POS_MOB = 5;
    private static final int HUD_BASE_POS_TIME = 8;
    private static final int HUD_POWERUP_POS = 12;

    private static final int HUD_OFFSET_Y = -Tile.SIZE_MID;
    private static final int HUD_TEXT_OFFSET_Y = (Tile.SIZE_MID / 2) + 3;
    private static final int HUD_MAX_NUM = 9999;

    private static final int TX_BASE_START = 3;
    private static final int TX_BASE_MID = 1;
    private static final int TX_BASE_END = 4;
    private static final int TX_OUTLAW = 13;
    private static final int TX_INFINITY = 14;
    private static final int TX_MOB = 12;
    private static final int TX_TIME = 8;

    private static final int TX_POWERUP_START = 5;
    private static final int TX_POWERUP_END = 7;
    private static final int TX_POWERUP_LAMP = 9;
    private static final int TX_POWERUP_HAY = 10;
    private static final int TX_POWERUP_WHEEL = 11;
    private static final int TX_POWERUP_SNAKEOIL = 15;

    private static final int TX_POP_START = 16;
    private static final int TX_POP_MID = 18;
    private static final int TX_POP_END = 17;

    private static final long UI_SLIDE_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(100);

    private LevelScene level;

    private boolean isPausedVisible;
    private boolean isGameEndVisible;
    private Button playButton;
    private Button exitButton;
    private Button resumeButton;

    private int hudOffsetY;

    private EventHandler<KeyEvent> selectorEventHandler;
    private Button selectedButton;

    public StatusOverlay(LevelScene scene) {
        this.level = scene;

        this.isPausedVisible = false;
        this.isGameEndVisible = false;
        this.playButton = null;
        this.exitButton = null;
        this.resumeButton = null;

        this.hudOffsetY = HUD_OFFSET_Y;
        scene.getActions().add(UI_SLIDE_INTERVAL, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (hudOffsetY == 0) {
                    return true;
                }
                hudOffsetY++;
                return false;
            }
        });

        this.selectorEventHandler = null;
        this.selectedButton = null;

        initializeButtons();
    }

    public void update(long now) {
        if (this.isGameEndVisible) {
            exitButton.update(now);
            playButton.update(now);
        } else if (this.isPausedVisible) {
            resumeButton.update(now);
        }
    }

    public void draw(GraphicsContext gc) {
        gc.save();
        gc.setFont(Game.FONT_32);
        gc.setFill(Game.COLOR_MAIN);

        if (isGameEndVisible || isPausedVisible) {
            this.drawShade(gc);
        }

        drawHUD(gc);
        if (isGameEndVisible) {
            drawGameEnd(gc);
        } else if (isPausedVisible) {
            drawPaused(gc);
        }

        if (this.selectedButton != null) {
            this.selectedButton.drawSelector(gc);
        }

        gc.restore();
    }

    private void drawHUD(GraphicsContext gc) {
        this.drawHUDBase(gc);

        int powerupLampCount = level.getPowerupCount(LampPowerup.ID);
        int powerupHayCount = level.getPowerupCount(HayPowerup.ID);
        int powerupWheelCount = level.getPowerupCount(WheelPowerup.ID);
        int powerupSnakeOilCount = level.getPowerupCount(SnakeOilPowerup.ID);

        int tileOffset = HUD_POWERUP_POS;
        tileOffset = this.drawHUDPowerup(gc, tileOffset,
                TX_POWERUP_LAMP, powerupLampCount,
                (powerupLampCount == 0) ? 0.5 : 1);
        tileOffset = this.drawHUDPowerup(gc, tileOffset,
                TX_POWERUP_HAY, powerupHayCount,
                (!level.getOutlaw().isImmortal()) ? 0.5 : 1);
        tileOffset = this.drawHUDPowerup(gc, tileOffset,
                TX_POWERUP_WHEEL, powerupWheelCount,
                (!level.isSlowSpeed()) ? 0.5 : 1);
        tileOffset = this.drawHUDPowerup(gc, tileOffset,
                TX_POWERUP_SNAKEOIL, powerupSnakeOilCount,
                (!level.isZeroSpeed()) ? 0.5 : 1);
    }

    private void drawHUDBase(GraphicsContext gc) {
        int strength = level.getOutlaw().getStrength();
        int mobKillCount = level.getMobKillCount();
        String strengthText = Integer.toString(strength);
        String mobKillCountText = Integer.toString(mobKillCount);
        String timeLeftText = this.getLevelTimeLeftText();
        // Draw base HUD background.
        int tileOffset = HUD_BASE_POS;
        TILE.draw(gc, Tile.SIZE_MID,
                hudOffsetY, TX_BASE_START);
        for (int i = 0; i < HUD_BASE_SIZE; i++) {
            TILE.draw(gc, Tile.SIZE_MID * ++tileOffset,
                    hudOffsetY, TX_BASE_MID);
        }
        TILE.draw(gc, Tile.SIZE_MID * ++tileOffset,
                hudOffsetY, TX_BASE_END);
        // Base HUD: player strength.
        TILE.draw(gc, Tile.SIZE_MID * HUD_BASE_POS_HP,
                hudOffsetY, TX_OUTLAW);
        if (level.getOutlaw().isImmortal() || strength > HUD_MAX_NUM) {
            TILE.draw(gc,
                    Tile.SIZE_MID * (HUD_BASE_POS_HP + 1),
                    hudOffsetY, TX_INFINITY);
        } else {
            gc.fillText(strengthText,
                    Tile.SIZE_MID * (HUD_BASE_POS_HP + 1),
                    hudOffsetY + HUD_TEXT_OFFSET_Y);
        }
        // Base HUD: mob kill count.
        TILE.draw(gc,
                Tile.SIZE_MID * HUD_BASE_POS_MOB,
                hudOffsetY, TX_MOB);
        gc.fillText(mobKillCountText,
                Tile.SIZE_MID * (HUD_BASE_POS_MOB + 1),
                hudOffsetY + HUD_TEXT_OFFSET_Y);
        // Base HUD: time left.
        TILE.draw(gc,
                Tile.SIZE_MID * HUD_BASE_POS_TIME,
                hudOffsetY, TX_TIME);
        gc.fillText(timeLeftText,
                Tile.SIZE_MID * (HUD_BASE_POS_TIME + 1),
                hudOffsetY + HUD_TEXT_OFFSET_Y);
    }

    private int drawHUDPowerup(GraphicsContext gc,
            int tileOffset, int iconIndex, int value, double alpha) {
        String valueText = Integer.toString(value);
        gc.save();
        gc.setGlobalAlpha(alpha);
        TILE.draw(gc, Tile.SIZE_MID * tileOffset,
                hudOffsetY, TX_POWERUP_START);
        TILE.draw(gc, Tile.SIZE_MID * tileOffset,
                hudOffsetY, iconIndex);
        TILE.draw(gc, Tile.SIZE_MID * ++tileOffset,
                hudOffsetY, TX_POWERUP_END);
        gc.fillText(valueText, Tile.SIZE_MID * tileOffset,
                hudOffsetY + HUD_TEXT_OFFSET_Y);
        gc.restore();
        return tileOffset += 2;
    }

    private void drawShade(GraphicsContext gc) {
        gc.save();
        gc.setGlobalAlpha(0.5);
        gc.setFill(Game.COLOR_ACCENT);
        gc.fillRect(0, 0, Game.WINDOW_MAX_WIDTH, Game.WINDOW_MAX_HEIGHT);
        gc.restore();
    }

    private void drawMenuBackground(GraphicsContext gc, int base, int innerHeight) {
        for (int i = 0; i < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; i++) {
            TILE.draw(gc, Tile.SIZE_MID * i,
                    Tile.SIZE_MID * base, TX_POP_START);
        }

        for (int i = 0; i < innerHeight; i++) {
            base++;
            for (int j = 0; j < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; j++) {
                TILE.draw(gc, Tile.SIZE_MID * j,
                        Tile.SIZE_MID * base, TX_POP_MID);
            }
        }

        base++;
        for (int i = 0; i < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; i++) {
            TILE.draw(gc, Tile.SIZE_MID * i,
                    Tile.SIZE_MID * base, TX_POP_END);
        }
    }

    private void drawGameEnd(GraphicsContext gc) {
        this.drawMenuBackground(gc, Tile.ALL_VERTICAL / 3, 5);

        Image gameEndCenterImage = null;
        if (!level.getOutlaw().isAlive()) {
            gameEndCenterImage = GAME_END_BAD;
        } else {
            gameEndCenterImage = GAME_END_GOOD;
        }
        gc.drawImage(
                gameEndCenterImage,
                (Game.WINDOW_MAX_WIDTH / 2) - gameEndCenterImage.getWidth() / 2,
                (Game.WINDOW_MAX_HEIGHT / 2) - gameEndCenterImage.getHeight() / 2);

        Image standeePlayImage = STANDEE_PLAY;
        gc.drawImage(
                standeePlayImage,
                (Game.WINDOW_MAX_WIDTH / 5) - standeePlayImage.getWidth() / 2,
                (Game.WINDOW_MAX_HEIGHT / 2) - standeePlayImage.getHeight() / 2);

        Image standeeExitImage = STANDEE_EXIT;
        gc.drawImage(
                standeeExitImage,
                (Game.WINDOW_MAX_WIDTH) - (Game.WINDOW_MAX_WIDTH / 5) - standeeExitImage.getWidth() / 2,
                (Game.WINDOW_MAX_HEIGHT / 2) - standeeExitImage.getHeight() / 2);

        playButton.draw(gc);
        exitButton.draw(gc);
    }

    private void drawPaused(GraphicsContext gc) {
        this.drawMenuBackground(gc, Tile.ALL_VERTICAL - 4, 1);

        gc.drawImage(
                GAME_PAUSED,
                Tile.SIZE_MID,
                Game.WINDOW_MAX_HEIGHT
                - GAME_PAUSED.getHeight()
                - (Tile.SIZE_MID * 2));

        resumeButton.draw(gc);
        exitButton.draw(gc);
    }

    private void initializeButtons() {
        playButton = new Button(0, 0, 3);
        playButton.setText("PLAY");
        playButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(
                        new LevelScene(level.getDifficulty()));
            }
        });
        exitButton = new Button(0, 0, 3);
        exitButton.setText("EXIT");
        exitButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new MainMenuScene());
            }
        });
        resumeButton = new Button(0, 0, 3);
        resumeButton.setText("RESUME");
        resumeButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                level.togglePaused();
            }
        });
    }

    public void toggleGameEndVisibility() {
        this.isGameEndVisible = !this.isGameEndVisible;
        if (this.isGameEndVisible) {
            exitButton.setX((int) ((Game.WINDOW_MAX_WIDTH)
                    - (Game.WINDOW_MAX_WIDTH / 5)
                    - exitButton.getBounds().getWidth() / 2));
            exitButton.setY((Game.WINDOW_MAX_HEIGHT / 2) + 50);
            exitButton.attach(level);

            playButton.setX((int) ((Game.WINDOW_MAX_WIDTH / 5)
                    - playButton.getBounds().getWidth() / 2));
            playButton.setY((Game.WINDOW_MAX_HEIGHT / 2) + 50);
            playButton.attach(level);
            this.attachButtonSelector(playButton, exitButton);
        } else {
            exitButton.detach(level);
            playButton.detach(level);
            this.detachButtonSelector(playButton, exitButton);
        }
    }

    public void togglePausedVisibility() {
        this.isPausedVisible = !this.isPausedVisible;
        if (this.isPausedVisible) {
            exitButton.setX((int) (Game.WINDOW_MAX_WIDTH
                    - (exitButton.getBounds().getWidth())
                    - (Tile.SIZE_MID * 2)));
            exitButton.setY(Game.WINDOW_MAX_HEIGHT - (Tile.SIZE_MID * 3) + 8);
            exitButton.attach(level);

            resumeButton.setX((int) (Game.WINDOW_MAX_WIDTH
                    - (playButton.getBounds().getWidth())
                    - (Tile.SIZE_MID * 8)));
            resumeButton.setY(Game.WINDOW_MAX_HEIGHT - (Tile.SIZE_MID * 3) + 8);
            resumeButton.attach(level);
            this.attachButtonSelector(resumeButton, exitButton);
        } else {
            exitButton.detach(level);
            resumeButton.detach(level);
            this.detachButtonSelector(resumeButton, exitButton);
        }
    }

    private void attachButtonSelector(Button leftButton, Button rightButton) {
        if (this.selectorEventHandler != null) {
            return;
        }
        this.selectedButton = null;
        this.selectorEventHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                case LEFT:
                case RIGHT:
                    if (selectedButton == leftButton) {
                        selectedButton = rightButton;
                    } else {
                        selectedButton = leftButton;
                    }
                    break;
                case ENTER:
                case SPACE:
                    if (selectedButton == null) {
                        selectedButton = leftButton;
                    } else {
                        selectedButton.click();
                    }
                    break;
                default:
                    break;
                }
            }
        };
        level.getInner().addEventHandler(KeyEvent.KEY_PRESSED,
                this.selectorEventHandler);
    }

    private void detachButtonSelector(Button a, Button b) {
        if (this.selectorEventHandler == null) {
            return;
        }
        level.getInner().removeEventHandler(KeyEvent.KEY_PRESSED,
                this.selectorEventHandler);
        this.selectorEventHandler = null;
        this.selectedButton = null;
    }

    private String getLevelTimeLeftText() {
        String formatString = "%s:%s";
        if (level.getLevelTimeLeft() <= 0) {
            return String.format(formatString, "0", "00");
        }
        long levelTimeLeftMinutes =
                TimeUnit.NANOSECONDS.toMinutes(level.getLevelTimeLeft());
        long levelTimeLeftSeconds =
                TimeUnit.NANOSECONDS.toSeconds(level.getLevelTimeLeft())
                - (60 * levelTimeLeftMinutes);

        if (levelTimeLeftSeconds <= 9) {
            formatString = "%s:0%s";
        }

        return String.format(
                formatString,
                levelTimeLeftMinutes,
                levelTimeLeftSeconds);
    }

}
