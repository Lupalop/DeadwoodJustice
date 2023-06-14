package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

/**
 * The ImmortalityEffect class is used on illustrating the player's
 * immortality state when triggered by a power-up.
 * @author Francis Dominic Fajardo
 */
public final class ImmortalityEffect extends Effect {

    private static final Image FRAMESET = new Image(
            Game.getAsset("fx_portal.png"));

    private static final int FRAMESET_ROWS = 1;
    private static final int FRAMESET_COLUMNS = 10;

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
