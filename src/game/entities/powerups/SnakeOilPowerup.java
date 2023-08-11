package game.entities.powerups;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

/**
 * This class represents the freeze all mobs power-up.
 * @author Francis Dominic Fajardo
 */
public final class SnakeOilPowerup extends Powerup {

    /** Power-up ID. */
    public static final int ID = 3;
    /** Frame set: power-up. */
    private static final Image FRAMESET = new Image(
            Game.getAsset("pw_snakeoil.png"));

    /**
     * Constructs an instance of SnakeOilPowerup.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param parent the LevelScene object owning this entity.
     */
    public SnakeOilPowerup(int x, int y, LevelScene parent) {
        super(x, y, parent);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup() {
        this.getParent().applyZeroMobSpeed(POWERUP_TIMEOUT);
        this.getParent().trackCollectedPowerup(ID);
    }

}
