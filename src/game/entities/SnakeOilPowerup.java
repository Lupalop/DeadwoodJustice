package game.entities;

import game.Game;
import game.LevelScene;
import javafx.scene.image.Image;

public class SnakeOilPowerup extends Powerup {

    public static final int ID = 3;

    public static final Image FRAMESET = new Image(
            Game.getAsset("pw_snakeoil.png"));

    public SnakeOilPowerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup(LevelScene scene) {
        scene.applyZeroMobSpeed(POWERUP_TIMEOUT);
        scene.consumePowerup(ID);
    }

}
