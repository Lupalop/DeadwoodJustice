package game;

public class TimedAction {

    private long startTime;
    private long endTime;
    private boolean autoReset;
    private Runnable callback;
    private TimedActionManager owner;

    public TimedAction(long startTime, long endTime, boolean autoReset,
            Runnable callback, TimedActionManager owner) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.autoReset = autoReset;
        this.callback = callback;
        this.owner = owner;
    }

    public void update(long now) {
        long deltaTime = (now - this.startTime);
        if (deltaTime >= this.endTime) {
            callback.run();
            if (autoReset) {
                this.startTime = now;
            } else {
                owner.remove(this);
            }
        }
    }

}
