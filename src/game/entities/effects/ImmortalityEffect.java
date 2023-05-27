package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

public class ImmortalityEffect extends Effect {

    public static final Image FRAMESET = new Image(
            Game.getAsset("fx_portal.png"));

    public static final int FRAMESET_ROWS = 1;
    public static final int FRAMESET_COLUMNS = 10;

    public ImmortalityEffect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public ImmortalityEffect(Sprite spriteTarget) {
        super(spriteTarget);
    }

    @Override
    public void initialize() {
        this.setFrameSet(FRAMESET, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 4);
        this.playFrames(5, 9, null, 0);
    }

}
