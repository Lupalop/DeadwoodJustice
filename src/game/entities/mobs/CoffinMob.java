package game.entities.mobs;

import game.Game;
import game.entities.FrameRange;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class CoffinMob extends Mob {

    public static final Image FRAMESET_W = new Image(
            Game.getAsset("coffin_sheet_w.png"));

    public static final int FRAMESET_ROWS = 4;
    public static final int FRAMESET_COLUMNS = 18;
    public static final int[] FRAMESET_OFFSET =
            new int[] { 9, 35, 14, 17 };
    public static final FrameRange FRAME_RANGE =
            new FrameRange(19, 32,
                    -1, -1,
                    -1, -1,
                    37, 53,
                    54, 57,
                    -1, -1);

    public CoffinMob(int x, int y, LevelScene parent) {
        super(x, y, 1, -1, parent);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRange(FRAME_RANGE);

        super.initialize();
    }

}
