package game.scenes;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.Game;
import game.UIUtils;
import game.entities.Button;
import game.entities.Tile;
import game.entities.powerups.HayPowerup;
import game.entities.powerups.LampPowerup;
import game.entities.powerups.Powerup;
import game.entities.powerups.SnakeOilPowerup;
import game.entities.powerups.WheelPowerup;
import game.entities.props.HeaderProp;
import game.entities.props.Prop;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

public class StatusOverlay {

    private static final Image GAME_PAUSED =
            new Image(Game.getAsset("ui_paused.png"));
    private static final Image GAME_END_BAD =
            new Image(Game.getAsset("ui_game_end_bad.png"));
    private static final Image GAME_END_GOOD =
            new Image(Game.getAsset("ui_game_end_good.png"));
    private static final Image STANDEE_PLAY =
            new Image(Game.getAsset("ui_game_end_standee_play.png"));
    private static final Image STANDEE_EXIT =
            new Image(Game.getAsset("ui_game_end_standee_exit.png"));

    private static final int HUD_MAX_SCORE = 9999999;

    private static final int HUD_BASE_SIZE = 13;
    private static final int HUD_BASE_POS = 2;
    private static final int HUD_BASE_POS_HP = 2;
    private static final int HUD_BASE_POS_MOB = 5;
    private static final int HUD_BASE_POS_TIME = 8;
    private static final int HUD_BASE_POS_SCORE = 11;
    private static final int HUD_POWERUP_POS = 16;

    private static final int HUD_OFFSET_Y = -Tile.SIZE_MID;
    private static final int HUD_TEXT_OFFSET_Y = (Tile.SIZE_MID / 2) + 3;
    private static final int HUD_MAX_NUM = 9999;

    private static final int TX_BASE_START = 0;
    private static final int TX_BASE_MID = 1;
    private static final int TX_BASE_END = 2;

    private static final int TX_POWERUP_START = 3;
    private static final int TX_POWERUP_MID = 4;
    private static final int TX_POWERUP_END = 5;

    private static final int TX_OUTLAW = 9;
    private static final int TX_INFINITY = 10;
    private static final int TX_MOB = 11;
    private static final int TX_TIME = 12;
    private static final int TX_SCORE = 13;

    private static final int TX_POWERUP_LAMP = 14;
    private static final int TX_POWERUP_HAY = 15;
    private static final int TX_POWERUP_WHEEL = 16;
    private static final int TX_POWERUP_SNAKEOIL = 17;

    private static final long UI_SLIDE_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(100);

    private LevelScene level;

    private boolean isPausedVisible;
    private boolean isGameEndVisible;
    private boolean isNameInputVisible;

    private Button playButton;
    private Button exitButton;
    private Button resumeButton;
    private Button goButton;

    private int hudOffsetY;

    private Prop headerProp;
    private String nameInputValue;
    private Text nameInputText;
    private Text nameInputDescription;
    private Text nameInputTip;
    private EventHandler<KeyEvent> nameInputEventHandler;

    private EventHandler<KeyEvent> selectorEventHandler;
    private Button selectedButton;

