package game.entities.powerups;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public final class HayPowerup extends Powerup {

    public static final int ID = 1;

    private static final Image FRAMESET = new Image(
            Game.getAsset("pw_hay.png"));

    public HayPowerup(int x, int y, LevelScene parent) {
        super(x, y, parent);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup() {
        this.getParent().applyImmortality(POWERUP_TIMEOUT);
        this.getParent().consumePowerup(ID);
    }

}
