package game.entities;

import java.util.concurrent.TimeUnit;

import game.Game;
import game.LevelScene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;

public class StatusHUD extends Sprite implements LevelUpdatable {

    private static final Tileset TILESET =
            new Tileset("tilemap_ui.png", 4, 8);

    private static final Image UI_GAME_END_BAD =
            new Image(Game.getAsset("ui_game_end_bad.png"));
    private static final Image UI_GAME_END_GOOD =
            new Image(Game.getAsset("ui_game_end_good.png"));
    private static final Image UI_STANDEE_PLAY =
            new Image(Game.getAsset("ui_game_end_standee_play.png"));
    private static final Image UI_STANDEE_EXIT =
            new Image(Game.getAsset("ui_game_end_standee_exit.png"));
    private static final int UI_HUD_MAX_NUM = 9999;

    private LevelScene level;
    private boolean isGameEndVisible;
    private Button playButton;
    private Button exitButton;

    public StatusHUD(LevelScene scene) {
        super(0, 0);

        this.level = scene;
        this.isGameEndVisible = false;
        this.playButton = null;
        this.exitButton = null;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();

        drawStatus(gc);
        if (isGameEndVisible) {
            drawGameEnd(gc);
        }

        gc.restore();
    }

    private void drawStatus(GraphicsContext gc) {
        gc.setFont(Game.FONT_32);
        gc.setFill(Paint.valueOf("EECA84"));

        int strength = level.getOutlaw().getStrength();
        String strengthText = Integer.toString(strength);
        int mobKillCount = level.getMobKillCount();
        String mobKillCountText = Integer.toString(mobKillCount);
        String timeLeftText = this.getLevelTimeLeftText();

        int powerupLampCount = level.getPowerupCount(LampPowerup.ID);
        String powerupLampCountText =
                Integer.toString(powerupLampCount);
        int powerupHayCount = level.getPowerupCount(HayPowerup.ID);
        String powerupHayCountText =
                Integer.toString(powerupHayCount);
        int powerupWheelCount = level.getPowerupCount(WheelPowerup.ID);
        String powerupWheelCountText =
                Integer.toString(powerupWheelCount);
        int powerupSnakeOilCount = level.getPowerupCount(SnakeOilPowerup.ID);
        String powerupSnakeOilCountText =
                Integer.toString(powerupSnakeOilCount);

        int tileSize = Tileset.TILE_SIZE_MID;
        TILESET.draw(gc, tileSize, 0, 3);
        for (int i = 0; i < 9; i++) {
            TILESET.draw(gc, tileSize * (i + 2), 0, 1);
        }
        TILESET.draw(gc, tileSize * 11, 0, 4);

        TILESET.draw(gc, tileSize * 8, 0, 8);
        if (level.getOutlaw().isImmortal() || strength > UI_HUD_MAX_NUM) {
            TILESET.draw(gc, tileSize * 3, -5, 14);
        } else {
            gc.fillText(strengthText, tileSize * 3, (32 / 2) + 3);
        }

        TILESET.draw(gc, tileSize * 5, 0, 12);
        gc.fillText(mobKillCountText, tileSize * 6, (32 / 2) + 3);

        TILESET.draw(gc, tileSize * 2, 0, 13);
        gc.fillText(timeLeftText, tileSize * 9, (32 / 2) + 3);

        //

        gc.save();
        if (powerupLampCount == 0) {
            gc.setGlobalAlpha(0.5);
        }
        TILESET.draw(gc, tileSize * 12, 0, 5);
        TILESET.draw(gc, tileSize * 12, 0, 9);
        TILESET.draw(gc, tileSize * 13, 0, 7);
        gc.fillText(powerupLampCountText, tileSize * 13, (32 / 2) + 3);
        gc.restore();

        gc.save();
        if (!level.getOutlaw().isImmortal()) {
            gc.setGlobalAlpha(0.5);
        }
        TILESET.draw(gc, tileSize * 15, 0, 5);
        TILESET.draw(gc, tileSize * 15, 0, 10);
        TILESET.draw(gc, tileSize * 16, 0, 7);
        gc.fillText(powerupHayCountText, tileSize * 16, (32 / 2) + 3);
        gc.restore();

        gc.save();
        if (!level.isSlowSpeed()) {
            gc.setGlobalAlpha(0.5);
        }
        TILESET.draw(gc, tileSize * 18, 0, 5);
        TILESET.draw(gc, tileSize * 18, 0, 11);
        TILESET.draw(gc, tileSize * 19, 0, 7);
        gc.fillText(powerupWheelCountText, tileSize * 19, (32 / 2) + 3);
        gc.restore();

        gc.save();
        if (!level.isZeroSpeed()) {
            gc.setGlobalAlpha(0.5);
        }
        TILESET.draw(gc, tileSize * 21, 0, 5);
        TILESET.draw(gc, tileSize * 21, 0, 15);
        TILESET.draw(gc, tileSize * 22, 0, 7);
        gc.fillText(powerupSnakeOilCountText, tileSize * 22, (32 / 2) + 3);
        gc.restore();
    }

