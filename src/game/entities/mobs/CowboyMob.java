package game.entities.mobs;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class CowboyMob extends Mob {

    public static final Image FRAMESET_W = new Image(
            Game.getAsset("cowboy_sheet_w.png"));

    public static final int FRAMESET_ROWS = 4;
    public static final int FRAMESET_COLUMNS = 8;
    public static final int[] FRAMESET_OFFSET =
            new int[] { 15, 14, 7, 0 };
    public static final int[] FRAME_RANGES =
            new int[] {
                    // Walking
                    0, 7,
                    // On impact
                    8, 12,
                    // On being damaged
                    16, 21,
                    // Death
                    24, 27,
                    // On shoot
                    16, 21
            };

    private static final int MOB_SCALE = 3;

    public CowboyMob(int x, int y, LevelScene parent) {
        super(x, y, 3000, 50, parent);

        this.setDeadOnPlayerImpact(false);
        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRanges(FRAME_RANGES);

        super.initialize();
        this.setScale(MOB_SCALE);

        if (this.isMovingRight()) {
            this.changeDirection();
        }

        this.excludedFromMaxSpeed = true;
        this.setChasingPlayer(true);
    }

    @Override
    protected boolean getShootingCapability() {
        return true;
    }

}
