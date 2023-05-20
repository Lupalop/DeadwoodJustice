package game;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class TimedActionManager {

    private ArrayList<TimedAction> actions;
    private ArrayList<TimedAction> pendingAdds;
    private ArrayList<TimedAction> pendingRemoves;

    public TimedActionManager() {
        this.actions = new ArrayList<TimedAction>();
        this.pendingAdds = new ArrayList<TimedAction>();
        this.pendingRemoves = new ArrayList<TimedAction>();
    }

    public synchronized void add(long startTime, long endTime,
            boolean autoReset, Callable<Boolean> callback) {
        TimedAction action = new TimedAction(startTime, endTime, autoReset, callback, this);
        this.pendingAdds.add(action);
    }

    public synchronized void add(long endTime, boolean autoReset,
            Callable<Boolean> callback) {
        this.add(System.nanoTime(), endTime, autoReset, callback);
    }

    public synchronized void remove(TimedAction action) {
        this.pendingRemoves.add(action);
    }

    public synchronized void update(long now) {
        for (TimedAction action : this.actions) {
            action.update(now);
        }

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
