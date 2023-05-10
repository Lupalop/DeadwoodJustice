package game.entities;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class WheelPowerup extends Powerup {

    public final static Image FRAMESET = new Image(
            Game.getAsset("pw_wheel.png"));


    public WheelPowerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setImage(FRAMESET);
    }

    @Override
    public void doPowerup(LevelScene scene) {
        scene.triggerSlowMobSpeed(POWERUP_TIMEOUT);
    }
    
}
