package game;

import java.util.ArrayList;

public class TimedActionManager {

    private ArrayList<TimedAction> actions;
    private ArrayList<TimedAction> pendingAdds;
    private ArrayList<TimedAction> pendingRemoves;
    private long lastUpdateTime;

    TimedActionManager() {
        this.actions = new ArrayList<TimedAction>();
        this.pendingAdds = new ArrayList<TimedAction>();
        this.pendingRemoves = new ArrayList<TimedAction>();
        this.lastUpdateTime = 0;
    }

    void add(TimedAction action) {
        this.pendingAdds.add(action);
    }

    void remove(TimedAction action) {
        this.pendingRemoves.add(action);
    }

    void clear() {
        this.pendingRemoves.addAll(this.actions);
    }

    void update(long now) {
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
