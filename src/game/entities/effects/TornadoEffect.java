package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

/**
 * This class represents the effect applied on mobs when the
 * player collects a SnakeOilPowerup.
 * @author Francis Dominic Fajardo
 */
public final class TornadoEffect extends Effect {

    /** Frame set: effect. */
    private static final Image FRAMESET = new Image(
            Game.getAsset("fx_tornado.png"));
    /** Frame set: rows. */
    private static final int FRAMESET_ROWS = 1;
    /** Frame set: columns. */
    private static final int FRAMESET_COLUMNS = 10;

    /**
     * Constructs an instance of TornadoEffect.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     */
    public TornadoEffect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    /**
     * Constructs an instance of TornadoEffect that follows the
     * specified Sprite.
     * @param spriteTarget the sprite to be followed.
     */
    public TornadoEffect(Sprite spriteTarget) {
        super(spriteTarget);
    }

    @Override
    public void initialize() {
        this.setFrameAutoReset(false);
        this.setFrameSet(FRAMESET, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 9);
    }

}