    public StatusOverlay(LevelScene scene) {
        this.level = scene;

        this.isNameInputVisible = false;
        this.isPausedVisible = false;
        this.isGameEndVisible = false;
        this.playButton = null;
        this.exitButton = null;
        this.resumeButton = null;
        this.goButton = null;

        this.hudOffsetY = HUD_OFFSET_Y;
        scene.getActions().add(UI_SLIDE_INTERVAL, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (hudOffsetY == 0) {
                    return true;
                }
                hudOffsetY++;
                return false;
            }
        });

        this.headerProp = null;
        this.nameInputValue = "";
        this.nameInputText = null;
        this.nameInputDescription = null;
        this.nameInputTip = null;
        this.nameInputEventHandler = null;

        this.selectorEventHandler = null;
        this.selectedButton = null;

        initializeControls();
    }

    public void update(long now) {
        if (this.isGameEndVisible) {
            exitButton.update(now);
            playButton.update(now);
        } else if (this.isPausedVisible) {
            resumeButton.update(now);
        } else if (this.isNameInputVisible) {
            goButton.update(now);
            headerProp.update(now);
        }
    }

    public void draw(GraphicsContext gc) {
        gc.save();
        gc.setFont(UIUtils.FONT_32);
        gc.setFill(UIUtils.COLOR_PRIMARY);

        if (isGameEndVisible || isPausedVisible || isNameInputVisible) {
            UIUtils.drawShade(gc);
        }

        drawHUD(gc);
        if (isGameEndVisible) {
            drawGameEnd(gc);
        } else if (isPausedVisible) {
            drawPaused(gc);
        } else if (isNameInputVisible) {
            drawNameInput(gc);
        }

        if (this.selectedButton != null) {
            this.selectedButton.drawSelector(gc);
        }

        gc.restore();
    }

    private void drawHUD(GraphicsContext gc) {
        this.drawHUDBase(gc);

        int powerupLampCount = level.getPowerupCount(LampPowerup.ID);
        int powerupHayCount = level.getPowerupCount(HayPowerup.ID);
        int powerupWheelCount = level.getPowerupCount(WheelPowerup.ID);
        int powerupSnakeOilCount = level.getPowerupCount(SnakeOilPowerup.ID);

        int tileOffset = HUD_POWERUP_POS;
        tileOffset = this.drawHUDPowerup(gc, tileOffset,
                TX_POWERUP_LAMP, powerupLampCount, LampPowerup.ID);
        tileOffset = this.drawHUDPowerup(gc, tileOffset,
                TX_POWERUP_HAY, powerupHayCount, HayPowerup.ID);
        tileOffset = this.drawHUDPowerup(gc, tileOffset,
                TX_POWERUP_WHEEL, powerupWheelCount, WheelPowerup.ID);
        tileOffset = this.drawHUDPowerup(gc, tileOffset,
                TX_POWERUP_SNAKEOIL, powerupSnakeOilCount, SnakeOilPowerup.ID);
    }

    private void drawHUDBase(GraphicsContext gc) {
        int strength = level.getOutlaw().getStrength();
        int mobKillCount = level.getMobKillCount();
        String strengthText = Integer.toString(strength);
        String mobKillCountText = Integer.toString(mobKillCount);
        String timeLeftText = this.getLevelTimeLeftText();
        String scoreText = Integer.toString(this.level.getScore());
        if (this.level.getScore() > HUD_MAX_SCORE) {
            scoreText = ">" + HUD_MAX_SCORE;
        }

        // Draw base HUD background.
        int tileOffset = HUD_BASE_POS;
        UIUtils.TILE.draw(gc, Tile.SIZE_MID, hudOffsetY, TX_BASE_START);
        for (int i = 0; i < HUD_BASE_SIZE; i++) {
            UIUtils.TILE.draw(gc, Tile.SIZE_MID * tileOffset++,
                    hudOffsetY, TX_BASE_MID);
        }
        UIUtils.TILE.draw(gc, Tile.SIZE_MID * tileOffset++,
                hudOffsetY, TX_BASE_END);

        // Base HUD: player strength.
        UIUtils.TILE.draw(gc, Tile.SIZE_MID * HUD_BASE_POS_HP,
                hudOffsetY, TX_OUTLAW);
        if (level.getOutlaw().isImmortal() || strength > HUD_MAX_NUM) {
            UIUtils.TILE.draw(gc,
                    Tile.SIZE_MID * (HUD_BASE_POS_HP + 1),
                    hudOffsetY, TX_INFINITY);
        } else {
            gc.fillText(strengthText,
                    Tile.SIZE_MID * (HUD_BASE_POS_HP + 1),
                    hudOffsetY + HUD_TEXT_OFFSET_Y);
        }
        // Base HUD: mob kill count.
        UIUtils.TILE.draw(gc,
                Tile.SIZE_MID * HUD_BASE_POS_MOB,
                hudOffsetY, TX_MOB);
        gc.fillText(mobKillCountText,
                Tile.SIZE_MID * (HUD_BASE_POS_MOB + 1),
                hudOffsetY + HUD_TEXT_OFFSET_Y);
        // Base HUD: time left.
        UIUtils.TILE.draw(gc,
                Tile.SIZE_MID * HUD_BASE_POS_TIME,
                hudOffsetY, TX_TIME);
        gc.fillText(timeLeftText,
                Tile.SIZE_MID * (HUD_BASE_POS_TIME + 1),
                hudOffsetY + HUD_TEXT_OFFSET_Y);
        // Base HUD: score.
        UIUtils.TILE.draw(gc,
                Tile.SIZE_MID * HUD_BASE_POS_SCORE,
                hudOffsetY, TX_SCORE);
        gc.fillText(scoreText,
                Tile.SIZE_MID * (HUD_BASE_POS_SCORE + 1),
                hudOffsetY + HUD_TEXT_OFFSET_Y);
    }

    private int drawHUDPowerup(GraphicsContext gc,
            int tileOffset, int iconIndex, int value, int powerupIndex) {
        String valueText = Integer.toString(value);
        int startPartId = (powerupIndex == 0)
                ? TX_POWERUP_START
                : TX_POWERUP_MID;
        int endPartId = (powerupIndex == Powerup.TOTAL_POWERUPS - 1)
                ? TX_POWERUP_END
                : TX_POWERUP_MID;
        UIUtils.TILE.draw(gc, Tile.SIZE_MID * tileOffset,
                hudOffsetY, startPartId);
        UIUtils.TILE.draw(gc, Tile.SIZE_MID * tileOffset,
                hudOffsetY, iconIndex);
        UIUtils.TILE.draw(gc, Tile.SIZE_MID * ++tileOffset,
                hudOffsetY, endPartId);
        gc.fillText(valueText, Tile.SIZE_MID * tileOffset,
                hudOffsetY + HUD_TEXT_OFFSET_Y);
        return tileOffset += 1;
    }

    private void drawGameEnd(GraphicsContext gc) {
        UIUtils.drawMenuBackground(gc, Tile.ALL_VERTICAL / 3, 5);

        Image gameEndCenterImage = null;
        if (!level.getOutlaw().isAlive()) {
            gameEndCenterImage = GAME_END_BAD;
        } else {
            gameEndCenterImage = GAME_END_GOOD;
        }
        gc.drawImage(
                gameEndCenterImage,
                (Game.WINDOW_MAX_WIDTH / 2) - gameEndCenterImage.getWidth() / 2,
                (Game.WINDOW_MAX_HEIGHT / 2) - gameEndCenterImage.getHeight() / 2);

        Image standeePlayImage = STANDEE_PLAY;
        gc.drawImage(
                standeePlayImage,
                (Game.WINDOW_MAX_WIDTH / 5) - standeePlayImage.getWidth() / 2,
                (Game.WINDOW_MAX_HEIGHT / 2) - standeePlayImage.getHeight() / 2);

        Image standeeExitImage = STANDEE_EXIT;
        gc.drawImage(
                standeeExitImage,
                (Game.WINDOW_MAX_WIDTH) - (Game.WINDOW_MAX_WIDTH / 5) - standeeExitImage.getWidth() / 2,
                (Game.WINDOW_MAX_HEIGHT / 2) - standeeExitImage.getHeight() / 2);

        playButton.draw(gc);
        exitButton.draw(gc);
    }

    private void drawPaused(GraphicsContext gc) {
        UIUtils.drawMenuBackground(gc, Tile.ALL_VERTICAL - 4, 1);

        gc.drawImage(
                GAME_PAUSED,
                Tile.SIZE_MID,
                Game.WINDOW_MAX_HEIGHT
                - GAME_PAUSED.getHeight()
                - (Tile.SIZE_MID * 2));

        resumeButton.draw(gc);
        exitButton.draw(gc);
    }

    private void drawNameInput(GraphicsContext gc) {
        UIUtils.drawMenuBackground(gc, (Tile.ALL_VERTICAL / 2), 1);

        goButton.draw(gc);
        headerProp.draw(gc);
    }

    private void initializeControls() {
        playButton = new Button(0, 0, 3);
        playButton.setText("PLAY");
        playButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(
                        new LevelScene(level.getDifficulty()));
            }
        });
        exitButton = new Button(0, 0, 3);
        exitButton.setText("EXIT");
        exitButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                Game.setGameScene(new MainMenuScene());
            }
        });
        resumeButton = new Button(0, 0, 3);
        resumeButton.setText("RESUME");
        resumeButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                level.togglePaused();
            }
        });
        goButton = new Button(0, 0, 3);
        goButton.setText("GO");
        goButton.setClickAction(new Runnable() {
            @Override
            public void run() {
                if (nameInputValue.trim().isEmpty()) {
                    return;
                }
                toggleNameInputVisibility();
                level.handleHighScore(nameInputValue);
            }
        });
        headerProp = new HeaderProp(0, 0, HeaderProp.NAME_INPUT);
        headerProp.setScale(1);
        nameInputText = new Text(nameInputValue);
        nameInputText.setFont(UIUtils.FONT_48);
        nameInputText.setFill(UIUtils.COLOR_PRIMARY);
        nameInputDescription = new Text("You've attained a new high score!");
        nameInputDescription.setFont(UIUtils.FONT_ALT_48);
        nameInputDescription.setFill(UIUtils.COLOR_PRIMARY);
        nameInputDescription.setStroke(UIUtils.COLOR_TERTIARY);
        nameInputDescription.setStrokeType(StrokeType.OUTSIDE);
        nameInputDescription.setStrokeWidth(3);
        nameInputDescription.setY((Game.WINDOW_MAX_HEIGHT / 2)
                + (Tile.SIZE_MID * 6));
        nameInputDescription.setX((Game.WINDOW_MAX_WIDTH / 2)
                - (nameInputDescription.getBoundsInLocal().getWidth() / 2));
        nameInputTip = new Text("Your name goes here");
        nameInputTip.setFont(UIUtils.FONT_32);
        nameInputTip.setFill(UIUtils.COLOR_PRIMARY);
        nameInputTip.setY((Game.WINDOW_MAX_HEIGHT / 2)
                + (Tile.SIZE_MID) + 15);
        nameInputTip.setX((Game.WINDOW_MAX_WIDTH / 2)
                - (nameInputTip.getBoundsInLocal().getWidth() / 2));
    }

    public void toggleGameEndVisibility() {
        this.isGameEndVisible = !this.isGameEndVisible;
        if (this.isGameEndVisible) {
            exitButton.setX((int) ((Game.WINDOW_MAX_WIDTH)
                    - (Game.WINDOW_MAX_WIDTH / 5)
                    - exitButton.getBounds().getWidth() / 2));
            exitButton.setY((Game.WINDOW_MAX_HEIGHT / 2) + 50);
            exitButton.attach(level);

            playButton.setX((int) ((Game.WINDOW_MAX_WIDTH / 5)
                    - playButton.getBounds().getWidth() / 2));
            playButton.setY((Game.WINDOW_MAX_HEIGHT / 2) + 50);
            playButton.attach(level);
            this.attachButtonSelector(playButton, exitButton, false);
        } else {
            exitButton.detach(level);
            playButton.detach(level);
            this.detachButtonSelector(playButton, exitButton, false);
        }
    }

    public void togglePausedVisibility() {
        this.isPausedVisible = !this.isPausedVisible;
        if (this.isPausedVisible) {
            exitButton.setX((int) (Game.WINDOW_MAX_WIDTH
                    - (exitButton.getBounds().getWidth())
                    - (Tile.SIZE_MID * 2)));
            exitButton.setY(Game.WINDOW_MAX_HEIGHT - (Tile.SIZE_MID * 3) + 8);
            exitButton.attach(level);

            resumeButton.setX((int) (Game.WINDOW_MAX_WIDTH
                    - (playButton.getBounds().getWidth())
                    - (Tile.SIZE_MID * 8)));
            resumeButton.setY(Game.WINDOW_MAX_HEIGHT - (Tile.SIZE_MID * 3) + 8);
            resumeButton.attach(level);
            this.attachButtonSelector(resumeButton, exitButton, false);
        } else {
            exitButton.detach(level);
            resumeButton.detach(level);
            this.detachButtonSelector(resumeButton, exitButton, false);
        }
    }

    public void toggleNameInputVisibility() {
        this.isNameInputVisible = !this.isNameInputVisible;

        if (this.isNameInputVisible) {
            headerProp.setX((int) ((Game.WINDOW_MAX_WIDTH / 2)
                    - (headerProp.getWidth() / 2)));
            headerProp.setY((int) ((Game.WINDOW_MAX_HEIGHT / 2)
                    - (headerProp.getHeight() / 2)) - (Tile.SIZE_MID));

            goButton.setX((int) ((Game.WINDOW_MAX_WIDTH / 2)
                    - (goButton.getBounds().getWidth() / 2)));
            goButton.setY((Game.WINDOW_MAX_HEIGHT / 2)
                    + (Tile.SIZE_MID * 3));

            this.nameInputEventHandler = new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCharacter().equals(KeyCode.BACK_SPACE.getChar())
                            && nameInputValue.length() > 0) {
                        nameInputValue = nameInputValue.substring(0, nameInputValue.length() - 1);
                    } else if (nameInputValue.length() <= LevelScene.NAME_MAX_LEN) {
                        nameInputValue += event.getCharacter().trim();
                    }
                    nameInputText.setText(nameInputValue);
                    nameInputText.setY((Game.WINDOW_MAX_HEIGHT / 2)
                            + (Tile.SIZE_MID) + 15);
                    nameInputText.setX((Game.WINDOW_MAX_WIDTH / 2)
                            - (nameInputText.getBoundsInLocal().getWidth() / 2));

                    if (nameInputValue.length() == 0) {
                        nameInputTip.setOpacity(1);
                    } else {
                        nameInputTip.setOpacity(0);
                    }
                }
            };

            goButton.attach(level);
            this.attachButtonSelector(goButton, null, true);
            this.selectedButton = goButton;
            this.level.getInner().addEventHandler(KeyEvent.KEY_TYPED, this.nameInputEventHandler);
            this.level.getRoot().getChildren().add(nameInputText);
            this.level.getRoot().getChildren().add(nameInputDescription);
            this.level.getRoot().getChildren().add(nameInputTip);
        } else {
            goButton.detach(level);
            this.detachButtonSelector(goButton, null, true);
            this.level.getInner().removeEventHandler(KeyEvent.KEY_TYPED, this.nameInputEventHandler);
            this.level.getRoot().getChildren().remove(nameInputText);
            this.level.getRoot().getChildren().remove(nameInputDescription);
            this.level.getRoot().getChildren().remove(nameInputTip);
            this.nameInputEventHandler = null;
        }
    }

    private void attachButtonSelector(Button leftButton, Button rightButton, boolean onRelease) {
        if (this.selectorEventHandler != null) {
            return;
        }
        this.selectedButton = null;
        this.selectorEventHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                case TAB:
                case LEFT:
                case RIGHT:
                    if (rightButton == null) {
                        selectedButton = leftButton;
                    } else if (leftButton == null) {
                        selectedButton = rightButton;
                    } else if (selectedButton == leftButton) {
                        selectedButton = rightButton;
                    } else {
                        selectedButton = leftButton;
                    }
                    break;
                case ENTER:
                case SPACE:
                    if (selectedButton == null) {
                        selectedButton = leftButton;
                    } else {
                        selectedButton.click();
                    }
                    break;
                default:
                    break;
                }
            }
        };
        level.getInner().addEventHandler(
                onRelease ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED,
                this.selectorEventHandler);
    }

    private void detachButtonSelector(Button a, Button b, boolean onRelease) {
        if (this.selectorEventHandler == null) {
            return;
        }
        level.getInner().removeEventHandler(
                onRelease ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED,
                this.selectorEventHandler);
        this.selectorEventHandler = null;
        this.selectedButton = null;
    }

    private String getLevelTimeLeftText() {
        String formatString = "%s:%s";
        if (level.getLevelTimeLeft() <= 0) {
            return String.format(formatString, "0", "00");
        }
        long levelTimeLeftMinutes =
                TimeUnit.NANOSECONDS.toMinutes(level.getLevelTimeLeft());
        long levelTimeLeftSeconds =
                TimeUnit.NANOSECONDS.toSeconds(level.getLevelTimeLeft())
                - (60 * levelTimeLeftMinutes);

        if (levelTimeLeftSeconds <= 9) {
            formatString = "%s:0%s";
        }

        return String.format(
                formatString,
                levelTimeLeftMinutes,
                levelTimeLeftSeconds);
    }

}
