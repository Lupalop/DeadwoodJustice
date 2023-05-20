package game.entities;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.LevelScene;
import game.MainMenuScene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;

public class StatusHUD extends Sprite {

    private static final Tile TILE =
            new Tile("tilemap_ui.png", 4, 8);

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

    private static final Paint HUD_TEXT_COLOR = Paint.valueOf("EECA84");
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
    private boolean isGameEndVisible;
    private int hudOffsetY;
    private Button playButton;
    private Button exitButton;

    public StatusHUD(LevelScene scene) {
        super(0, 0);

        this.level = scene;
        this.isGameEndVisible = false;
        this.hudOffsetY = -32;
        this.playButton = null;
        this.exitButton = null;

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
    }

    @Override
    public void update(long now) {
        if (this.isGameEndVisible) {
            exitButton.update(now);
            playButton.update(now);
        } else if (level.isLevelDone()) {
            this.isGameEndVisible = true;
            this.addGameEndButtons();
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.setFont(Game.FONT_32);
        gc.setFill(HUD_TEXT_COLOR);

        drawHUD(gc);
        if (isGameEndVisible) {
            drawGameEnd(gc);
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

    private void drawGameEnd(GraphicsContext gc) {
        int base = Tile.ALL_VERTICAL / 3;
        for (int i = 0; i < (Game.WINDOW_MAX_WIDTH) / Tile.SIZE_MID; i++) {
            TILE.draw(gc, Tile.SIZE_MID * i,
                    Tile.SIZE_MID * base, TX_POP_START);
        }

        for (int i = 0; i < 5; i++) {
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

    private void addGameEndButtons() {
        playButton = new Button(0, 0, 2, level);
        playButton.setText("PLAY");
        playButton.setX((int) ((Game.WINDOW_MAX_WIDTH / 5) - playButton.getBounds().getWidth() / 2));
        playButton.setY((Game.WINDOW_MAX_HEIGHT / 2) + 50);
        playButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new LevelScene());
            }
        });

        exitButton = new Button(0, 0, 2, level);
        exitButton.setText("EXIT");
        exitButton.setX((int) ((Game.WINDOW_MAX_WIDTH) - (Game.WINDOW_MAX_WIDTH / 5) - exitButton.getBounds().getWidth() / 2));
        exitButton.setY((Game.WINDOW_MAX_HEIGHT / 2) + 50);
        exitButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new MainMenuScene());
            }
        });
    }

    private String getLevelTimeLeftText() {
        String formatString = "%s:%s";
        if (level.getLevelTimeLeft() < 0) {
            return String.format(formatString, "0", "00");
        }
        long levelTimeLeftMinutes =
                TimeUnit.SECONDS.toMinutes(level.getLevelTimeLeft());
        long levelTimeLeftSeconds =
                level.getLevelTimeLeft() - (60 * levelTimeLeftMinutes);

        if (levelTimeLeftSeconds <= 9) {
            formatString = "%s:0%s";
        }

        return String.format(
                formatString,
                levelTimeLeftMinutes,
                levelTimeLeftSeconds);
    }

}
