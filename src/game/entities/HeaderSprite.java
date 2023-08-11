package game.entities;

import game.Game;
import javafx.scene.image.Image;

/**
 * This class is a sprite that represents images used as headers
 * in the user interface (e.g., in-game menus).
 * @author Francis Dominic Fajardo
 */
public final class HeaderSprite extends Sprite {

    /** Path to header: how to play. */
    public static final String HOW_TO_PLAY = "ui_howtoplay.png";
    /** Path to header: high scores. */
    public static final String HIGH_SCORES = "ui_highscores.png";
    /** Path to header: title logo. */
    public static final String MENU_TITLE = "ui_title.png";
    /** Path to header: name input. */
    public static final String NAME_INPUT = "ui_name.png";

    /**
     * Constructs an instance of HeaderSprite.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     * @param assetPath path to an image asset (constant).
     */
    public HeaderSprite(int xPos, int yPos, String assetPath) {
        super(xPos, yPos);

        this.setImage(new Image(Game.getAsset(assetPath)));
    }

}
