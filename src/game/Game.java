package game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import game.scenes.GameScene;
import game.scenes.MainMenuScene;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 * The Game class handles the initialization and running of a game.
 * It is responsible for retrieving assets and user data, playing
 * music and sound effects, switching scenes, and executing the
 * game timer.
 * @author Francis Dominic Fajardo
 */
public final class Game {

    /** The game name. */
    public static final String GAME_NAME = "Deadwood Justice";
    /** The location of the game's assets. */
    public static final String GAME_ASSETS_PATH = "/game/assets/";

    /** The game window's minimum width. */
    public static final int WINDOW_MIN_WIDTH = 0;
    /** The game window's minimum height. */
    public static final int WINDOW_MIN_HEIGHT = 0;
    /** The game window's maximum width. */
    public static final int WINDOW_MAX_WIDTH = 800;
    /** The game window's maximum height. */
    public static final int WINDOW_MAX_HEIGHT = 600;

    /** Direction: up. */
    public static final byte DIR_UP = 0x1;
    /** Direction: down. */
    public static final byte DIR_DOWN = 0x2;
    /** Direction: left. */
    public static final byte DIR_LEFT = 0x4;
    /** Direction: right. */
    public static final byte DIR_RIGHT = 0x8;

    /** Flag: is debug mode enabled? */
    public static final boolean DEBUG_MODE = false;
    /** Flag: is directional shooting for the player allowed? */
    public static final boolean FLAG_DIRECTIONAL_SHOOTING = false;
    /** Flag: is the game loop's refresh rate fixed? */
    public static final boolean FLAG_FREEZE_REFRESH_RATE = true;

    /** The random number generator. */
    public static final Random RNG = new Random();

    /** The path to the file containing high scores. */
    private static final Path PATH_HIGH_SCORES = Path.of("scores.dat");
    /** The maximum number of high scores that can be stored. */
    private static final int MAX_HIGH_SCORES = 10;

    /** An instance of the media player. */
    private static MediaPlayer mediaPlayer;
    /** A hashtable containing cached background music (Media) objects. */
    private static Hashtable<String, Media> cachedBGM;
    /** A hashtable containing cached sound effects (AudioClip) objects. */
    private static Hashtable<String, AudioClip> cachedSFX;
    /** An array list containing stored player scores. */
    private static ArrayList<PlayerScore> highScores;

    /** An instance of the primary stage or main window. */
    private static Stage primaryStage;
    /** An instance of the currently running game scene. */
    private static GameScene gameScene;
    /** An instance of the game timer. */
    private static GameTimer gameTimer;
    /** A boolean determining whether the game is done initializing. */
    private static boolean initialized;

    /**
     * Initialize and run the game.
     * @param primaryStage a Stage object.
     */
    public static void run(Stage primaryStage) {
        if (Game.initialized) {
            return;
        }

        Game.mediaPlayer = null;
        Game.cachedBGM = new Hashtable<String, Media>();
        Game.cachedSFX = new Hashtable<String, AudioClip>();
        Game.highScores = new ArrayList<PlayerScore>(MAX_HIGH_SCORES);
        Game.loadHighScores();

        Game.primaryStage = primaryStage;
        Game.primaryStage.setResizable(false);
        Game.primaryStage.setTitle(GAME_NAME);
        Game.setGameScene(new MainMenuScene());
        Game.gameTimer = new GameTimer();
        Game.gameTimer.start();
        Game.primaryStage.show();

        // XXX: We have to re-play the background music again here since
        // starting the media playback before the stage/window is
        // shown doesn't work properly.
        Game.playGameSceneBGM();

        Game.initialized = true;
    }

    /**
     * Gets the value of the primary stage property.
     * @return a Stage object.
     */
    public static Stage getPrimaryStage() {
        return Game.primaryStage;
    }

    /**
     * Gets the value of the game scene property.
     * @return a GameScene object.
     */
    public static GameScene getGameScene() {
        return Game.gameScene;
    }

    /**
     * Sets the value of the game scene property.
     * @param gameScene a GameScene object.
     */
    public static void setGameScene(GameScene gameScene) {
        Game.gameScene = gameScene;
        Game.primaryStage.setScene(Game.gameScene.getInner());
        Game.playGameSceneBGM();
    }

    /**
     * Gets the String representation of the asset URL at the given path.
     * @param path a String containing the asset path.
     * @return a String object.
     */
    public static String getAsset(String path) {
        return Game.class.getResource(
                GAME_ASSETS_PATH + path).toExternalForm();
    }

    /**
     * Gets the Path representation of the asset URL at the given path.
     * @param path a String containing the asset path.
     * @return a Path object.
     */
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

