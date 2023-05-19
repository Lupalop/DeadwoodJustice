package game.entities;

import game.Game;
import game.LevelScene;
import javafx.scene.image.Image;

public class HayPowerup extends Powerup {

    public static final int ID = 1;

    public static final Image FRAMESET = new Image(
            Game.getAsset("pw_hay.png"));

    public HayPowerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup(LevelScene scene) {
        scene.getOutlaw().applyImmortality(POWERUP_TIMEOUT);
        scene.consumePowerup(ID);
    }

}
