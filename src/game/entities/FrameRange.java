package game.entities;

import java.util.concurrent.TimeUnit;

import javafx.scene.image.Image;

public class FrameRange {

    private int[] ranges;

    private static final int START_WALK = 0;
    private static final int START_IMPACT = 2;
    private static final int START_DAMAGE = 4;
    private static final int START_SHOOT = 6;
    private static final int START_DEATH = 8;
    private static final int START_IDLE = 10;

    private static final int END_WALK = 1;
    private static final int END_IMPACT = 3;
    private static final int END_DAMAGE = 5;
    private static final int END_SHOOT = 7;
    private static final int END_DEATH = 9;
    private static final int END_IDLE = 11;

    private static final int TOTAL_RANGES = (6 * 2);

    private static final long SPEED_SHOOT =
            TimeUnit.MILLISECONDS.toNanos(50);
    private static final long SPEED_DAMAGE =
            TimeUnit.MILLISECONDS.toNanos(400);

    public FrameRange(int walkStart, int walkEnd,
            int impactStart, int impactEnd,
            int damageStart, int damageEnd,
            int shootStart, int shootEnd,
            int deathStart, int deathEnd,
            int idleStart, int idleEnd) {
        this.ranges = new int[TOTAL_RANGES];

        this.ranges[START_WALK] = walkStart;
        this.ranges[END_WALK] = walkEnd;

        this.ranges[START_IMPACT] = impactStart;
        this.ranges[END_IMPACT] = impactEnd;

        this.ranges[START_DAMAGE] = damageStart;
        this.ranges[END_DAMAGE] = damageEnd;

        this.ranges[START_SHOOT] = shootStart;
        this.ranges[END_SHOOT] = shootEnd;

        this.ranges[START_DEATH] = deathStart;
        this.ranges[END_DEATH] = deathEnd;

        this.ranges[START_IDLE] = idleStart;
        this.ranges[END_IDLE]= idleEnd;
    }

    public void playWalk(Sprite target) {
        target.setMinMaxFrame(this.ranges[START_WALK], this.ranges[END_WALK]);
    }

    public void playImpact(Sprite target) {
        target.playFrames(this.ranges[START_IMPACT], this.ranges[END_IMPACT]);
    }

    public void playDamage(Sprite target) {
        target.playFrames(this.ranges[START_DAMAGE], this.ranges[END_DAMAGE],
                null, SPEED_DAMAGE);
    }

    public void playShoot(Sprite target, Image frameSetOverride) {
        target.playFrames(this.ranges[START_SHOOT], this.ranges[END_SHOOT],
                frameSetOverride, SPEED_SHOOT);
    }

    public void playShoot(Sprite target) {
        playShoot(target, null);
    }

    public void playDeath(Sprite target) {
        target.setMinMaxFrame(this.ranges[START_DEATH], this.ranges[END_DEATH]);
    }

    public void playIdle(Sprite target) {
        target.setMinMaxFrame(this.ranges[START_IDLE], this.ranges[END_IDLE]);
    }

}
