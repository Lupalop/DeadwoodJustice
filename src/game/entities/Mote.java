package game.entities;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.UIUtils;
import game.scenes.LevelScene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The Mote class is a sprite representing small text pop-ups
 * that appear for a short period.
 * @author Francis Dominic Fajardo
 */
public final class Mote extends Sprite {

    /** Mote type: neutral (white). */
    public static final byte TYPE_NEUTRAL = 0x0;
    /** Mote type: bad (red). */
    public static final byte TYPE_BAD = 0x1;
    /** Mote type: good (green). */
    public static final byte TYPE_GOOD = 0x2;

    /** Interval for the mote pop-up transition. */
    private static final long MOTE_POPUP_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(250);
    /** Interval for the mote removal transition. */
    private static final long MOTE_DEATH_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(500);

    /** Offset: from sprite source (x-coordinate. */
    private static final int OFFSET_FROM_SOURCE_X = 15;
    /** Offset: from sprite source (y-coordinate). */
    private static final int OFFSET_FROM_SOURCE_Y = 10;
    /** Offset: before pop-up. */
    private static final int OFFSET_Y_START = -5;
    /** Offset: after pop-up. */
    private static final int OFFSET_Y_END = 0;
    /** Offset: stroked text from center. */
    private static final int OFFSET_STROKE = 2;
    /** Initial opacity transition. */
    private static final double OPACITY_START = 0;
    /** Opacity change value. */
    private static final double OPACITY_DELTA = 0.2;

    /** Sprite object representing the mote's source. */
    private Sprite target;
    /** Text in the mote. */
    private String text;
    /** Current y-coordinate offset. */
    private int offsetY;
    /** Current opacity. */
    private double opacity;
    /** Current mote type. */
    private byte type;

    /**
     * Creates a new instance of the Mote class.
     * @param target the Sprite from which the mote originated.
     * @param text the text contained by the mote.
     * @param type the type of the mote (constant).
     */
    public Mote(Sprite target, String text, byte type) {
        super(0, 0);
        this.target = target;
        this.text = text;
        this.type = type;

        this.offsetY = OFFSET_Y_START;
        this.opacity = OPACITY_START;
    }

    /**
     * Creates a new instance of the Mote class.
     * @param target the Sprite from which the mote originated.
     * @param text the number contained by the mote.
     * @param type the type of the mote (constant).
     */
    public Mote(Sprite target, int number, byte type) {
        this(target, Integer.toString(number), type);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();

        gc.setGlobalAlpha(this.opacity);
        gc.setFont(UIUtils.FONT_32);

        // gc.strokeText is inflexible, so we draw the outline on our own.
        gc.setFill(Color.BLACK);
        gc.fillText(this.text,
                this.getX() - OFFSET_STROKE, this.getY() + offsetY);
        gc.fillText(this.text,
                this.getX() + OFFSET_STROKE, this.getY() + offsetY);
        gc.fillText(this.text,
                this.getX(), this.getY() - OFFSET_STROKE + offsetY);
        gc.fillText(this.text,
                this.getX(), this.getY() + OFFSET_STROKE + offsetY);

        if (this.type == TYPE_NEUTRAL) {
            gc.setFill(Color.WHITE);
        } else if (this.type == TYPE_BAD) {
            gc.setFill(Color.RED);
        } else if (this.type == TYPE_GOOD) {
            gc.setFill(Color.LIMEGREEN);
        }
        gc.fillText(this.text, this.getX(), this.getY() + offsetY);

        gc.restore();
    }

    @Override
    public void update(long now) {
        this.setX((int) this.target.getBounds().getMaxX() - OFFSET_FROM_SOURCE_X);
        this.setY((int) this.target.getBounds().getMaxY() + OFFSET_FROM_SOURCE_Y);
    }

    /**
     * Shows this mote on the canvas.
     * @param level the LevelScene object containing this mote.
     */
    public void show(LevelScene level) {
        level.getActions().add(MOTE_POPUP_INTERVAL, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (offsetY == OFFSET_Y_END) {
                    kill(level);
                    return true;
                }
                offsetY++;
                opacity += OPACITY_DELTA;
                return false;
            }
        });
    }

    /**
     * Kills this mote on the canvas.
     * @param level the LevelScene object containing this mote.
     */
    private void kill(LevelScene level) {
        level.getActions().add(MOTE_DEATH_INTERVAL, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                remove();
                return true;
            }
        });
    }

}
