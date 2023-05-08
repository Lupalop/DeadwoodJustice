package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class CoyoteMob extends Mob {

    public final static Image FRAMESET_W = new Image(
            Game.getAsset("coyote_sheet_w.png"));

    public final static int FRAMESET_ROWS = 4;
    public final static int FRAMESET_COLUMNS = 24;
    public final static int[] FRAMESET_OFFSET = 
            new int[] { 11, 33, 16, 21 };
    public final static int[] FRAME_RANGES =
            new int[] {
                    // Walking
                    24, 37,
                    // Death
                    71, 74
            };
    
    public CoyoteMob(int x, int y) {
        super(x, y);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRanges(FRAME_RANGES);
        
        super.initialize();
    }

}
