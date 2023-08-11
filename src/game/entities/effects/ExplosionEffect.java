package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

/**
 * This class represents the effect applied on explosions (e.g., dying mobs).
 * @author Francis Dominic Fajardo
 */
public final class ExplosionEffect extends Effect {

    /** Frame set: effect. */
    private static final Image FRAMESET = new Image(
            Game.getAsset("fx_explode.png"));
    /** Frame set: rows. */
    private static final int FRAMESET_ROWS = 1;
    /** Frame set: columns. */
    private static final int FRAMESET_COLUMNS = 8;

    /**
     * Constructs an instance of ExplosionEffect.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     */
    public ExplosionEffect(int x, int y) {
        super(x, y);
    }

    /**
     * Constructs an instance of ExplosionEffect that follows
     * the specified Sprite.
     * @param spriteTarget the sprite to be followed.
     */
    public ExplosionEffect(Sprite spriteTarget) {
        super(spriteTarget);
    }

    @Override
    protected void initialize() {
        this.setFrameAutoReset(false);
        this.setFrameSet(FRAMESET, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 7);
    }

}
