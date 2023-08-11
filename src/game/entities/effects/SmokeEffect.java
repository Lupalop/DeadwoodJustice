package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

/**
 * This class represents the effect applied on a sprite whenever
 * a Powerup is collected.
 * @author Francis Dominic Fajardo
 */
public final class SmokeEffect extends Effect {

    /** Frame set: effect. */
    private static final Image FRAMESET = new Image(
            Game.getAsset("fx_smoke.png"));
    /** Frame set: rows. */
    private static final int FRAMESET_ROWS = 1;
    /** Frame set: columns. */
    private static final int FRAMESET_COLUMNS = 6;

    /**
     * Constructs an instance of SmokeEffect.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     */
    public SmokeEffect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    /**
     * Constructs an instance of SmokeEffect that follows the
     * specified Sprite.
     * @param spriteTarget the sprite to be followed.
     */
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
