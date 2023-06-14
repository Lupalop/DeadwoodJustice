package game.entities;

import java.util.concurrent.TimeUnit;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * The Sprite class is responsible for all the graphics drawn
 * on the game canvas.
 * @author Francis Dominic Fajardo
 */
public abstract class Sprite implements Comparable<Sprite> {

    /** The default sprite scale. */
    public static final int BASE_SCALE = 2;

    /** Delta x-coordinate change. */
    protected int dx;
    /** Delta y-coordinate change. */
    protected int dy;
    /** Whether bounds should be updated. */
    protected boolean boundsDirty;

    /** The default speed of changing frames. */
    private static final long DEFAULT_FRAME_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(100);

    /** The image associated with this sprite. */
    private Image image;
    /** The x-coordinate position. */
    private int x;
    /** The y-coordinate position. */
    private int y;
    /** The width of this sprite. */
    private double width;
    /** The height of this sprite. */
    private double height;
    /** The rectangular bounds of this sprite. */
    private Rectangle2D bounds;
    /** The scale of this sprite. */
    private int scale;
    /** Whether this sprite is visible. */
    private boolean visible;
    /** Whether this sprite is about to be removed. */
    private boolean removed;
    /** Whether this sprite is flipped horizontally. */
    private boolean flipHorizontal;
    /** Whether this sprite is flipped vertically. */
    private boolean flipVertical;

    /** An array containing rectangles for each frame in the image. */
    private Rectangle2D sourceRectangles[];
    /** The current frame number. */
    private int frame;
    /** The total number of frames. */
    private int totalFrames;
    /** The last time since changing frames. */
    private long lastFrameTime;
    /** The speed of changing frames. */
    private long frameInterval;
    /** Frame sequence: start range. */
    private int minFrame;
    /** Frame sequence: end range. */
    private int maxFrame;
    /** Whether to start the frame sequence again. */
    private boolean frameAutoReset;
    /** Whether the frame sequence is complete. */
    private boolean frameSequenceDone;
    /** Whether the frame set and/or ranges were overridden. */
    private boolean hasFrameOverride;
    /** Override: sprite image. */
    private Image overrideImage;
    /** Override: sequence start range. */
    private int overrideMinFrame;
    /** Override: sequence end range. */
    private int overrideMaxFrame;
    /** Override: sequence frame change speed. */
    private long overrideFrameInterval;
    /** Frame offset from all sides (used to remove transparent space). */
    private int[] boundsOffset;

    /**
     * Creates a new instance of the Sprite class.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     */
    public Sprite(int x, int y) {
        this.dx = 0;
        this.dy = 0;
        this.boundsDirty = true;

        this.x = x;
        this.y = y;
        this.width = 0;
        this.height = 0;
        this.bounds = null;
        this.scale = BASE_SCALE;
        this.visible = true;
        this.removed = false;
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

    /**
     * Creates an instance of the Sprite class at position (0, 0).
     */
    public Sprite() {
        this(0, 0);
    }

    /**
     * Updates the sprite state.
     * @param now The timestamp of the current frame given in nanoseconds.
     */
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

    /**
     * Draws this sprite on the canvas.
     * @param gc a GraphicsContext object.
     */
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
    }

    /**
     * Plays the given frame sequence.
     * @param min the frame sequence start range.
     * @param max the frame sequence end range.
     * @param frameSetOverride the custom frame set image.
     * @param frameIntervalOverride the custom frame sequence change speed.
     */
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

    /**
     * Plays the given frame sequence.
     * @param min the frame sequence start range.
     * @param max the frame sequence end range.
     */
    protected void playFrames(int min, int max) {
        this.playFrames(min, max, null, 0);
    }

    /**
     * Clears overrides imposed on this sprite.
     */
    private void clearFrameOverride() {
        this.overrideMinFrame = -1;
        this.overrideMaxFrame = -1;
        this.overrideFrameInterval = -1;
        this.overrideImage = null;
        this.hasFrameOverride = false;
        this.frameSequenceDone = true;
        this.frame = this.minFrame;
    }

    /**
     * Gets the value of the bounds property.
     * @return a Rectangle2D object.
     */
    public Rectangle2D getBounds() {
        if (this.boundsDirty) {
            this.resizeBounds();
            this.boundsDirty = false;
        }

        return this.bounds;
    }

    /**
     * Updates the value of the bounds property.
     * @return a Rectangle2D object.
     */
    protected Rectangle2D resizeBounds() {
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

        return this.bounds;
    }

    /**
     * Gets the value of the image property.
     * @return an Image object.
     */
    public Image getImage() {
        if (this.overrideImage != null) {
            return this.overrideImage;
        }
        return this.image;
    }

    /**
     * Gets the value of the x-coordinate position property.
     * @return an integer.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Gets the value of the y-coordinate position property.
     * @return an integer.
     */
    public int getY() {
        return this.y;
    }

