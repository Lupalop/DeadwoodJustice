package game.entities.mobs;

import game.Game;
import game.entities.FrameRange;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class CowboyMob extends Mob {

    private static final Image FRAMESET_W = new Image(
            Game.getAsset("cowboy_sheet_w.png"));

    private static final int FRAMESET_ROWS = 4;
    private static final int FRAMESET_COLUMNS = 8;
    private static final int[] FRAMESET_OFFSET =
            new int[] { 15, 14, 7, 0 };
    private static final FrameRange FRAME_RANGE =
            new FrameRange(0, 7,
                    8, 12,
                    16, 21,
                    8, 12,
                    24, 27,
                    -1, -1);

    private static final int BASE_SCALE = 3;

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
        return true;
    }

}
