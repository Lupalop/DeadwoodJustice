package game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * This class is responsible for adding, removing, updating,
 * starting, and stopping action timers.
 * @author Francis Dominic Fajardo
 */
public class ActionTimerManager {

    /** A list containing action timers. */
    private ArrayList<ActionTimer> actions;
    /** A list containing action timers to be added. */
    private ArrayList<ActionTimer> pendingAdds;
    /** A list containing action timers to be removed. */
    private ArrayList<ActionTimer> pendingRemoves;
    /** Stores the time of the last update call. */
    private long lastUpdateTime;
    /** Whether this manager should process updates. */
    private boolean enabled;

    /**
     * Constructs an empty instance of ActionTimerManager.
     */
    public ActionTimerManager() {
        this.actions = new ArrayList<ActionTimer>();
        this.pendingAdds = new ArrayList<ActionTimer>();
        this.pendingRemoves = new ArrayList<ActionTimer>();
        this.lastUpdateTime = 0;
        this.enabled = true;
    }

    /**
     * Adds an action timer.
     * @param timer an ActionTimer object.
     */
    public synchronized void add(ActionTimer timer) {
        this.pendingAdds.add(timer);
    }

    /**
     * Adds an action timer.
     * @param interval the time to wait before the task is invoked.
     * @param autoReset determines if the timer should reset after completion.
     * @param elapsed task invoked when time is up.
     * @return an ActionTimer object.
     */
    public synchronized ActionTimer add(long interval, boolean autoReset,
            Callable<Boolean> elapsed) {
        ActionTimer action = new ActionTimer(
                interval, autoReset, elapsed, this);
        this.add(action);
        return action;
    }

    /**
     * Adds multiple action timers from the specified collection.
     * @param actions a collection containing action timers.
     */
    public synchronized void addAll(Collection<ActionTimer> actions) {
        this.pendingAdds.addAll(actions);
    }

    /**
     * Removes an action timer.
     * @param action an ActionTimer object.
     */
    public synchronized void remove(ActionTimer action) {
        this.pendingRemoves.add(action);
    }

    /**
     * Removes multiple action timers from the specified collection.
     * @param actions a collection containing action timers.
     */
    public synchronized void removeAll(Collection<ActionTimer> actions) {
        this.pendingRemoves.addAll(actions);
    }

    /**
     * Removes all action timers.
     */
    public synchronized void removeAll() {
        this.removeAll(this.actions);
    }

    /**
     * Stops all action timers.
     */
    public synchronized void stopAll() {
        this.enabled = false;
    }

    /**
     * Starts all action timers.
     */
    public synchronized void startAll() {
        this.enabled = true;
    }

    /**
     * Updates the state of all action timers.
     * @param now The timestamp of the current frame given in nanoseconds.
     */
    public synchronized void update(long now) {
        // Pass the delta time when updating action timers.
        long deltaTime = (now - this.lastUpdateTime);
        if (this.enabled) {
            for (ActionTimer action : this.actions) {
                action.update(deltaTime);
            }
        }
        // Store the time of this current update call.
        this.lastUpdateTime = now;

        // Process all action timers to be added, if any.
        if (this.pendingAdds.size() > 0) {
            this.actions.addAll(this.pendingAdds);
            this.pendingAdds.clear();
        }

        // Process all action timers to be removed, if any.
        if (this.pendingRemoves.size() > 0) {
            this.actions.removeAll(this.pendingRemoves);
            this.pendingRemoves.clear();
        }
    }

}
