package game.entities;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class SnakeOilPowerup extends Powerup {

    public final static Image FRAMESET = new Image(
            Game.getAsset("pw_snakeoil.png"));

    public static final int ID = 3;

    public SnakeOilPowerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setImage(FRAMESET);
    }

    @Override
    public void doPowerup(LevelScene scene) {
        scene.triggerZeroMobSpeed(POWERUP_TIMEOUT);
        scene.notifyPowerupConsumed(ID);
    }
    
}
