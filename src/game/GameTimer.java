package game;

import java.util.ArrayList;

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

    void remove(TimedAction action) {
        this.pendingRemoves.add(action);
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
