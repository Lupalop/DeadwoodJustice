package game;

import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;

public final class GameTimer extends AnimationTimer {

    private static final int REFRESH_RATE = 60;
    private static final long FRAME_DURATION =
            TimeUnit.SECONDS.toNanos(1) / REFRESH_RATE;
    private long nextFrameTime = 0;

    @Override
    public void handle(long now) {
        if (Game.getGameScene() == null) {
            return;
        }

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
