package game;

/**
 * This class stores information on the current high score entry,
 * which includes the name, score, and game difficulty.
 * @author Francis Dominic Fajardo
 */
public class GameScore {

    /** The player's name. */
    private String name;
    /** The player's score. */
    private int score;
    /** The game difficulty when the score was achieved. */
    private int difficulty;

    /**
     * Constructs an instance of PlayerScore.
     * @param name the player's name.
     * @param score the player's score.
     * @param difficulty the game difficulty.
     */
    public GameScore(String name, int score, int difficulty) {
        this.name = name;
        this.score = score;
        this.difficulty = difficulty;
    }

    /**
     * Retrieves the player name assigned to this entry.
     * @return a String.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the score assigned to this entry.
     * @return an integer.
     */
    public int getScore() {
        return score;
    }

    /**
     * Retrieves the difficulty assigned to this entry.
     * @return an integer representing difficulty.
     */
    public int getDifficulty() {
        return difficulty;
    }

}
