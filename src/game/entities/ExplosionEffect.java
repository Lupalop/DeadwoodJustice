package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class ExplosionEffect extends Sprite {

    public final static Image FRAMESET_FX = new Image(
            Game.getAsset("fx_explode.png"));

    public final static int FRAMESET_ROWS = 1;
    public final static int FRAMESET_COLUMNS = 8;

    public ExplosionEffect(int xPos, int yPos) {
        super(xPos, yPos);

        this.setFrameSet(FRAMESET_FX, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setScale(2);
        this.setMinMaxFrame(0, 7);
        this.setFrameAutoReset(false);
    }

}
