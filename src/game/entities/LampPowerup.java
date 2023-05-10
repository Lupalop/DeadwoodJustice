package game.entities;

import game.Game;
import game.scenes.LevelScene;
import javafx.scene.image.Image;

public class LampPowerup extends Powerup {

    public final static Image FRAMESET = new Image(
            Game.getAsset("pw_lamp.png"));


    public LampPowerup(int xPos, int yPos) {
        super(xPos, yPos);
        this.setImage(FRAMESET);
    }

    @Override
    public void doPowerup(LevelScene scene) {
        scene.getOutlaw().increaseStrength(scene.getOutlaw().getStrength());
    }
    
}
