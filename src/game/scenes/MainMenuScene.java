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
 * Main Menu scene.
 * @author Francis Dominic Fajardo
 */
public final class MainMenuScene extends GameScene {

    private static final long MAP_SWITCH_INTERVAL =
            TimeUnit.SECONDS.toNanos(3);
    private static final int MENU_Y =
            Game.WINDOW_MAX_HEIGHT + 8 - (Tile.SIZE_MID * 5);
    private static final String[] MENU_LABELS = {
            "P: I'M TOO CHICKEN",
            "P: HURT ME PLENTY",
            "P: NIGHTMARE",
            "HIGH SCORES",
            "HELP",
            "CREDITS",
            "EXIT",
    };
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

    private static int menuIndex = -1;

    private Sprite titleProp;
    private Button previousButton;
    private Button actionButton;
    private Button nextButton;

    public MainMenuScene() {
        super();
        this.addMenuControls();
        this.handleKeyPressEvent();
    }

    private void addMenuControls() {
        titleProp = new HeaderSprite(0, 0, HeaderSprite.MENU_TITLE);
        titleProp.setX((int) ((Game.WINDOW_MAX_WIDTH / 2) - (titleProp.getBounds().getWidth() / 2)));
        titleProp.setY((int) ((Game.WINDOW_MAX_HEIGHT / 2) - (titleProp.getBounds().getHeight() / 2)) - (Tile.SIZE_MID));

        previousButton = new Button(Tile.SIZE_MID * 5, MENU_Y,
                Button.SIZE_ARROW_LEFT);
        previousButton.attach(this);
        previousButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                updateMenuIndex(false);
            }
        });

        actionButton = new Button(Tile.SIZE_MID * 7, MENU_Y, Tile.ALL_HORIZONTAL - (16));
        actionButton.attach(this);
        if (menuIndex != -1) {
            updateButton();
        } else {
            updateMenuIndex(true);
        }

        nextButton = new Button(Game.WINDOW_MAX_WIDTH - (Tile.SIZE_MID * 6), MENU_Y,
                Button.SIZE_ARROW_RIGHT);
        nextButton.attach(this);
        nextButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                updateMenuIndex(true);
            }
        });

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

        this.updateButton();
    }

    private void updateButton() {
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
