package game.entities;

import game.Game;
import javafx.scene.image.Image;

public class CowboyMob extends Mob {

    public final static Image FRAMESET_W = new Image(
            Game.getAsset("cowboy_sheet_w.png"));

    public final static int FRAMESET_ROWS = 4;
    public final static int FRAMESET_COLUMNS = 8;
    public final static int[] FRAMESET_OFFSET = 
            new int[] { 15, 14, 7, 0 };
    public final static int[] FRAME_RANGES =
            new int[] {
                    // Walking
                    0, 7,
                    // On impact
                    8, 12,
                    // On being damaged
                    16, 21,
                    // Death
                    24, 27
            };
    
    public CowboyMob(int x, int y) {
        super(x, y, 3000, 50);
        
        this.setIsDeadOnPlayerImpact(false);
        this.setFrameSet(FRAMESET_W, FRAMESET_ROWS, FRAMESET_COLUMNS);
        this.setBoundsOffset(FRAMESET_OFFSET);
        this.setFrameRanges(FRAME_RANGES);
        
        super.initialize();
        
        if (this.moveRight) {
            this.changeDirection();
        }
    }

}
