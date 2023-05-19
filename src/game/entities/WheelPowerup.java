package game.entities;

import game.Game;
import game.LevelScene;
import javafx.scene.image.Image;

public class WheelPowerup extends Powerup {

    public static final int ID = 2;

    public static final Image FRAMESET = new Image(
            Game.getAsset("pw_wheel.png"));

    public WheelPowerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup(LevelScene scene) {
        scene.applySlowMobSpeed(POWERUP_TIMEOUT);
        scene.consumePowerup(ID);
    }

}
