package game.scenes;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.entities.Button;
import game.entities.HeaderSprite;
import game.entities.Sprite;
import game.entities.Tile;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * This class handles the Main Menu scene logic.
 * @author Francis Dominic Fajardo
 */
public final class MainMenuScene extends GameScene {

    /** Tuning: duration before generating another background map. */
    private static final long MAP_SWITCH_INTERVAL =
            TimeUnit.SECONDS.toNanos(3);
    /** Tuning: initial y-position of the menu. */
    private static final int MENU_Y =
            Game.WINDOW_MAX_HEIGHT + 8 - (Tile.SIZE_MID * 5);

    /** Main menu labels. */
    private static final String[] MENU_LABELS = {
            "P: I'M TOO CHICKEN",
            "P: HURT ME PLENTY",
            "P: NIGHTMARE",
            "HIGH SCORES",
            "HELP",
            "CREDITS",
            "EXIT",
    };
    /** Main menu actions. These should match the index of each label. */
    private static final Runnable[] MENU_ACTIONS = {
            new Runnable() {
                @Override
                public void run() {
                    Game.setGameScene(new LevelScene(
                            LevelScene.DIFFICULTY_EASY));
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    Game.setGameScene(new LevelScene(
                            LevelScene.DIFFICULTY_MEDIUM));
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    Game.setGameScene(new LevelScene(
                            LevelScene.DIFFICULTY_HARD));
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    Game.setGameScene(new HighScoresScene());
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    Game.setGameScene(new HelpScene());
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    Game.setGameScene(new CreditsScene());
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            },
    };
    /** Currently selected menu item index. */
    private static int menuIndex = -1;

    /** Control: scene header. */
    private Sprite titleProp;
    /** Control: previous menu item button. */
    private Button previousButton;
    /** Control: selected menu item button. */
    private Button actionButton;
    /** Control: next menu item button. */
    private Button nextButton;

    /**
     * Constructs an empty instance of MainMenuScene.
     */
    public MainMenuScene() {
        super();
        this.addMenuControls();
        this.handleKeyPressEvent();
    }

    /**
     * Adds menu controls.
     */
    private void addMenuControls() {
        titleProp = new HeaderSprite(0, 0, HeaderSprite.MENU_TITLE);
        titleProp.setX((int) ((Game.WINDOW_MAX_WIDTH / 2)
                - (titleProp.getBounds().getWidth() / 2)));
        titleProp.setY((int) ((Game.WINDOW_MAX_HEIGHT / 2)
                - (titleProp.getBounds().getHeight() / 2))
                - (Tile.SIZE_MID));

        previousButton = new Button(Tile.SIZE_MID * 5,
                MENU_Y,
                Button.SIZE_ARROW_LEFT);
        previousButton.attach(this);
        previousButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                updateMenuIndex(false);
            }
        });

        actionButton = new Button(Tile.SIZE_MID * 7,
                MENU_Y,
                Tile.ALL_HORIZONTAL - (16));
        actionButton.attach(this);
        if (menuIndex != -1) {
            updateMenuButton();
        } else {
            updateMenuIndex(true);
        }

        nextButton = new Button(Game.WINDOW_MAX_WIDTH - (Tile.SIZE_MID * 6),
                MENU_Y,
                Button.SIZE_ARROW_RIGHT);
        nextButton.attach(this);
        nextButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                updateMenuIndex(true);
            }
        });

        // Change the background level map every X seconds.
        this.actions.add(MAP_SWITCH_INTERVAL, true, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                levelMap.generate(true);
                levelMap.generateProps(true);
                return true;
            }
        });

        this.levelMap.addOverlay(this.titleProp);
        this.levelMap.addOverlay(this.previousButton);
        this.levelMap.addOverlay(this.actionButton);
        this.levelMap.addOverlay(this.nextButton);
    }

    /**
     * Updates the currently selected menu item and index.
     * @param isNext whether the index moves forward or backward.
     */
    private void updateMenuIndex(boolean isNext) {
        if (isNext) {
            menuIndex++;
            if (menuIndex >= MENU_LABELS.length) {
                menuIndex = 0;
            }
        } else {
            menuIndex--;
            if (menuIndex < 0) {
                menuIndex = MENU_LABELS.length - 1;
            }
        }

        this.updateMenuButton();
    }

    /**
     * Updates the currently selected menu item.
     */
    private void updateMenuButton() {
        actionButton.setText(MENU_LABELS[menuIndex]);
        actionButton.setClickAction(MENU_ACTIONS[menuIndex]);
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

    /**
     * Handles the key press events for this scene.
     */
    private void handleKeyPressEvent() {
        this.scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                switch (code) {
                case LEFT:
                    previousButton.click();
                    break;
                case RIGHT:
                    nextButton.click();
                    break;
                case ENTER:
                case SPACE:
                    actionButton.click();
                    break;
                default:
                    break;
                }
            }
        });
    }

    @Override
    public String getBGM() {
        return "bgm_01.mp3";
    }

}