    /**
     * Extracts the asset in the given path (if available) and gets the
     * String representation of the URL to the extracted asset.
     *
     * This is used only if the game is stored inside a JAR file.
     * @param path a String containing the asset path.
     * @return a Path object.
     */
    private static Path getAssetFromTemporaryPath(String path) {
        try {
            InputStream in = Game.class.getResourceAsStream(
                    GAME_ASSETS_PATH + path);
            Path tempPath = Files.createTempFile("", "");
            File tempFile = new File(tempPath.toString());
            OutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = in.readAllBytes();
            out.write(buffer);
            out.close();
            return tempPath;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets a list containing high scores.
     * @return a List<PlayerScore> object.
     */
    public static List<PlayerScore> getHighScores() {
        return (Game.highScores.size() > MAX_HIGH_SCORES)
                ? Game.highScores.subList(0, MAX_HIGH_SCORES)
                : Game.highScores;
    }

    /**
     * Gets the potential index where the provided score will be inserted.
     * @param score the player's score.
     * @return an integer.
     */
    public static int getHighScoreIndex(int score) {
        // We need the index, so use an iterator.
        ListIterator<PlayerScore> iter = Game.highScores.listIterator();
        while (iter.hasNext()) {
            PlayerScore other = iter.next();
            if (other == null) {
                continue;
            }
            // Case 1: Score is higher than something else on the list.
            if (score > other.getScore()) {
                return iter.previousIndex();
            }
        }
        // Case 2: Score is lesser than everything else and there's still
        // space on the list.
        if (Game.highScores.size() < MAX_HIGH_SCORES) {
            return Game.highScores.size();
        }
        // Case 3: Score isn't qualified for the high scores list.
        return -1;
    }

    /**
     * Adds the given name, score, and difficulty to the list of high scores
     * if applicable.
     * @param name the player name.
     * @param score the player score.
     * @param difficulty the game difficulty.
     * @return a boolean indicating if the high score was added.
     */
    public static boolean addHighScore(String name, int score,
            int difficulty) {
        int index = Game.getHighScoreIndex(score);
        if (index == -1) {
            return false;
        }
        Game.highScores.add(index, new PlayerScore(
                name, score, difficulty));
        Game.saveHighScores();
        return true;
    }

    /**
     * Loads the high scores from file.
     */
    private static void loadHighScores() {
        if (!PATH_HIGH_SCORES.toFile().exists()) {
            return;
        }

        try {
            // Read all lines from the high scores file.
            List<String> lines = Files.readAllLines(PATH_HIGH_SCORES);
            for (String line : lines) {
                // Each info part is delimited by a comma.
                String[] lineParts = line.split(",");
                if (lineParts.length != 3) {
                    continue;
                }
                // Create a new player score object and add to the list.
                PlayerScore score = new PlayerScore(
                        lineParts[0],
                        Integer.parseInt(lineParts[1]),
                        Integer.parseInt(lineParts[2]));
                Game.highScores.add(score);
            }
        } catch (Exception e) {
            System.out.println("Failed to load high scores data file.");
            if (DEBUG_MODE) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the high scores to file.
     */
    private static void saveHighScores() {
        File output = PATH_HIGH_SCORES.toFile();
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(output));
            // Iterate through all the player score objects in the list and
            // write them to the high scores file.
            for (PlayerScore score : getHighScores()) {
                writer.printf("%s,%s,%s%n",
                        score.getName(),
                        score.getScore(),
                        score.getDifficulty());
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Failed to save high scores data file.");
            if (DEBUG_MODE) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Sets the currently playing media player.
     * @param assetName the BGM asset name.
     * @param volume the volume at which the media will be played.
     */
    private static synchronized void setMediaPlayer(
            String assetName, double volume) {
        if (!Game.initialized) {
            return;
        }

        Media media = Game.cachedBGM.get(assetName);
        // Create a new Media object and cache it if it wasn't played
        // previously to prevent a hang caused by switching tracks.
        if (media == null) {
            String assetPath = Game.getAsset(assetName);
            // Invalid asset.
            if (assetPath == null) {
                return;
            }
            media = new Media(assetPath);
            Game.cachedBGM.put(assetName, media);
        }

        // Stop the currently playing media player.
        if (Game.mediaPlayer != null) {
            if (Game.mediaPlayer.getMedia() == media) {
                return;
            }
            Game.mediaPlayer.stop();
        }

        // Create a new media player object and play it indefinitely.
        Game.mediaPlayer = new MediaPlayer(media);
        Game.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        Game.mediaPlayer.play();
    }

    /**
     * Plays a background music track.
     * @param assetName the BGM asset name.
     * @param volume the volume at which the media will be played.
     */
    public static void playBGM(String assetName, double volume) {
        // XXX: We have to do this in a new thread to avoid the
        // noticeable lag on input when loading media files.
        Thread player = new Thread(new Runnable() {
            @Override
            public void run() {
                setMediaPlayer(assetName, volume);
            }
        });
        player.start();
    }

    /**
     * Plays a background music track.
     * @param assetName the BGM asset name.
     */
    public static void playBGM(String assetName) {
        playBGM(assetName, 1);
    }

    /**
     * Plays the background music track associated with the current
     * game scene. This doesn't do anything if the asset doesn't exist.
     */
    private static void playGameSceneBGM() {
        if (Game.gameScene.getBGM() != null) {
            Game.playBGM(Game.gameScene.getBGM());
        }
    }

    /**
     * Plays a sound effect.
     * @param assetName the SFX asset name.
     * @param volume the volume at which the media will be played.
     */
    public static void playSFX(String assetName, double volume) {
        if (!Game.initialized) {
            return;
        }

        AudioClip clip = Game.cachedSFX.get(assetName);
        // Create a new AudioClip object and cache it if it wasn't played
        // previously to prevent a hang when playing sound effects.
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

    /**
     * Plays a sound effect.
     * @param assetName the SFX asset name.
     */
    public static void playSFX(String assetName) {
        playSFX(assetName, 1);
    }

    /**
     * Returns whether the given direction(s) are active in the given
     * byte indicating active directions.
     * @param activeDirections a byte indicating active directions.
     * @param directionFlag the direction(s) being checked for.
     * @return a boolean value.
     */
    public static boolean isDirectionActive(byte activeDirections, byte directionFlag) {
        return ((activeDirections & directionFlag) == directionFlag);
    }

}