    /**
     * Gets the value of the width property.
     * @return a double.
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * Gets the value of the height property.
     * @return a double.
     */
    public double getHeight() {
        return this.height;
    }

    /**
     * Gets the value of the scale property.
     * @return an integer.
     */
    public int getScale() {
        return this.scale;
    }

    /**
     * Gets the value of the visible property.
     * @return a boolean.
     */
    public boolean getVisible() {
        return this.visible;
    }

    /**
     * Gets the value of the marked for removal property.
     * @return a boolean.
     */
    public boolean getRemoved() {
        return this.removed;
    }

    /**
     * Gets the value of the auto-reset frame sequence property.
     * @return a boolean.
     */
    public boolean isFrameAutoReset() {
        return this.frameAutoReset;
    }

    /**
     * Gets the value of the frame sequence done property.
     * @return a boolean.
     */
    public boolean isFrameSequenceDone() {
        return this.frameSequenceDone;
    }

    /**
     * Sets the value of the x-coordinate position property.
     * @param x an integer.
     */
    public void setX(int x) {
        this.x = x;
        this.boundsDirty = true;
    }

    /**
     * Sets the value of the y-coordinate position property.
     * @param y an integer.
     */
    public void setY(int y) {
        this.y = y;
        this.boundsDirty = true;
    }

    /**
     * Adds to the value of the x-coordinate position property.
     * @param x an integer.
     */
    public void addX(int x) {
        this.setX(this.getX() + x);
    }

    /**
     * Adds to the value of the y-coordinate position property.
     * @param y an integer.
     */
    public void addY(int y) {
        this.setY(this.getY() + y);
    }

    /**
     * Sets the value of the image property.
     * @param image an Image object.
     */
    protected void setImage(Image image) {
        this.image = image;
        this.boundsDirty = (this.getWidth() != image.getWidth()
                || this.getHeight() != image.getHeight());
        if (boundsDirty) {
            this.width = image.getWidth();
            this.height = image.getHeight();
        }
    }

    /**
     * Sets the value of the width property.
     * @param val a double.
     */
    public void setWidth(double val) {
        this.width = val;
        this.boundsDirty = true;
    }

    /**
     * Sets the value of the height property.
     * @param val a double.
     */
    public void setHeight(double val) {
        this.height = val;
        this.boundsDirty = true;
    }

    /**
     * Sets the value of the scale property.
     * @param scale an integer.
     */
    public void setScale(int scale) {
        this.scale = scale;
        this.boundsDirty = true;
    }

    /**
     * Sets the value of the visible property.
     * @param value a boolean.
     */
    public void setVisible(boolean value) {
        this.visible = value;
    }

    /**
     * Marks this sprite for removal.
     */
    public void remove() {
        this.removed = true;
    }

    /**
     * Flips the sprite texture.
     * @param value a boolean.
     * @param isVertical whether the texture should be flipped vertically.
     */
    protected void setFlip(boolean value, boolean isVertical) {
        if (isVertical) {
            this.flipVertical = value;
            this.boundsDirty = this.boundsDirty || (this.flipVertical != value);
        } else {
            this.flipHorizontal = value;
            this.boundsDirty = this.boundsDirty || (this.flipHorizontal != value);
        }
    }

    /**
     * Sets the sprite's frame set.
     * @param frameSet an Image object.
     * @param rows number of rows.
     * @param columns number of columns.
     * @param reframe whether to calculate the source rectangles.
     */
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

    /**
     * Sets the sprite's frame set.
     * @param frameSet an Image object.
     * @param rows number of rows.
     * @param columns number of columns.
     */
    protected void setFrameSet(Image frameSet, int rows, int columns) {
        this.setFrameSet(frameSet, rows, columns, true);
    }

    /**
     * Sets the sprite's frame set.
     * @param frameSet an Image object.
     */
    protected void setFrameSet(Image frameSet) {
        this.setFrameSet(frameSet, -1, -1, false);
    }

    /**
     * Sets the currently displayed frame.
     * @param frame an integer.
     */
    protected void setFrame(int frame) {
        this.frame = frame;
    }

    /**
     * Sets the value of the frame interval property.
     * @param interval a long.
     */
    protected void setFrameInterval(long interval) {
        this.frameInterval = interval;
    }

    /**
     * Sets the ranges of the frame sequence.
     * @param min start range.
     * @param max end range.
     */
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

    /**
     * Sets the value of the frame auto-reset property.
     * @param frameAutoReset a boolean.
     */
    protected void setFrameAutoReset(boolean frameAutoReset) {
        this.frameAutoReset = frameAutoReset;
    }

    /**
     * Sets the value of the bounds offset property.
     * @param boundsOffset an integer array.
     */
    protected void setBoundsOffset(int boundsOffset[]) {
        this.boundsOffset = boundsOffset;
        this.boundsDirty = true;
    }

    @Override
    public int compareTo(Sprite o) {
        return (int) (this.getBounds().getMaxY() - o.getBounds().getMaxY());
    }

}
