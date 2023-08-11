package game.entities.powerups;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

/**
 * This class represents the immortality power-up.
 * @author Francis Dominic Fajardo
 */
public final class HayPowerup extends Powerup {

    /** Power-up ID. */
    public static final int ID = 1;
    /** Frame set: power-up. */
    private static final Image FRAMESET = new Image(
            Game.getAsset("pw_hay.png"));

    /**
     * Constructs an instance of HayPowerup.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param parent the LevelScene object owning this entity.
     */
    public HayPowerup(int x, int y, LevelScene parent) {
        super(x, y, parent);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup() {
        this.getParent().applyImmortality(POWERUP_TIMEOUT);
        this.getParent().trackCollectedPowerup(ID);
    }

}
