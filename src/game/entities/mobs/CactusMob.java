package game.entities.mobs;

import game.Game;
import game.entities.FrameRange;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class CactusMob extends Mob {

    public static final Image FRAMESET_W = new Image(
            Game.getAsset("cactus_sheet_w.png"));

    public static final int FRAMESET_ROWS = 4;
    public static final int FRAMESET_COLUMNS = 11;
    public static final int[] FRAMESET_OFFSET =
            new int[] { 10, 12, 10, 1 };
    public static final FrameRange FRAME_RANGE =
            new FrameRange(11, 19,
                    -1, -1,
                    -1, -1,
                    22, 31,
                    33, 36,
                    -1, -1);

    public CactusMob(int x, int y, LevelScene parent) {
        super(x, y, 1, -1, parent);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRange(FRAME_RANGE);

        super.initialize();
    }

}
