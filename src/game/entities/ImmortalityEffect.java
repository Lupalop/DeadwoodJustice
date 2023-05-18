package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class ImmortalityEffect extends Effect {

    public final static Image FRAMESET_FX = new Image(
            Game.getAsset("fx_portal.png"));

    public final static int FRAMESET_ROWS = 1;
    public final static int FRAMESET_COLUMNS = 10;

    public ImmortalityEffect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public ImmortalityEffect(Sprite spriteTarget) {
        super(spriteTarget);
    }

    @Override
    public void initialize() {
        this.setFrameSet(FRAMESET_FX, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 4);
        this.playFrames(5, 9, null, 0);
    }

}
