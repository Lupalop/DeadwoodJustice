package game.scenes;

import game.Game;
import game.UIUtils;
import game.entities.Button;
import game.entities.HeaderSprite;
import game.entities.Sprite;
import game.entities.Tile;

/**
 * This class handles the Help scene logic.
 * @author Francis Dominic Fajardo
 */
public final class HelpScene extends GameScene {

    /** Control: back button. */
    private Button backButton;
    /** Control: scene header. */
    private Sprite helpImage;

    /**
     * Constructs an empty instance of HelpScene.
     */
    public HelpScene() {
        super();
        this.addMenuControls();
        UIUtils.handleReturnToMainMenu(this);
    }

    /**
     * Adds menu controls.
     */
    private void addMenuControls() {
        backButton = new Button(Tile.SIZE_MID, Tile.SIZE_MID,
                Button.SIZE_ARROW_LEFT);
        backButton.attach(this);
        backButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new MainMenuScene());
            }
        });

        helpImage = new HeaderSprite(0, 0, HeaderSprite.HOW_TO_PLAY);
        helpImage.setScale(1);

        this.levelMap.addOverlay(backButton);
        this.levelMap.addOverlay(helpImage);
    }

    @Override
    public void update(long now) {
        this.levelMap.update(now);
        this.actions.update(now);
    }

    @Override
    public void draw(long now) {
        this.gc.clearRect(0, 0, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.levelMap.draw(gc);
    }

    @Override
    public String getBGM() {
        return "bgm_01.mp3";
    }

}
