package game;

import java.util.concurrent.Callable;

public class TimedAction {

    private long startTime;
    private long endTime;
    private boolean autoReset;
    private Callable<Boolean> callback;
    private TimedActionManager owner;

    public TimedAction(long startTime, long endTime, boolean autoReset,
            Callable<Boolean> callback, TimedActionManager owner) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.autoReset = autoReset;
        this.callback = callback;
        this.owner = owner;
    }

    public void update(long now) {
        long deltaTime = (now - this.startTime);
        if (deltaTime >= this.endTime) {
            boolean removeOrReset = false;
            try {
                // The callback return value determines if we should
                // remove or reset this timer.
                removeOrReset = callback.call();
            } catch (Exception e) {
                if (Game.DEBUG_MODE) {
                    e.printStackTrace();
                }
                // Just remove the timed action if we encounter an exception.
                removeOrReset = true;
            }

            if (removeOrReset) {
                if (autoReset) {
                    this.startTime = now;
                } else {
                    owner.remove(this);
                }
            }
        }
    }

}
