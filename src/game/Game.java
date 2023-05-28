package game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Random;

import game.scenes.GameScene;
import game.scenes.MainMenuScene;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public final class Game {

    public static final String GAME_NAME = "Deadwood Justice";
    public static final String GAME_ASSETS_PATH = "/game/assets/";

    public static final String FONT_PATH =
            Game.getAsset("THALEAHFAT.ttf");
    public static final String FONT_MUP_PATH =
            Game.getAsset("MATCHUPPRO.ttf");

    public static final int FONT_SIZE_32 = 32;
    public static final int FONT_SIZE_48 = 48;
    public static final int FONT_SIZE_BTN = 21;

    public static final Font FONT_48 =
            Font.loadFont(FONT_PATH, FONT_SIZE_48);
    public static final Font FONT_32 =
            Font.loadFont(FONT_PATH, FONT_SIZE_32);

    public static final Font FONT_ALT_48 =
            Font.loadFont(FONT_MUP_PATH, FONT_SIZE_48);
    public static final Font FONT_ALT_32 =
            Font.loadFont(FONT_MUP_PATH, FONT_SIZE_32);

    public static final int WINDOW_MIN_WIDTH = 0;
    public static final int WINDOW_MAX_WIDTH = 800;
    public static final int WINDOW_MIN_HEIGHT = 0;
    public static final int WINDOW_MAX_HEIGHT = 600;

    public static final byte DIR_UP = 0x1;
    public static final byte DIR_DOWN = 0x2;
    public static final byte DIR_LEFT = 0x4;
    public static final byte DIR_RIGHT = 0x8;

    public static final Paint COLOR_MAIN = Paint.valueOf("eeca84");
    public static final Paint COLOR_ACCENT = Paint.valueOf("49276d");

    public static final boolean DEBUG_MODE = false;
    public static final boolean FLAG_DIRECTIONAL_SHOOTING = false;
    public static final boolean FLAG_SMARTER_MOBS = true;
    public static final boolean FLAG_DELAY_IF_BOSS_IS_ALIVE = false;
    public static final boolean FLAG_FIX_DRAW_ORDER = true;
    public static final boolean FLAG_MOBS_CHECK_PASSABILITY = true;
    public static final boolean FLAG_IGNORE_PROP_COLLISION = true;
    public static final boolean FLAG_MOBS_CAN_SHOOT = false;
    public static final boolean FLAG_SHOW_MOTES = true;

    public static final Random RNG = new Random();

    private static MediaPlayer mediaPlayer;
    private static Hashtable<String, Media> cachedBGM;
    private static Hashtable<String, AudioClip> cachedSFX;

    private static Stage primaryStage;
    private static GameScene gameScene;
    private static GameTimer gameTimer = new GameTimer();
    private static boolean initialized;

    public static void run(Stage primaryStage) {
        if (Game.initialized) {
            return;
        }

        Game.mediaPlayer = null;
        Game.cachedBGM = new Hashtable<String, Media>();
        Game.cachedSFX = new Hashtable<String, AudioClip>();

        Game.primaryStage = primaryStage;
        Game.primaryStage.setResizable(false);
        Game.primaryStage.setTitle(GAME_NAME);
        Game.setGameScene(new MainMenuScene());
        Game.gameTimer.start();
        Game.primaryStage.show();

        // XXX: We have to re-play the background music again here since
        // starting the media playback before the stage/window is
        // shown doesn't work properly.
        Game.playGameSceneBGM();

        Game.initialized = true;
    }

    public static Stage getPrimaryStage() {
        return Game.primaryStage;
    }

    public static GameScene getGameScene() {
        return Game.gameScene;
    }

    public static void setGameScene(GameScene gameScene) {
        Game.gameScene = gameScene;
        Game.primaryStage.setScene(Game.gameScene.getInner());
        Game.playGameSceneBGM();
    }

    public static String getAsset(String path) {
        return Game.class.getResource(
                GAME_ASSETS_PATH + path).toExternalForm();
    }

    public static Path getAssetAsPath(String path) {
        try {
            return Path.of(Game.class.getResource(
                    GAME_ASSETS_PATH + path).toURI());
        } catch (URISyntaxException e) {
            return null;
        } catch (FileSystemNotFoundException e) {
            // Try to extract the asset from the JAR file and
            // return the path to the extracted temporary file.
            return Game.getAssetFromTemporaryPath(path);
        }
    }

    private static Path getAssetFromTemporaryPath(String path) {
        try {
            InputStream in = Game.class.getResourceAsStream(
                    GAME_ASSETS_PATH + path);
            Path tempPath = Files.createTempFile("", "");
            byte[] buffer = in.readAllBytes();
            File tempFile = new File(tempPath.toString());
            OutputStream out = new FileOutputStream(tempFile);
            out.write(buffer);
            out.close();
            return tempPath;
        } catch (Exception e) {
            return null;
        }
    }

    private static synchronized void setMediaPlayer(
            String assetName, double volume) {
        if (!Game.initialized) {
            return;
        }

        Media media = Game.cachedBGM.get(assetName);
        if (media == null) {
            String assetPath = Game.getAsset(assetName);
            if (assetPath == null) {
                return;
            }
            media = new Media(assetPath);
            Game.cachedBGM.put(assetName, media);
        }

        if (Game.mediaPlayer != null) {
            if (Game.mediaPlayer.getMedia() == media) {
                return;
            }
            Game.mediaPlayer.stop();
        }

        Game.mediaPlayer = new MediaPlayer(media);
        Game.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        Game.mediaPlayer.play();
    }

    public static void playBGM(String assetName, double volume) {
        // XXX: We have to do this in a different thread to avoid the
        // noticeable lag on input when loading media files.
        Thread player = new Thread(new Runnable() {
            @Override
            public void run() {
                setMediaPlayer(assetName, volume);
            }
        });
        player.start();
    }

    public static void playBGM(String assetName) {
        playBGM(assetName, 1);
    }

    private static void playGameSceneBGM() {
        if (Game.gameScene.getBGM() != null) {
            Game.playBGM(Game.gameScene.getBGM());
        }
    }

    public static void playSFX(String assetName, double volume) {
        if (!Game.initialized) {
            return;
        }

        AudioClip clip = Game.cachedSFX.get(assetName);
        if (clip == null) {
            String assetPath = Game.getAsset(assetName);
            if (assetPath == null) {
                return;
            }
            clip = new AudioClip(assetPath);
            Game.cachedSFX.put(assetName, clip);
        }

        clip.play(volume);
    }

    public static void playSFX(String assetName) {
        playSFX(assetName, 1);
    }

    public static boolean isDirectionActive(byte activeDirections, byte directionFlag) {
        return ((activeDirections & directionFlag) == directionFlag);
    }

}
