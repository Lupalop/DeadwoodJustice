package game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

public class TimedActionManager {

    private ArrayList<TimedAction> actions;
    private ArrayList<TimedAction> pendingAdds;
    private ArrayList<TimedAction> pendingRemoves;
    private long lastUpdateTime;

    public TimedActionManager() {
        this.actions = new ArrayList<TimedAction>();
        this.pendingAdds = new ArrayList<TimedAction>();
        this.pendingRemoves = new ArrayList<TimedAction>();
        this.lastUpdateTime = 0;
    }

    public synchronized void add(long interval, boolean autoReset,
            Callable<Boolean> elapsed) {
        TimedAction action = new TimedAction(
                interval, autoReset, elapsed, this);
        this.pendingAdds.add(action);
    }

    public synchronized void addAll(Collection<TimedAction> actions) {
        this.pendingAdds.addAll(actions);
    }

    public synchronized void remove(TimedAction action) {
        this.pendingRemoves.add(action);
    }

    public synchronized void removeAll(Collection<TimedAction> actions) {
        this.pendingRemoves.addAll(actions);
    }

    public synchronized void removeAll() {
        this.removeAll(this.actions);
    }

    public synchronized void stopAll(Collection<TimedAction> actions) {
        for (TimedAction action : actions) {
            action.stop();
        }
    }

    public synchronized void stopAll() {
        this.stopAll(this.actions);
    }

    public synchronized void startAll(Collection<TimedAction> actions) {
        for (TimedAction action : actions) {
            action.start();
        }
    }

    public synchronized void startAll() {
        this.startAll(this.actions);
    }

    public synchronized void update(long now) {
        long deltaTime = (now - this.lastUpdateTime);
        for (TimedAction action : this.actions) {
            action.update(deltaTime);
        }
        this.lastUpdateTime = now;

        if (this.pendingAdds.size() > 0) {
            this.actions.addAll(this.pendingAdds);
            this.pendingAdds.clear();
        }

        if (this.pendingRemoves.size() > 0) {
            this.actions.removeAll(this.pendingRemoves);
            this.pendingRemoves.clear();
        }
    }

}
