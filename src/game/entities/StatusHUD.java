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

        TILESET.draw(gc, TILE_SIZE, 0, 3);
        for (int i = 0; i < 9; i++) {
            TILESET.draw(gc, TILE_SIZE * (i + 2), 0, 1);
        }
        TILESET.draw(gc, TILE_SIZE * 11, 0, 4);
        
        TILESET.draw(gc, TILE_SIZE * 2, 0, 13);
        gc.fillText(strength, TILE_SIZE * 3, (32 / 2) + 3);

        TILESET.draw(gc, TILE_SIZE * 5, 0, 12);
        gc.fillText("2000", TILE_SIZE * 6, (32 / 2) + 3);

        TILESET.draw(gc, TILE_SIZE * 8, 0, 8);
        gc.fillText("60 s", TILE_SIZE * 9, (32 / 2) + 3);
        
        //
        
        TILESET.draw(gc, TILE_SIZE * 12, 0, 5);
        TILESET.draw(gc, TILE_SIZE * 12, 0, 9);
        TILESET.draw(gc, TILE_SIZE * 13, 0, 7);

        TILESET.draw(gc, TILE_SIZE * 15, 0, 5);
        TILESET.draw(gc, TILE_SIZE * 15, 0, 10);
        TILESET.draw(gc, TILE_SIZE * 16, 0, 7);

        TILESET.draw(gc, TILE_SIZE * 18, 0, 5);
        TILESET.draw(gc, TILE_SIZE * 18, 0, 11);
        TILESET.draw(gc, TILE_SIZE * 19, 0, 7);

        TILESET.draw(gc, TILE_SIZE * 21, 0, 5);
        TILESET.draw(gc, TILE_SIZE * 22, 0, 7);
    }
}
