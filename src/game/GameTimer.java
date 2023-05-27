package game;

import java.util.ArrayList;
import java.util.Collection;

import javafx.animation.AnimationTimer;

public final class GameTimer extends AnimationTimer {

    private ArrayList<TimedAction> actions;
    private ArrayList<TimedAction> pendingAdds;
    private ArrayList<TimedAction> pendingRemoves;
    private long lastUpdateTime;

    GameTimer() {
        this.actions = new ArrayList<TimedAction>();
        this.pendingAdds = new ArrayList<TimedAction>();
        this.pendingRemoves = new ArrayList<TimedAction>();
        this.lastUpdateTime = 0;
    }

    @Override
    public void handle(long now) {
        if (Game.getGameScene() == null) {
            return;
        }

        this.updateActions(now);
        Game.getGameScene().update(now);
        Game.getGameScene().draw(now);
    }

    void add(TimedAction action) {
        this.pendingAdds.add(action);
    }

    void addAll(Collection<TimedAction> actions) {
        this.pendingAdds.addAll(actions);
    }

    void remove(TimedAction action) {
        this.pendingRemoves.add(action);
    }

    void removeAll(Collection<TimedAction> actions) {
        this.pendingRemoves.addAll(actions);
    }

    void stopAll(Collection<TimedAction> actions) {
        for (TimedAction action : actions) {
            action.stop();
        }
    }

    void startAll(Collection<TimedAction> actions) {
        for (TimedAction action : actions) {
            action.start();
        }
    }

    void clear() {
        this.pendingRemoves.addAll(this.actions);
    }

    void updateActions(long now) {
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
