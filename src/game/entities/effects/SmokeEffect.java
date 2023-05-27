package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

public class SmokeEffect extends Effect {

    public static final Image FRAMESET = new Image(
            Game.getAsset("fx_smoke.png"));

    public static final int FRAMESET_ROWS = 1;
    public static final int FRAMESET_COLUMNS = 6;

    public SmokeEffect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public SmokeEffect(Sprite spriteTarget) {
        super(spriteTarget);
    }

    @Override
    public void initialize() {
        this.setFrameAutoReset(false);
        this.setFrameSet(FRAMESET, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 5);
    }

}
