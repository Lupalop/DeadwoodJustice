package game.entities;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import game.UIUtils;
import game.scenes.LevelScene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Mote extends LevelSprite {

    public static final byte TYPE_NEUTRAL = 0x0;
    public static final byte TYPE_BAD = 0x1;
    public static final byte TYPE_GOOD = 0x2;

    private static final long MOTE_POPUP_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(250);
    private static final long MOTE_DEATH_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(500);

    private static final int OFFSET_FROM_SOURCE_X = 15;
    private static final int OFFSET_FROM_SOURCE_Y = 10;

    private static final int OFFSET_Y_START = -5;
    private static final int OFFSET_Y_END = 0;
    private static final double OPACITY_START = 0;
    private static final double OPACITY_DELTA = 0.2;

    private static final int STROKE_OFFSET = 2;

    private Sprite target;
    private String text;

    private int offsetY;
    private double opacity;

    private byte type;

    public Mote(Sprite target, String text, byte type, LevelScene parent) {
        super(0, 0, parent);
        this.target = target;
        this.text = text;
        this.type = type;

        this.offsetY = OFFSET_Y_START;
        this.opacity = OPACITY_START;

        show();
    }

    public Mote(Sprite target, int number, byte type, LevelScene parent) {
        this(target, Integer.toString(number), type, parent);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();

        gc.setGlobalAlpha(this.opacity);
        gc.setFont(UIUtils.FONT_32);

        // gc.strokeText is inflexible, so we draw the outline on our own.
        gc.setFill(Color.BLACK);
        gc.fillText(this.text,
                this.getX() - STROKE_OFFSET, this.getY() + offsetY);
        gc.fillText(this.text,
                this.getX() + STROKE_OFFSET, this.getY() + offsetY);
        gc.fillText(this.text,
                this.getX(), this.getY() - STROKE_OFFSET + offsetY);
        gc.fillText(this.text,
                this.getX(), this.getY() + STROKE_OFFSET + offsetY);

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

    private void show() {
        getParent().getActions().add(MOTE_POPUP_INTERVAL, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (offsetY == OFFSET_Y_END) {
                    kill();
                    return true;
                }
                offsetY++;
                opacity += OPACITY_DELTA;
                return false;
            }
        });
    }

    private void kill() {
        getParent().getActions().add(MOTE_DEATH_INTERVAL, false, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                getParent().getLevelMap().removeSpriteOnUpdate(Mote.this);
                return true;
            }
        });
    }

}
