package game;

import java.util.ArrayList;
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
            Callable<Boolean> callback) {
        TimedAction action = new TimedAction(
                interval, autoReset, callback, this);
        this.pendingAdds.add(action);
    }

    public synchronized void remove(TimedAction action) {
        this.pendingRemoves.add(action);
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
