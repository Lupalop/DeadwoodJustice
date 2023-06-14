package game.entities;

import java.util.concurrent.TimeUnit;

import javafx.scene.image.Image;

/**
 * The FrameRange class holds the frame ranges for the different states
 * of a mob/outlaw and sets the appropriate minimum or maximum values
 * or plays the requested frames.
 * @author Francis Dominic Fajardo
 */
public final class FrameRange {

    /** An integer array storing all frame ranges. */
    private int[] ranges;

    /** Start range: walk. */
    private static final int START_WALK = 0;
    /** Start range: impact. */
    private static final int START_IMPACT = 2;
    /** Start range: damage. */
    private static final int START_DAMAGE = 4;
    /** Start range: shoot. */
    private static final int START_SHOOT = 6;
    /** Start range: death. */
    private static final int START_DEATH = 8;
    /** Start range: idle. */
    private static final int START_IDLE = 10;

    /** End range: walk. */
    private static final int END_WALK = 1;
    /** End range: impact. */
    private static final int END_IMPACT = 3;
    /** End range: damage. */
    private static final int END_DAMAGE = 5;
    /** End range: shoot. */
    private static final int END_SHOOT = 7;
    /** End range: death. */
    private static final int END_DEATH = 9;
    /** End range: idle. */
    private static final int END_IDLE = 11;

    /** Total number of frame ranges stored. */
    private static final int TOTAL_RANGES = (6 * 2);

    /** The speed of changing frames for the shoot state. */
    private static final long SPEED_SHOOT =
            TimeUnit.MILLISECONDS.toNanos(50);
    /** The speed of changing frames for the damaged state. */
    private static final long SPEED_DAMAGE =
            TimeUnit.MILLISECONDS.toNanos(400);

    /**
     * Creates a new instance of the FrameRange class.
     * @param walkStart start range: walk.
     * @param walkEnd end range: walk.
     * @param impactStart start range: impact.
     * @param impactEnd end range: impact.
     * @param damageStart start range: damage.
     * @param damageEnd end range: damage.
     * @param shootStart start range: shoot.
     * @param shootEnd end range: shoot.
     * @param deathStart start range: death.
     * @param deathEnd end range: death.
     * @param idleStart start range: idle.
     * @param idleEnd end range: idle
     */
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

    /**
     * Plays the walk frame range.
     * @param target a Sprite object.
     */
    public void playWalk(Sprite target) {
        target.setMinMaxFrame(this.ranges[START_WALK], this.ranges[END_WALK]);
    }

    /**
     * Plays the damaged on impact frame range.
     * @param target a Sprite object.
     */
    public void playImpact(Sprite target) {
        target.playFrames(this.ranges[START_IMPACT], this.ranges[END_IMPACT]);
    }

    /**
     * Plays the damaged frame range.
     * @param target a Sprite object.
     */
    public void playDamage(Sprite target) {
        target.playFrames(this.ranges[START_DAMAGE], this.ranges[END_DAMAGE],
                null, SPEED_DAMAGE);
    }

    /**
     * Plays the shoot frame range.
     * @param target a Sprite object.
     * @param frameSetOverride custom frame set image.
     */
    public void playShoot(Sprite target, Image frameSetOverride) {
        target.playFrames(this.ranges[START_SHOOT], this.ranges[END_SHOOT],
                frameSetOverride, SPEED_SHOOT);
    }

    /**
     * Plays the shoot frame range.
     * @param target a Sprite object.
     */
    public void playShoot(Sprite target) {
        playShoot(target, null);
    }

    /**
     * Plays the death frame range.
     * @param target a Sprite object.
     */
    public void playDeath(Sprite target) {
        target.setMinMaxFrame(this.ranges[START_DEATH], this.ranges[END_DEATH]);
    }

    /**
     * Plays the idle frame range.
     * @param target a Sprite object.
     */
    public void playIdle(Sprite target) {
        target.setMinMaxFrame(this.ranges[START_IDLE], this.ranges[END_IDLE]);
    }

}
