package game.entities;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class StatusHUD extends Sprite {

    private static Tileset TILESET =
            new Tileset("tilemap_ui.png", 2, 8);
    private static int TILE_SIZE = 32;
    private LevelScene level;
    
    public StatusHUD(LevelScene scene) {
        super(0, 0);
        TILESET.setScale(2);
        this.level = scene;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFont(Game.FONT_32);
        gc.setFill(Paint.valueOf("EECA84"));

        String strength = Integer.toString(level.getOutlaw().getStrength());
        if (level.getOutlaw().isImmortal()) {
            strength = "9999";
        }
        String mobKillCount = Integer.toString(level.getMobKillCount());
        String timeLeft = level.getTimeLeftDisplayString();
        String powerupLampCount =
                Integer.toString(level.getPowerupCount(LampPowerup.ID));
        String powerupHayCount =
                Integer.toString(level.getPowerupCount(HayPowerup.ID));
        String powerupWheelCount =
                Integer.toString(level.getPowerupCount(WheelPowerup.ID));

        TILESET.draw(gc, TILE_SIZE, 0, 3);
        for (int i = 0; i < 9; i++) {
            TILESET.draw(gc, TILE_SIZE * (i + 2), 0, 1);
        }
        TILESET.draw(gc, TILE_SIZE * 11, 0, 4);
        
        TILESET.draw(gc, TILE_SIZE * 8, 0, 8);
        gc.fillText(strength, TILE_SIZE * 3, (32 / 2) + 3);

        TILESET.draw(gc, TILE_SIZE * 5, 0, 12);
        gc.fillText(mobKillCount, TILE_SIZE * 6, (32 / 2) + 3);

        TILESET.draw(gc, TILE_SIZE * 2, 0, 13);
        gc.fillText(timeLeft, TILE_SIZE * 9, (32 / 2) + 3);
        
        //
        
        gc.save();
        if (powerupLampCount.equals("0")) {
            gc.setGlobalAlpha(0.5);
        }
        TILESET.draw(gc, TILE_SIZE * 12, 0, 5);
        TILESET.draw(gc, TILE_SIZE * 12, 0, 9);
        TILESET.draw(gc, TILE_SIZE * 13, 0, 7);
        gc.fillText(powerupLampCount, TILE_SIZE * 13, (32 / 2) + 3);
        gc.restore();

        gc.save();
        if (!level.getOutlaw().isImmortal()) {
            gc.setGlobalAlpha(0.5);
        }
        TILESET.draw(gc, TILE_SIZE * 15, 0, 5);
        TILESET.draw(gc, TILE_SIZE * 15, 0, 10);
        TILESET.draw(gc, TILE_SIZE * 16, 0, 7);
        gc.fillText(powerupHayCount, TILE_SIZE * 16, (32 / 2) + 3);
        gc.restore();

        gc.save();
        if (!level.isSlowSpeed()) {
            gc.setGlobalAlpha(0.5);
        }
        TILESET.draw(gc, TILE_SIZE * 18, 0, 5);
        TILESET.draw(gc, TILE_SIZE * 18, 0, 11);
        TILESET.draw(gc, TILE_SIZE * 19, 0, 7);
        gc.fillText(powerupWheelCount, TILE_SIZE * 19, (32 / 2) + 3);
        gc.restore();

        gc.save();
        gc.setGlobalAlpha(0.5);
        TILESET.draw(gc, TILE_SIZE * 21, 0, 5);
        TILESET.draw(gc, TILE_SIZE * 22, 0, 7);
        gc.restore();
    }
}
