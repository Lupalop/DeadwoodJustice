package game.scenes;

import game.TimedActionManager;
import javafx.scene.Group;
import javafx.scene.Scene;

public interface GameScene {

    public void update(long now);
    public void draw(long now);

    public Scene getInner();
    public Group getRoot();
    public TimedActionManager getActions();
    public String getBGM();

}