    @Override
    public void update(long now, LevelScene level) {
        if (this.isGameEndVisible) {
            exitButton.update(now);
            playButton.update(now);
        } else if (level.isLevelDone()) {
            this.isGameEndVisible = true;
            this.addGameEndButtons();
        }
    }

    private void addGameEndButtons() {
        playButton = new Button(0, 0, level);
        playButton.setText("PLAY");
        playButton.setX((int) ((Game.WINDOW_MAX_WIDTH / 5) - playButton.getBounds().getWidth() / 2));
        playButton.setY((Game.WINDOW_MAX_HEIGHT / 2) + 50);
        playButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new LevelScene());
            }
        });

        exitButton = new Button(0, 0, level);
        exitButton.setText("EXIT");
        exitButton.setX((int) ((Game.WINDOW_MAX_WIDTH) - (Game.WINDOW_MAX_WIDTH / 5) - exitButton.getBounds().getWidth() / 2));
        exitButton.setY((Game.WINDOW_MAX_HEIGHT / 2) + 50);
        exitButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        });
    }

    private void drawGameEnd(GraphicsContext gc) {
        int base = 6;
        int tileSize = Tileset.TILE_SIZE_MID;
        for (int i = 0; i < (Game.WINDOW_MAX_WIDTH) / tileSize; i++) {
            TILESET.draw(gc, tileSize * i, tileSize * base, 16);
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < (Game.WINDOW_MAX_WIDTH) / tileSize; j++) {
                TILESET.draw(gc, tileSize * j, tileSize * (base + 1 + i), 18);
            }
        }

        for (int i = 0; i < (Game.WINDOW_MAX_WIDTH) / tileSize; i++) {
            TILESET.draw(gc, tileSize * i, tileSize * (base + 6), 17);
        }

        Image gameEndCenterImage = null;
        if (!level.getOutlaw().isAlive()) {
            gameEndCenterImage = UI_GAME_END_BAD;
        } else {
            gameEndCenterImage = UI_GAME_END_GOOD;
        }
        gc.drawImage(
                gameEndCenterImage,
                (Game.WINDOW_MAX_WIDTH / 2) - gameEndCenterImage.getWidth() / 2,
                (Game.WINDOW_MAX_HEIGHT / 2) - gameEndCenterImage.getHeight() / 2);

        Image standeePlayImage = UI_STANDEE_PLAY;
        gc.drawImage(
                standeePlayImage,
                (Game.WINDOW_MAX_WIDTH / 5) - standeePlayImage.getWidth() / 2,
                (Game.WINDOW_MAX_HEIGHT / 2) - standeePlayImage.getHeight() / 2);

        Image standeeExitImage = UI_STANDEE_EXIT;
        gc.drawImage(
                standeeExitImage,
                (Game.WINDOW_MAX_WIDTH) - (Game.WINDOW_MAX_WIDTH / 5) - standeeExitImage.getWidth() / 2,
                (Game.WINDOW_MAX_HEIGHT / 2) - standeeExitImage.getHeight() / 2);

        playButton.draw(gc);
        exitButton.draw(gc);
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
