package game.entities.mobs;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class CoyoteMob extends Mob {

    public static final Image FRAMESET_W = new Image(
            Game.getAsset("coyote_sheet_w.png"));

    public static final int FRAMESET_ROWS = 4;
    public static final int FRAMESET_COLUMNS = 24;
    public static final int[] FRAMESET_OFFSET =
            new int[] { 11, 33, 16, 21 };
    public static final int[] FRAME_RANGES =
            new int[] {
                    // Walking
                    24, 37,
                    // On impact
                    -1, -1,
                    // On being damaged
                    -1, -1,
                    // Death
                    71, 74,
                    // On shoot
                    57, 73
            };

    public CoyoteMob(int x, int y, LevelScene parent) {
        super(x, y, 1, -1, guessShooterAbility(), parent);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRanges(FRAME_RANGES);

        super.initialize();
    }

}
