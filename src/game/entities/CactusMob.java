package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class CactusMob extends Mob {

    public final static Image FRAMESET_W = new Image(
            Game.getAsset("cactus_sheet_w.png"));

    public final static int FRAMESET_ROWS = 4;
    public final static int FRAMESET_COLUMNS = 11;
    public final static int[] FRAMESET_OFFSET = 
            new int[] { 10, 12, 10, 1 };
    public final static int[] FRAME_RANGES =
            new int[] {
                    // Walking
                    11, 19,
                    // Death
                    33, 36
            };
    
    public CactusMob(int x, int y) {
        super(x, y, 1);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRanges(FRAME_RANGES);
        
        super.initialize();
    }

}
