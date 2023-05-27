module FajardoProject {

    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.media;

    exports game;
    exports game.entities;
    exports game.entities.effects;
    exports game.entities.mobs;
    exports game.entities.powerups;
    exports game.scenes;

}
