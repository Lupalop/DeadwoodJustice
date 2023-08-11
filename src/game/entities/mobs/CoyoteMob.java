package game.entities.mobs;

import game.Game;
import game.entities.FrameRange;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

/**
 * This class represents a Coyote enemy.
 * @author Francis Dominic Fajardo
 */
public final class CoyoteMob extends Mob {

    /** Frame set: mob. */
    private static final Image FRAMESET_W = new Image(
            Game.getAsset("coyote_sheet_w.png"));
    /** Frame set: rows. */
    private static final int FRAMESET_ROWS = 4;
    /** Frame set: columns. */
    private static final int FRAMESET_COLUMNS = 24;
    /** Frame set: offset from sides. */
    private static final int[] FRAMESET_OFFSET =
            new int[] { 11, 33, 16, 21 };
    /** Frame set: range. */
    private static final FrameRange FRAME_RANGE =
            new FrameRange(24, 37,
                    -1, -1,
                    -1, -1,
                    57, 72,
                    71, 74,
                    -1, -1);

    /**
     * Constructs an instance of CoyoteMob.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param parent the LevelScene object owning this entity.
     */
    public CoyoteMob(int x, int y, LevelScene parent) {
        super(x, y, 1, -1, parent);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRange(FRAME_RANGE);

        super.initialize();
    }

}
