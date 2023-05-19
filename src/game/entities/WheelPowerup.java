package game.entities;

import game.Game;
import game.LevelScene;
import javafx.scene.image.Image;

public class WheelPowerup extends Powerup {

    public static final int ID = 2;

    public static final Image FRAMESET = new Image(
            Game.getAsset("pw_wheel.png"));

    public WheelPowerup(int x, int y, LevelScene parent) {
        super(x, y, parent);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup() {
        this.getParent().applySlowMobSpeed(POWERUP_TIMEOUT);
        this.getParent().consumePowerup(ID);
    }

}
