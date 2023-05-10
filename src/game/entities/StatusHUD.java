package game.entities;

import game.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class StatusHUD extends Sprite {

    private Tileset tileset;
    
    public StatusHUD() {
        super(0, 0);
        this.tileset = new Tileset("tilemap_ui.png", 2, 8);
        this.tileset.setScale(2);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFont(Game.FONT_32);
        
        this.tileset.draw(gc, 32, 0, 3);
        for (int i = 0; i < 9; i++) {
            this.tileset.draw(gc, 32 * (i + 2), 0, 1);
        }
        this.tileset.draw(gc, (32 * 11), 0, 4);
        
        this.tileset.draw(gc, 32 * 2, 0, 13);
        gc.setFill(Paint.valueOf("EECA84"));
        gc.fillText("2000", 32 * 3, (32 / 2) + 3);

        this.tileset.draw(gc, 32 * 5, 0, 12);
        gc.setFill(Paint.valueOf("EECA84"));
        gc.fillText("2000", 32 * 6, (32 / 2) + 3);

        this.tileset.draw(gc, (32 * 8), 0, 8);
        gc.setFill(Paint.valueOf("EECA84"));
        gc.fillText("60 s", (32 * 9), (32 / 2) + 3);
        
        //
        
        this.tileset.draw(gc, 32 * 12, 0, 5);
        this.tileset.draw(gc, 32 * 12, 0, 9);
        this.tileset.draw(gc, 32 * 13, 0, 7);

        this.tileset.draw(gc, 32 * 15, 0, 5);
        this.tileset.draw(gc, 32 * 15, 0, 10);
        this.tileset.draw(gc, 32 * 16, 0, 7);

        this.tileset.draw(gc, 32 * 18, 0, 5);
        this.tileset.draw(gc, 32 * 18, 0, 11);
        this.tileset.draw(gc, 32 * 19, 0, 7);

        this.tileset.draw(gc, 32 * 21, 0, 5);
        this.tileset.draw(gc, 32 * 22, 0, 7);
    }
}
