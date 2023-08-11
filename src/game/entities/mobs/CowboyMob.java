package game.entities.mobs;

import game.Game;
import game.entities.FrameRange;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

/**
 * This class represents a Cowboy, the boss enemy.
 * @author Francis Dominic Fajardo
 */
public final class CowboyMob extends Mob {

    /** Frame set: mob. */
    private static final Image FRAMESET_W = new Image(
            Game.getAsset("cowboy_sheet_w.png"));
    /** Frame set: rows. */
    private static final int FRAMESET_ROWS = 4;
    /** Frame set: columns. */
    private static final int FRAMESET_COLUMNS = 8;
    /** Frame set: offset from sides. */
    private static final int[] FRAMESET_OFFSET =
            new int[] { 15, 14, 7, 0 };
    /** Frame set: range. */
    private static final FrameRange FRAME_RANGE =
            new FrameRange(0, 7,
                    8, 12,
                    16, 17,
                    8, 12,
                    24, 27,
                    -1, -1);
    /**
     * Frame set: base scale for the image.
     * We do this to differentiate the boss from other kinds
     * of enemies. It does look absurd, but it is what it is.
     */
    private static final int BASE_SCALE = 3;

    /**
     * Constructs an instance of CowboyMob.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param parent the LevelScene object owning this entity.
     */
    public CowboyMob(int x, int y, LevelScene parent) {
        super(x, y, 3000, 50, parent);

        this.setDeadOnPlayerImpact(false);
        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRange(FRAME_RANGE);

        super.initialize();
        this.setScale(BASE_SCALE);

        if (this.isMovingRight()) {
            this.changeDirection();
        }

        this.setChasingPlayer(!parent.getRestrictedMode());
    }

    @Override
    protected boolean getShootingCapability() {
        // The boss is always allowed to shoot.
        return true;
    }

}
