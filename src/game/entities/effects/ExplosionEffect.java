package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

/**
 * The ExplosionEffect class is used on dying mobs.
 * @author Francis Dominic Fajardo
 */
public final class ExplosionEffect extends Effect {

    private static final Image FRAMESET = new Image(
            Game.getAsset("fx_explode.png"));

    private static final int FRAMESET_ROWS = 1;
    private static final int FRAMESET_COLUMNS = 8;

    public ExplosionEffect(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public ExplosionEffect(Sprite spriteTarget) {
        super(spriteTarget);
    }

    @Override
    public void initialize() {
        this.setFrameAutoReset(false);
        this.setFrameSet(FRAMESET, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setMinMaxFrame(0, 7);
    }

}
