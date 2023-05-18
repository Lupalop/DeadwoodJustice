package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class SmokeEffect extends Effect {

    public final static Image FRAMESET_FX = new Image(
            Game.getAsset("fx_smoke.png"));

    public final static int FRAMESET_ROWS = 1;
    public final static int FRAMESET_COLUMNS = 6;

    public SmokeEffect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public SmokeEffect(Sprite spriteTarget) {
        super(spriteTarget);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.setFrameSet(FRAMESET_FX, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 5);
    }
    
}
