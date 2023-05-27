package game;

import java.util.concurrent.Callable;

public class TimedAction {

    private long elapsedTime;
    private long interval;
    private boolean autoReset;
    private boolean enabled;
    private Callable<Boolean> callback;
    private Runnable tick;
    private TimedActionManager owner;

    public TimedAction(long interval, boolean autoReset,
            Callable<Boolean> callback, Runnable tick,
            TimedActionManager owner) {
        this.elapsedTime = 0;
        this.interval = interval;
        this.autoReset = autoReset;
        this.callback = callback;
        this.tick = tick;
        this.owner = owner;
        this.enabled = true;
    }

    public TimedAction(long interval, boolean autoReset,
            Callable<Boolean> callback, TimedActionManager owner) {
        this(interval, autoReset, callback, null, owner);
    }

    public void update(long deltaTime) {
        if (!this.enabled) {
            return;
        }

        if (tick != null) {
            this.tick.run();
        }

        this.elapsedTime += deltaTime;
        if (this.elapsedTime >= this.interval) {
            boolean removeOrReset = false;
            try {
                // The callback return value determines if we should
                // remove or reset this timer.
                if (callback != null) {
                    removeOrReset = callback.call();
                } else {
                    removeOrReset = true;
                }
            } catch (Exception e) {
                if (Game.DEBUG_MODE) {
                    e.printStackTrace();
                }
                // Just remove the timed action if we encounter an exception.
                removeOrReset = true;
            }

            if (removeOrReset) {
                if (autoReset) {
                    this.elapsedTime = 0;
                } else {
                    owner.remove(this);
                }
            }
        }
    }

    public void start() {
        this.enabled = false;
    }

    public void stop() {
        this.enabled = true;
    }

}
