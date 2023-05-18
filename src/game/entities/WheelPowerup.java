package game.entities;

import game.Game;
import game.LevelScene;
import javafx.scene.image.Image;

public class WheelPowerup extends Powerup {

    public final static Image FRAMESET = new Image(
            Game.getAsset("pw_wheel.png"));

    public static final int ID = 2;

    public WheelPowerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setImage(FRAMESET);
    }

    @Override
    public void doPowerup(LevelScene scene) {
        scene.triggerSlowMobSpeed(POWERUP_TIMEOUT);
        scene.notifyPowerupConsumed(ID);
    }
    
}
