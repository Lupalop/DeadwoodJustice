package game;

import java.util.concurrent.Callable;

/**
 * This class represents tasks scheduled to be executed once
 * or more than one time in a set interval.
 * @author Francis Dominic Fajardo
 */
public class TimedAction {

    /** Time elapsed before the set interval. */
    private long elapsedTime;
    /** The interval or the time to wait before the task is invoked. */
    private long interval;
    /** Whether the timer should reset after invoking the task. */
    private boolean autoReset;
    /** Whether the timer is running or not. */
    private boolean enabled;
    /** Whether the timer is closed and can no longer be run again. */
    private boolean closed;
    /** The task to be invoked when time is up. */
    private Callable<Boolean> elapsed;
    /** The manager owning this timer. */
    private TimedActionManager owner;

    /**
     * Constructs an instance of TimedAction.
     * @param interval the time to wait before the task is invoked.
     * @param autoReset determines if the timer should reset after completion.
     * @param elapsed task invoked when time is up.
     * @param owner the manager owning this timer.
     */
    public TimedAction(long interval, boolean autoReset,
            Callable<Boolean> elapsed, TimedActionManager owner) {
        this.elapsedTime = 0;
        this.interval = interval;
        this.autoReset = autoReset;
        this.elapsed = elapsed;
        this.owner = owner;
        this.enabled = true;
        this.closed = false;
    }

    /**
     * Updates the state of this timed action.
     * @param deltaTime time difference between update calls.
     */
    void update(long deltaTime) {
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
                // Reset the elapsed time or close this timer depending
                // on the value of the auto reset attribute.
                if (autoReset) {
                    this.elapsedTime = 0;
                } else {
                    close();
                }
            }
        }
    }

    /**
     * Starts the timed action.
     */
    public void start() {
        this.enabled = true;
    }

    /**
     * Stops the timed action.
     */
    public void stop() {
        this.enabled = false;
    }

    /**
     * Gets the elapsed time.
     * @return a long containing the elapsed time.
     */
    public long getElapsedTime() {
        return this.elapsedTime;
    }

    /**
     * Closes the timed action and prevents it from running again.
     */
    public void close() {
        if (this.closed) {
            return;
        }
        owner.remove(this);
        this.closed = true;
    }

    /**
     * Returns whether the timer was already closed.
     * @return a boolean.
     */
    public boolean isClosed() {
        return this.closed;
    }

}
