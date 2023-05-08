package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class CoffinMob extends Mob {

    public final static Image FRAMESET_W = new Image(
            Game.getAsset("coffin_sheet_w.png"));

    public final static int FRAMESET_ROWS = 4;
    public final static int FRAMESET_COLUMNS = 18;
    public final static int[] FRAMESET_OFFSET = 
            new int[] { 9, 35, 14, 17 };
    public final static int[] FRAME_RANGES =
            new int[] {
                    // Walking
                    19, 32,
                    // Death
                    54, 57
            };
    
    public CoffinMob(int x, int y) {
        super(x, y);

        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRanges(FRAME_RANGES);
        
        super.initialize();
    }

}
