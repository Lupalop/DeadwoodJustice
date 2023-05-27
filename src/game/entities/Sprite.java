package game.entities;

import java.util.concurrent.TimeUnit;

import game.Game;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class Sprite implements Comparable<Sprite> {

    public static final int DEFAULT_SCALE = 2;

    public static final int SIDE_LEFT = 0;
    public static final int SIDE_RIGHT = 1;
    public static final int SIDE_TOP = 2;
    public static final int SIDE_BOTTOM = 3;
    public static final int SIDE_INVALID = -1;

    protected int dx, dy;
    protected boolean boundsDirty;

    private static final long DEFAULT_FRAME_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(100);
    private static final int BASE_DIVIDER = 4;

    private Image image;
    private int x, y;
    private double width;
    private double height;
    private Rectangle2D bounds;
    private Rectangle2D baseBounds;
    private int scale;
    private boolean visible;
    private boolean flipHorizontal;
    private boolean flipVertical;

    private Rectangle2D sourceRectangles[];
    private int frame;
    private int totalFrames;
    private long lastFrameTime;
    private long frameInterval;

    private int minFrame;
    private int maxFrame;

    private boolean frameAutoReset;
    private boolean frameSequenceDone;
    private boolean hasFrameOverride;
    private Image overrideImage;
    private int overrideMinFrame;
    private int overrideMaxFrame;
    private long overrideFrameInterval;

    private int[] boundsOffset;

    public Sprite(int x, int y) {
        this.dx = 0;
        this.dy = 0;
        this.boundsDirty = true;

        this.x = x;
        this.y = y;
        this.width = 0;
        this.height = 0;
        this.bounds = null;
        this.baseBounds = null;
        this.scale = DEFAULT_SCALE;
        this.visible = true;
        this.flipHorizontal = false;
        this.flipVertical = false;

        this.sourceRectangles = null;
        this.frame = -1;
        this.totalFrames = 0;
        this.lastFrameTime = -1;
        this.frameInterval = DEFAULT_FRAME_INTERVAL;
        this.minFrame = -1;
        this.maxFrame = -1;
        this.frameSequenceDone = false;
        this.frameAutoReset = true;
        this.hasFrameOverride = false;
        this.overrideImage = null;
        this.overrideMinFrame = -1;
        this.overrideMaxFrame = -1;
        this.overrideFrameInterval = -1;

        this.boundsOffset = null;
    }

    public Sprite() {
        this(0, 0);
    }

    public void update(long now) {
        if (this.frame == -1 || this.totalFrames == 0
                || this.maxFrame == -1 || this.minFrame == -1) {
            return;
        }

        long deltaTime = (now - lastFrameTime);
        if (lastFrameTime == -1) {
            this.lastFrameTime = now;
            return;
        }

        long realFrameInterval = this.frameInterval;
        if (this.overrideFrameInterval > 0) {
            realFrameInterval = this.overrideFrameInterval;
        }

        if (deltaTime < realFrameInterval) {
            return;
        }

        if (this.frameAutoReset || !this.frameSequenceDone) {
            this.frame++;
        }

        if (this.hasFrameOverride) {
            if (this.frame == this.overrideMaxFrame) {
                this.clearFrameOverride();
            }
        } else if (this.frame == this.maxFrame) {
            if (this.frameAutoReset) {
                this.frame = this.minFrame;
            } else {
                this.frameSequenceDone = true;
            }
        }

        this.lastFrameTime = now;
    }


    public void draw(GraphicsContext gc) {
        double flipOffsetX = (flipHorizontal ? (this.getWidth() * this.getScale()) : 0);
        double flipOffsetY = (flipVertical ? (this.getHeight() * this.getScale()) : 0);
        double flipMultiplierWidth = (flipHorizontal ? -1 : 1);
        double flipMultiplierHeight = (flipVertical ? -1 : 1);

        if (frame != -1 && totalFrames != 0) {
            Rectangle2D source = this.sourceRectangles[frame];
            gc.drawImage(
                    this.getImage(),
                    source.getMinX(),
                    source.getMinY(),
                    source.getWidth(),
                    source.getHeight(),
                    this.getX() + flipOffsetX,
                    this.getY() + flipOffsetY,
                    this.getWidth() * this.getScale() * flipMultiplierWidth,
                    this.getHeight() * this.getScale() * flipMultiplierHeight);
        } else {
            gc.drawImage(
                    this.getImage(),
                    this.getX() + flipOffsetX,
                    this.getY() + flipOffsetY,
                    this.getWidth() * this.getScale() * flipMultiplierWidth,
                    this.getHeight() * this.getScale() * flipMultiplierHeight);
        }

        if (Game.DEBUG_MODE) {
            gc.save();
            gc.setStroke(javafx.scene.paint.Color.RED);
            gc.strokeRect(
                    this.getBounds().getMinX(), this.getBounds().getMinY(),
                    this.getBounds().getWidth(), this.getBounds().getHeight());
            gc.setStroke(javafx.scene.paint.Color.GREEN);
            gc.strokeRect(
                    this.getBaseBounds().getMinX(), this.getBaseBounds().getMinY(),
                    this.getBaseBounds().getWidth(), this.getBaseBounds().getHeight());
            gc.restore();
        }
    }

    private boolean intersects(Rectangle2D r1, Rectangle2D r2,
            boolean xIgnore, boolean yIgnore) {
        if (xIgnore || yIgnore) {
            r2 = new Rectangle2D(
                    xIgnore ? r1.getMinX() : r2.getMinX(),
                    yIgnore ? r1.getMinY() : r2.getMinY(),
                    r2.getWidth(),
                    r2.getHeight());
        }

        return r1.intersects(r2);
    }

    public boolean intersects(Sprite target,
            boolean xIgnore, boolean yIgnore) {
        return intersects(this.getBounds(), target.getBounds(),
                xIgnore, yIgnore);
    }

    public boolean intersects(Sprite target) {
        return intersects(target, false, false);
    }

    public boolean intersectsBase(Sprite target,
            boolean xIgnore, boolean yIgnore) {
        return intersects(this.getBaseBounds(), target.getBaseBounds(),
                xIgnore, yIgnore);
    }

    private int intersectsSide(Rectangle2D r1, Rectangle2D r2) {
        // Check if the rectangles intersect.
        if (!r1.intersects(r2)) {
            return -1;
        }

        // Check which sides of the rectangles intersect.
        if (r1.getMinX() < r2.getMaxX() && r1.getMinX() > r2.getMinX()) {
            return SIDE_LEFT; // Left side of r1 intersects right side of r2.
        } else if (r2.getMinX() < r1.getMaxX() && r2.getMinX() > r1.getMinX()) {
            return SIDE_RIGHT; // Right side of r1 intersects left side of r2.
        } else if (r1.getMinY() < r2.getMaxY() && r1.getMinY() > r2.getMinY()) {
            return SIDE_TOP; // Top side of r1 intersects bottom side of r2.
        } else if (r2.getMinY() < r1.getMaxY() && r2.getMinY() > r1.getMinY()) {
            return SIDE_BOTTOM; // Bottom side of r1 intersects top side of r2.
        }

        // Should never reach this point.
        return SIDE_INVALID;
    }

    public int intersectsSide(Rectangle2D r2) {
        return intersectsSide(this.getBounds(), r2);
    }

    public int baseIntersectsSide(Rectangle2D r2) {
        return intersectsSide(this.getBaseBounds(), r2);
    }

    protected void playFrames(int min, int max, Image frameSetOverride, long frameIntervalOverride) {
        // An existing frame range is already being played temporarily
        // or an invalid value was passed to min/max parameters.
        if (this.hasFrameOverride || min == -1 || max == -1) {
            return;
        }
        boolean shouldReset =
                this.overrideMinFrame != min || this.overrideMaxFrame != max;
        this.overrideMinFrame = min;
        this.overrideMaxFrame = max;
        if (frameSetOverride != null) {
            this.overrideImage = frameSetOverride;
        }
        if (frameIntervalOverride > 0) {
            this.overrideFrameInterval = frameIntervalOverride;
        }
        this.hasFrameOverride = true;
        if (shouldReset) {
            this.frameSequenceDone = false;
            this.frame = min;
        }
    }

    private void clearFrameOverride() {
        this.overrideMinFrame = -1;
        this.overrideMaxFrame = -1;
        this.overrideFrameInterval = -1;
        this.overrideImage = null;
        this.hasFrameOverride = false;
        this.frameSequenceDone = true;
        this.frame = this.minFrame;
    }

    public Rectangle2D getBounds() {
        if (this.boundsDirty) {
            double newX = this.getX();
            double newY = this.getY();
            double newWidth = this.getWidth();
            double newHeight = this.getHeight();

            if (this.boundsOffset != null) {
                if (this.flipHorizontal) {
                    newX += this.boundsOffset[1] * scale;
                } else {
                    newX += this.boundsOffset[0] * scale;
                }
                newWidth -= this.boundsOffset[0] + this.boundsOffset[1];

                if (this.flipVertical) {
                    newY += this.boundsOffset[3] * scale;
                } else {
                    newY += this.boundsOffset[2] * scale;
                }
                newHeight -= this.boundsOffset[2] + this.boundsOffset[3];
            }
            newWidth *= scale;
            newHeight *= scale;

            this.bounds = new Rectangle2D(
                    newX, newY, newWidth, newHeight);

            double baseHeight = newHeight / BASE_DIVIDER;
            this.baseBounds = new Rectangle2D(
                    newX, newY + newHeight - baseHeight,
                    newWidth, baseHeight);
        }

        return this.bounds;
    }

    public Rectangle2D getBaseBounds() {
        if (this.boundsDirty) {
            this.getBounds();
        }
        return this.baseBounds;
    }

    public Image getImage() {
        if (this.overrideImage != null) {
            return this.overrideImage;
        }
        return this.image;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public int getScale() {
        return this.scale;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public boolean isFrameAutoReset() {
        return this.frameAutoReset;
    }

    public boolean isFrameSequenceDone() {
        return this.frameSequenceDone;
    }

    public void setX(int x) {
        this.x = x;
        this.boundsDirty = true;
    }

    public void setY(int y) {
        this.y = y;
        this.boundsDirty = true;
    }

    public void addX(int x) {
        this.setX(this.getX() + x);
    }

    public void addY(int y) {
        this.setY(this.getY() + y);
    }

    protected void setImage(Image image) {
        this.image = image;
        this.boundsDirty = (this.getWidth() != image.getWidth()
                || this.getHeight() != image.getHeight());
        if (boundsDirty) {
            this.width = image.getWidth();
            this.height = image.getHeight();
        }
    }

    public void setWidth(double val) {
        this.width = val;
        this.boundsDirty = true;
    }

    public void setHeight(double val) {
        this.height = val;
        this.boundsDirty = true;
    }

    public void setScale(int scale) {
        this.scale = scale;
        this.boundsDirty = true;
    }

    public void setVisible(boolean value) {
        this.visible = value;
    }

    protected void setFlip(boolean value, boolean isVertical) {
        if (isVertical) {
            this.flipVertical = value;
            this.boundsDirty = this.boundsDirty || (this.flipVertical != value);
        } else {
            this.flipHorizontal = value;
            this.boundsDirty = this.boundsDirty || (this.flipHorizontal != value);
        }
    }

    protected void setFrameSet(Image frameSet, int rows, int columns, boolean reframe) {
        if (this.image != frameSet) {
            this.image = frameSet;
        }

        if (!reframe) {
            return;
        }

        this.width = (int) (frameSet.getWidth() / columns);
        this.height = (int) (frameSet.getHeight() / rows);
        this.boundsDirty = true;

        this.totalFrames = rows * columns;
        this.setMinMaxFrame(0, totalFrames);

        // Prepare source rectangles for each frame.
        this.sourceRectangles = new Rectangle2D[this.totalFrames];
        int sourceFrame = 0;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                sourceRectangles[sourceFrame] = new Rectangle2D(
                        this.getWidth() * column,
                        this.getHeight() * row,
                        this.getWidth(),
                        this.getHeight());
                sourceFrame++;
            }
        }
    }

    protected void setFrameSet(Image frameSet, int rows, int columns) {
        this.setFrameSet(frameSet, rows, columns, true);
    }

    protected void setFrameSet(Image frameSet) {
        this.setFrameSet(frameSet, -1, -1, false);
    }

    protected void setFrame(int frame) {
        this.frame = frame;
    }

    protected void setFrameInterval(long interval) {
        this.frameInterval = interval;
    }

    protected void setMinMaxFrame(int min, int max) {
        boolean shouldReset =
                this.minFrame != min || this.maxFrame != max;
        this.minFrame = min;
        this.maxFrame = max;
        if (shouldReset) {
            this.clearFrameOverride();
            this.frameSequenceDone = false;
            this.frame = min;
        }
    }

    protected void setFrameAutoReset(boolean frameAutoReset) {
        this.frameAutoReset = frameAutoReset;
    }

    protected void setBoundsOffset(int boundsOffset[]) {
        this.boundsOffset = boundsOffset;
        this.boundsDirty = true;
    }

    @Override
    public int compareTo(Sprite o) {
        return (int) (this.getBounds().getMaxY() - o.getBounds().getMaxY());
    }

}
