package game.entities.powerups;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

/**
 * This class represents the double-life power-up.
 * @author Francis Dominic Fajardo
 */
public final class LampPowerup extends Powerup {

    /** Power-up ID. */
    public static final int ID = 0;
    /** Frame set: power-up. */
    private static final Image FRAMESET = new Image(
            Game.getAsset("pw_lamp.png"));
    /** Tuning: strength cap on hard difficulty. */
    private static final int STRENGTH_MAX_ON_HARD = 300;

    /**
     * Constructs an instance of LampPowerup.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param parent the LevelScene object owning this entity.
     */
    public LampPowerup(int x, int y, LevelScene parent) {
        super(x, y, parent);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup() {
        int addedStrength = this.getParent().getOutlaw().getStrength();
        // Clamp gained strength for this power-up on hard difficulty.
        if (this.getParent().getDifficulty() == LevelScene.DIFFICULTY_HARD) {
            addedStrength = (addedStrength > STRENGTH_MAX_ON_HARD)
                    ? STRENGTH_MAX_ON_HARD
                    : addedStrength;
        }
        this.getParent().getOutlaw().increaseStrength(addedStrength);
        this.getParent().trackCollectedPowerup(ID);
    }

}
