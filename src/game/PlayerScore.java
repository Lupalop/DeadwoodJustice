package game;

/**
 * The PlayerScore class stores information on the current high score
 * entry, which includes the name, score, and game difficulty.
 * @author Francis Dominic Fajardo
 */
public class PlayerScore {

    /** The player's name. */
    private String name;
    /** The player's score. */
    private int score;
    /** The game difficulty when the score was achieved. */
    private int difficulty;

    /**
     * Creates a new instance of the PlayerScore class.
     * @param name the player's name.
     * @param score the player's score.
     * @param difficulty the game difficulty.
     */
    public PlayerScore(String name, int score, int difficulty) {
        this.name = name;
        this.score = score;
        this.difficulty = difficulty;
    }

    /**
     * Gets the value of the name property.
     * @return a String.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the score property.
     * @return an integer.
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the value of the difficulty property.
     * @return an integer representing difficulty.
     */
    public int getDifficulty() {
        return difficulty;
    }

}
