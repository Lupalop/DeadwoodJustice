package game.entities.powerups;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

/**
 * The WheelPowerup class represents the slow enemies speed power-up.
 * @author Francis Dominic Fajardo
 */
public final class WheelPowerup extends Powerup {

    public static final int ID = 2;

    private static final Image FRAMESET = new Image(
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
