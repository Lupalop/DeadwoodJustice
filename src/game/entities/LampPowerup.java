package game.entities;

import game.Game;
import game.LevelScene;
import javafx.scene.image.Image;

public class LampPowerup extends Powerup {

    public static final int ID = 0;

    public static final Image FRAMESET = new Image(
            Game.getAsset("pw_lamp.png"));

    public LampPowerup(int x, int y, LevelScene parent) {
        super(x, y, parent);
        this.setImage(FRAMESET);
    }

    @Override
    public void applyPowerup() {
        this.getParent().getOutlaw().increaseStrength(
                this.getParent().getOutlaw().getStrength());
        this.getParent().consumePowerup(ID);
    }

}
