package game.entities;

import game.Game;
import javafx.scene.image.Image;

public final class HeaderSprite extends Sprite {

    public static final String HOW_TO_PLAY = "ui_howtoplay.png";
    public static final String HIGH_SCORES = "ui_highscores.png";
    public static final String MENU_TITLE = "ui_title.png";
    public static final String NAME_INPUT = "ui_name.png";

    public HeaderSprite(int xPos, int yPos, String assetPath) {
        super(xPos, yPos);

        this.setImage(new Image(Game.getAsset(assetPath)));
    }

}
