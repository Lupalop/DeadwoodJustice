package game.entities.effects;

import game.Game;
import game.entities.Sprite;
import javafx.scene.image.Image;

/**
 * The TornadoEffect class is used on mobs when the freeze speed power-up
 * is applied and used by the player.
 * @author Francis Dominic Fajardo
 *
 */
public final class TornadoEffect extends Effect {

    private static final Image FRAMESET = new Image(
            Game.getAsset("fx_tornado.png"));

    private static final int FRAMESET_ROWS = 1;
    private static final int FRAMESET_COLUMNS = 10;

    public TornadoEffect(int xPos, int yPos) {
        super(xPos, yPos);
    }

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
