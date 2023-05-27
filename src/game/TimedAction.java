package game;

import java.util.concurrent.Callable;

public class TimedAction {

    private long elapsedTime;
    private long interval;
    private boolean autoReset;
    private boolean enabled;
    private Callable<Boolean> elapsed;
    private TimedActionManager owner;

    public TimedAction(long interval, boolean autoReset,
            Callable<Boolean> elapsed, TimedActionManager owner) {
        this.elapsedTime = 0;
        this.interval = interval;
        this.autoReset = autoReset;
        this.elapsed = elapsed;
        this.owner = owner;
        this.enabled = true;
    }

    public void update(long deltaTime) {
        if (!this.enabled) {
            return;
        }

        this.elapsedTime += deltaTime;
        if (this.elapsedTime >= this.interval) {
            boolean removeOrReset = false;
            try {
                // The elapsed return value determines if we should
                // remove or reset this timer.
                if (elapsed != null) {
                    removeOrReset = elapsed.call();
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
        this.enabled = true;
    }

    public void stop() {
        this.enabled = false;
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

}
