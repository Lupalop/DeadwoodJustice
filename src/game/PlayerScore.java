package game;

public class PlayerScore {

    private String name;
    private int score;
    private int difficulty;

    public PlayerScore(String name, int score, int difficulty) {
        this.name = name;
        this.score = score;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getDifficulty() {
        return difficulty;
    }

}
