package game;

import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;

/**
 * The GameTimer class is responsible for keeping track of the time and
 * for calling the update and draw methods on the current scene.
 * This allows the game to run at a consistent frame rate.
 * @author Francis Dominic Fajardo
 */
public final class GameTimer extends AnimationTimer {

    /** The target refresh rate. */
    private static final int REFRESH_RATE = 60;
    /** The duration of each frame. */
    private static final long FRAME_DURATION =
            TimeUnit.SECONDS.toNanos(1) / REFRESH_RATE;
    /** Keeps track of the time before the next frame draw/update. */
    private long nextFrameTime = 0;

    @Override
    public void handle(long now) {
        // There's no game scene to draw/update.
        if (Game.getGameScene() == null) {
            return;
        }

        // The following is based on code written by Julien Giband
        // at SO: https://stackoverflow.com/a/71500331/9610960
        if (Game.FLAG_FREEZE_REFRESH_RATE) {
            // Skip first frame but record its timing.
            if (nextFrameTime == 0) {
                nextFrameTime = now;
                return;
            }
            // Save a cycle if we have multiple calls for a single screen frame.
            if (now <= nextFrameTime) {
                return;
            }
            // Calculate remaining time until next screen frame.
            long rest = now % FRAME_DURATION;
            nextFrameTime = now;
            // Fix timing to next screen frame.
            if (rest != 0) {
                nextFrameTime += FRAME_DURATION - rest;
            }
        }

        Game.getGameScene().update(now);
        Game.getGameScene().draw(now);
    }

}
