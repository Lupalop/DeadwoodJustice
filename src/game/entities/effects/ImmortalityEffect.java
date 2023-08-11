package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

/**
 * This class represents the effect applied on the player sprite
 * to illustrate the immortality state, which is triggered by a power-up.
 * @author Francis Dominic Fajardo
 */
public final class ImmortalityEffect extends Effect {

    /** Frame set: effect. */
    private static final Image FRAMESET = new Image(
            Game.getAsset("fx_portal.png"));
    /** Frame set: rows. */
    private static final int FRAMESET_ROWS = 1;
    /** Frame set: columns. */
    private static final int FRAMESET_COLUMNS = 10;

    /**
     * Constructs an instance of ImmortalityEffect.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     */
    public ImmortalityEffect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    /**
     * Constructs an instance of ImmortalityEffect that follows
     * the specified Sprite.
     * @param spriteTarget the sprite to be followed.
     */
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
