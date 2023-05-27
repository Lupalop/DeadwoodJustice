package game.entities.powerups;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class LampPowerup extends Powerup {

    public static final int ID = 0;

    public static final Image FRAMESET = new Image(
            Game.getAsset("pw_lamp.png"));

    private static final int STRENGTH_MAX_ON_HARD = 300;

    public LampPowerup(int x, int y, LevelScene parent) {
        super(x, y, parent);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup() {
        int outlawStrength = this.getParent().getOutlaw().getStrength();
        // Clamp gained strength for this power-up on hard difficulty.
        if (this.getParent().getDifficulty() == LevelScene.DIFFICULTY_HARD) {
            outlawStrength = (outlawStrength > STRENGTH_MAX_ON_HARD)
                    ? STRENGTH_MAX_ON_HARD
                    : outlawStrength;
        }

        this.getParent().getOutlaw().increaseStrength(outlawStrength);
        this.getParent().consumePowerup(ID);
    }

}
