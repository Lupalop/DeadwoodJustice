package game.entities;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class HayPowerup extends Powerup {

    public final static Image FRAMESET = new Image(
            Game.getAsset("pw_hay.png"));

    public static final int ID = 1;

    public HayPowerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setImage(FRAMESET);
    }

    @Override
    public void doPowerup(LevelScene scene) {
        scene.getOutlaw().triggerImmortality(POWERUP_TIMEOUT);
        scene.notifyPowerupConsumed(ID);
    }
    
}
