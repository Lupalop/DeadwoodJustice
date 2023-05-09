package game.entities;

import java.util.ArrayList;

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
        this.setScale(3);
        
        if (this.moveRight) {
            this.changeDirection();
        }
    }

    @Override
    public void update(long currentNanoTime, Outlaw outlaw, ArrayList<Mob> otherMobs, boolean isMaxSpeed) {
        super.update(currentNanoTime, outlaw, otherMobs, isMaxSpeed);

        if (!this.isAlive()) {
            return;
        }

        if (!outlaw.isAlive()) {
            return;
        }
        
        if (!Game.DEBUG_MODE) {
            return;
        }
        
        int outlawHalfSpeed = Outlaw.OUTLAW_SPEED / 2; 
        if (outlaw.getY() > this.getY()) {
            this.dy = outlawHalfSpeed;
        } else {
            this.dy = -outlawHalfSpeed;
        }
    }
    
}
