package game.scenes;

import javafx.scene.Group;
import javafx.scene.Scene;

public interface GameScene {
    public Scene getInnerScene();

    public void update(long currentNanoTime);
    public void draw(long currentNanoTime);
    
    public Group getRoot();
}
