package game;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The main class of the application.
 * @author Francis Dominic Fajardo
 */
public class Main extends Application {

    /**
     * This method is called when the application is launched.
     * @param args array of command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Game.run(primaryStage);
    }

}
