package game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * This class is responsible for adding, removing, updating,
 * starting, and stopping timed actions.
 * @author Francis Dominic Fajardo
 */
public class TimedActionManager {

    /** A list containing timed actions. */
    private ArrayList<TimedAction> actions;
    /** A list containing timed actions to be added. */
    private ArrayList<TimedAction> pendingAdds;
    /** A list containing timed actions to be removed. */
    private ArrayList<TimedAction> pendingRemoves;
    /** Stores the time of the last update call. */
    private long lastUpdateTime;
    /** Whether this manager should process updates. */
    private boolean enabled;

    /**
     * Constructs an empty instance of TimedActionManager.
     */
    public TimedActionManager() {
        this.actions = new ArrayList<TimedAction>();
        this.pendingAdds = new ArrayList<TimedAction>();
        this.pendingRemoves = new ArrayList<TimedAction>();
        this.lastUpdateTime = 0;
        this.enabled = true;
    }

    /**
     * Adds a timed action.
     * @param action the TimedAction object.
     */
    public synchronized void add(TimedAction action) {
        this.pendingAdds.add(action);
    }

    /**
     * Adds a timed action.
     * @param interval the time to wait before the task is invoked.
     * @param autoReset determines if the timer should reset after completion.
     * @param elapsed task invoked when time is up.
     * @return a TimedAction object.
     */
    public synchronized TimedAction add(long interval, boolean autoReset,
            Callable<Boolean> elapsed) {
        TimedAction action = new TimedAction(
                interval, autoReset, elapsed, this);
        this.add(action);
        return action;
    }

    /**
     * Adds multiple timed actions from a given collection.
     * @param actions a collection containing timed actions.
     */
    public synchronized void addAll(Collection<TimedAction> actions) {
        this.pendingAdds.addAll(actions);
    }

    /**
     * Removes a timed action.
     * @param action a TimedAction object.
     */
    public synchronized void remove(TimedAction action) {
        this.pendingRemoves.add(action);
    }

    /**
     * Removes multiple timed actions from a given collection.
     * @param actions a collection containing timed actions.
     */
    public synchronized void removeAll(Collection<TimedAction> actions) {
        this.pendingRemoves.addAll(actions);
    }

    /**
     * Removes all timed actions.
     */
    public synchronized void removeAll() {
        this.removeAll(this.actions);
    }

    /**
     * Stops all timed actions.
     */
    public synchronized void stopAll() {
        this.enabled = false;
    }

    /**
     * Starts all timed actions.
     */
    public synchronized void startAll() {
        this.enabled = true;
    }

    /**
     * Updates the state of all timed actions.
     * @param now The timestamp of the current frame given in nanoseconds.
     */
    public synchronized void update(long now) {
        // Pass the delta time when updating timed actions.
        long deltaTime = (now - this.lastUpdateTime);
        if (this.enabled) {
            for (TimedAction action : this.actions) {
                action.update(deltaTime);
            }
        }
        // Store the time of this current update call.
        this.lastUpdateTime = now;

        // Process all timed actions to be added, if any.
        if (this.pendingAdds.size() > 0) {
            this.actions.addAll(this.pendingAdds);
            this.pendingAdds.clear();
        }

        // Process all timed actions to be removed, if any.
        if (this.pendingRemoves.size() > 0) {
            this.actions.removeAll(this.pendingRemoves);
            this.pendingRemoves.clear();
        }
    }

}
