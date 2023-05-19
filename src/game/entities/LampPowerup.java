package game.entities;

import game.Game;
import game.LevelScene;
import javafx.scene.image.Image;

public class LampPowerup extends Powerup {

    public static final int ID = 0;

    public static final Image FRAMESET = new Image(
            Game.getAsset("pw_lamp.png"));

    public LampPowerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup(LevelScene scene) {
        scene.getOutlaw().increaseStrength(scene.getOutlaw().getStrength());
        scene.consumePowerup(ID);
    }

}
