package game;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.entities.Button;
import game.entities.Prop;
import game.entities.Sprite;
import game.entities.Tile;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

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
                Game.WINDOW_MAX_HEIGHT, Game.COLOR_MAIN);
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
    }

    private Prop titleProp;
    private Button previousButton;
    private Button actionButton;
    private Button nextButton;

    private int menuIndex = -1;
    private String[] menuLabels = {
            "NEW GAME",
            //"DIFFICULTY: EASY",
            //"HIGH SCORES",
            "HELP",
            "CREDITS",
            "EXIT",
    };
    private Runnable[] menuActions = {
            new Runnable() {
                @Override
                public void run() {
                    Game.setGameScene(new LevelScene());
                }
            },
            /*new Runnable() {
                @Override
                public void run() {
                    actionButton.setText("TODO");
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    actionButton.setText("TODO");
                }
            },*/
            new Runnable() {
                @Override
                public void run() {
                    actionButton.setText("TODO");
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
        titleProp = new Prop(0, 0, "ui_title.png");
        titleProp.setScale(1);
        titleProp.setX((int) ((Game.WINDOW_MAX_WIDTH / 2) - (titleProp.getWidth() / 2)));
        titleProp.setY((int) ((Game.WINDOW_MAX_HEIGHT / 2) - (titleProp.getHeight() / 2)) - (Tile.SIZE_MID));

        previousButton = new Button(Tile.SIZE_MID * 4, MENU_Y, 0, this);
        previousButton.setText(Button.TEXT_BACK);
        previousButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                updateButton(false);
            }
        });

        actionButton = new Button(Tile.SIZE_MID * 7, MENU_Y, 9, this);
        updateButton(true);

        nextButton = new Button(Game.WINDOW_MAX_WIDTH - (Tile.SIZE_MID * 6), MENU_Y, 0, this);
        nextButton.setText(Button.TEXT_FORWARD);
        nextButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                updateButton(true);
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

    private void updateButton(boolean isNext) {
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

    @Override
    public Scene getInner() {
        return this.scene;
    }

    @Override
    public Group getRoot() {
        return root;
    }

    public TimedActionManager getActions() {
        return this.actions;
    }

    public LevelMap getLevelMap() {
        return this.levelMap;
    }

}
