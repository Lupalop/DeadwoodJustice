package game;

import javafx.scene.Group;
import javafx.scene.Scene;

public interface GameScene {

    public void update(long now);
    public void draw(long now);

    public Scene getInner();
    public Group getRoot();

}
