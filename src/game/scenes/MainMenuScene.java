package game.scenes;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.LevelMap;
import game.TimedActionManager;
import game.UIUtils;
import game.entities.Button;
import game.entities.Sprite;
import game.entities.Tile;
import game.entities.props.HeaderProp;
import game.entities.props.Prop;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class MainMenuScene implements GameScene {

    private static final int MENU_Y =
            Game.WINDOW_MAX_HEIGHT + 8 - (Tile.SIZE_MID * 5);
    private static final long MENU_BG_INTERVAL =
            TimeUnit.SECONDS.toNanos(3);

    private Group root;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;

    private TimedActionManager actions;
    private LevelMap levelMap;

    public MainMenuScene() {
        this.root = new Group();
        this.scene = new Scene(root, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT, UIUtils.COLOR_PRIMARY);
        this.canvas = new Canvas(Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.root.getChildren().add(canvas);
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setImageSmoothing(false);

        this.actions = new TimedActionManager();
        this.addMenuControls();

        this.levelMap = new LevelMap();
        this.levelMap.generate();
        this.levelMap.generateProps();

        this.handleKeyPressEvent();
    }

    private Prop titleProp;
    private Button previousButton;
    private Button actionButton;
    private Button nextButton;

    private static int menuIndex = -1;
    private String[] menuLabels = {
            "P: I'M TOO CHICKEN",
            "P: HURT ME PLENTY",
            "P: NIGHTMARE",
            "HIGH SCORES",
            "HELP",
            "CREDITS",
            "EXIT",
    };
    private Runnable[] menuActions = {
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

    private void addMenuControls() {
        titleProp = new HeaderProp(0, 0, HeaderProp.MENU_TITLE);
        titleProp.setScale(2);
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

        actionButton = new Button(Tile.SIZE_MID * 7, MENU_Y, 9);
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

        this.actions.add(MENU_BG_INTERVAL, true, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                levelMap.generate(true);
                levelMap.generateProps(true);
                return true;
            }
        });
    }

    private void updateMenuIndex(boolean isNext) {
        if (isNext) {
            menuIndex++;
            if (menuIndex >= menuLabels.length) {
                menuIndex = 0;
            }
        } else {
            menuIndex--;
            if (menuIndex < 0) {
                menuIndex = menuLabels.length - 1;
            }
        }

        this.updateButton();
    }

    private void updateButton() {
        actionButton.setText(menuLabels[menuIndex]);
        actionButton.setClickAction(menuActions[menuIndex]);
    }

    @Override
    public void update(long now) {
        for (Sprite sprite : this.levelMap.getSprites()) {
            sprite.update(now);
        }
        this.levelMap.update(now);
        this.actions.update(now);

        this.titleProp.update(now);
        previousButton.update(now);
        actionButton.update(now);
        nextButton.update(now);
    }

    @Override
    public void draw(long now) {
        this.gc.clearRect(0, 0, Game.WINDOW_MAX_WIDTH,
                Game.WINDOW_MAX_HEIGHT);
        this.levelMap.draw(gc);

        this.titleProp.draw(gc);
        previousButton.draw(gc);
        actionButton.draw(gc);
        nextButton.draw(gc);
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

    public static void handleReturnKeyPressEvent(GameScene scene) {
        scene.getInner().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                switch (code) {
                case BACK_SPACE:
                case ESCAPE:
                    Game.setGameScene(new MainMenuScene());
                    Game.playSFX(Button.SFX_BUTTON);
                    break;
                default:
                    break;
                }
            }
        });
    }

    @Override
    public Scene getInner() {
        return this.scene;
    }

    @Override
    public Group getRoot() {
        return root;
    }

    @Override
    public TimedActionManager getActions() {
        return this.actions;
    }

    public LevelMap getLevelMap() {
        return this.levelMap;
    }

    @Override
    public String getBGM() {
        return "bgm_01.mp3";
    }

}
