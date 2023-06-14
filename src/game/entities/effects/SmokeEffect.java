package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

public final class SmokeEffect extends Effect {

    private static final Image FRAMESET = new Image(
            Game.getAsset("fx_smoke.png"));

    private static final int FRAMESET_ROWS = 1;
    private static final int FRAMESET_COLUMNS = 6;

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
