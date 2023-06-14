package game.entities.powerups;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public final class SnakeOilPowerup extends Powerup {

    public static final int ID = 3;

    private static final Image FRAMESET = new Image(
            Game.getAsset("pw_snakeoil.png"));

    public SnakeOilPowerup(int x, int y, LevelScene parent) {
        super(x, y, parent);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup() {
        this.getParent().applyZeroMobSpeed(POWERUP_TIMEOUT);
        this.getParent().consumePowerup(ID);
    }

}
