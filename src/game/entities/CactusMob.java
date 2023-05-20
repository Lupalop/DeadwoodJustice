package game.entities;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class CactusMob extends Mob {

    public static final Image FRAMESET_W = new Image(
            Game.getAsset("cactus_sheet_w.png"));

    public static final int FRAMESET_ROWS = 4;
    public static final int FRAMESET_COLUMNS = 11;
    public static final int[] FRAMESET_OFFSET =
            new int[] { 10, 12, 10, 1 };
    public static final int[] FRAME_RANGES =
            new int[] {
                    // Walking
                    11, 19,
                    // On impact
                    -1, -1,
                    // On being damaged
                    -1, -1,
                    // Death
                    33, 36,
                    // On shoot
                    22, 31
            };

    public CactusMob(int x, int y, LevelScene parent) {
        super(x, y, 1, -1, guessShooterAbility(), parent);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRanges(FRAME_RANGES);

        super.initialize();
    }

}
