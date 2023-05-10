package game.entities;

import java.util.concurrent.TimeUnit;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class Sprite {

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
    private int currentFrame;
    private int totalFrames;
    private long lastFrameTime;
    private long frameInterval;

    private int minFrame;
    private int maxFrame;

    private boolean isFrameAutoReset;
    private boolean isFrameSequenceDone;
    private boolean hasFrameOverride;
    private Image overrideImage;
    private int overrideMinFrame;
    private int overrideMaxFrame;
    private long overrideFrameInterval;

    private int[] boundsOffset;
    
    private final static long DEFAULT_FRAME_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(100);
    private final static int BASE_DIVIDER = 4;

    protected int dx, dy;
    protected boolean boundsDirty;

    public Sprite(int xPos, int yPos) {
        this.x = xPos;
        this.y = yPos;
        this.scale = 1;
        this.visible = true;
        this.flipHorizontal = false;
        this.flipVertical = false;
        this.boundsDirty = true;

        this.currentFrame = -1;
        this.totalFrames = 0;
        this.lastFrameTime = -1;
        this.frameInterval = DEFAULT_FRAME_INTERVAL;
        
        this.minFrame = -1;
        this.maxFrame = -1;

        this.isFrameSequenceDone = false;
        this.isFrameAutoReset = true;
        this.hasFrameOverride = false;
        this.overrideImage = null;
        this.overrideMinFrame = -1;
        this.overrideMaxFrame = -1;
        this.overrideFrameInterval = -1;
        
        this.boundsOffset = null;
    }

    public void draw(GraphicsContext gc) {
        double flipOffsetX = (flipHorizontal ? (this.width * this.scale) : 0);
        double flipOffsetY = (flipVertical ? (this.height * this.scale) : 0);
        double flipMultiplierWidth = (flipHorizontal ? -1 : 1);
        double flipMultiplierHeight = (flipVertical ? -1 : 1);
        
        if (currentFrame != -1 && totalFrames != 0) {
            Rectangle2D source = this.sourceRectangles[currentFrame];
            gc.drawImage(
                    this.getImage(),
                    source.getMinX(),
                    source.getMinY(),
                    source.getWidth(),
                    source.getHeight(),
                    this.x + flipOffsetX,
                    this.y + flipOffsetY,
                    this.width * this.scale * flipMultiplierWidth,
                    this.height * this.scale * flipMultiplierHeight);
        } else {
            gc.drawImage(
                    this.getImage(),
                    this.x + flipOffsetX,
                    this.y + flipOffsetY,
                    this.width * this.scale * flipMultiplierWidth,
                    this.height * this.scale * flipMultiplierHeight);
        }
        
        if (game.Game.DEBUG_MODE) {
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

    public void update(long currentNanoTime) {
        if (this.currentFrame == -1 || this.totalFrames == 0
                || this.maxFrame == -1 || this.minFrame == -1) {
            return;
        }

        long deltaTime = (currentNanoTime - lastFrameTime);
        if (lastFrameTime == -1) {
            this.lastFrameTime = currentNanoTime;
            return;
        }

        if (deltaTime < getFrameInterval()) {
            return;
        }
        
        if (this.isFrameAutoReset || !this.isFrameSequenceDone) { 
            this.currentFrame++;
        }

        if (this.hasFrameOverride) {
            if (this.currentFrame == this.overrideMaxFrame) {
                this.clearFrameOverride();
            }
        }
        else if (this.currentFrame == this.maxFrame) {
            if (this.isFrameAutoReset) {
                this.currentFrame = this.minFrame;
            } else {
                this.isFrameSequenceDone = true;
            }
        }
        this.lastFrameTime = currentNanoTime;
    }

    public boolean intersects(Sprite rect2) {
        Rectangle2D rectangle1 = this.getBounds();
        Rectangle2D rectangle2 = rect2.getBounds();

        return rectangle1.intersects(rectangle2);
    }

    public static int getIntersectionSide(Rectangle2D r1, Rectangle2D r2) {
        // Check if the rectangles intersect.
        if (!r1.intersects(r2)) {
            return -1;
        }

        // Check which sides of the rectangles intersect.
        if (r1.getMinX() < r2.getMaxX() && r1.getMinX() > r2.getMinX()) {
            return 0; // Left side of r1 intersects right side of r2.
        } else if (r2.getMinX() < r1.getMaxX() && r2.getMinX() > r1.getMinX()) {
            return 1; // Right side of r1 intersects left side of r2.
        } else if (r1.getMinY() < r2.getMaxY() && r1.getMinY() > r2.getMinY()) {
            return 2; // Top side of r1 intersects bottom side of r2.
        } else if (r2.getMinY() < r1.getMaxY() && r2.getMinY() > r1.getMinY()) {
            return 3; // Bottom side of r1 intersects top side of r2.
        }

        // Should never reach this point.
        return -1;        
    }
    
    public int intersectsSide(Rectangle2D r2) {
        return getIntersectionSide(this.getBounds(), r2);
    }
    
    public int baseIntersectsSide(Rectangle2D r2) {
        return getIntersectionSide(this.getBaseBounds(), r2);
    }
    
    public Rectangle2D getBounds() {
        if (this.boundsDirty) {
            double newX = this.x;
            double newY = this.y;
            double newWidth = this.width;
            double newHeight = this.height;
            
            if (this.getBoundsOffset() != null) {
                if (this.flipHorizontal) {
                    newX += this.getBoundsOffset()[1] * scale;
                } else {
                    newX += this.getBoundsOffset()[0] * scale;
                }
                newWidth -= this.getBoundsOffset()[0] + this.getBoundsOffset()[1];
                
                if (this.flipVertical) {
                    newY += this.getBoundsOffset()[3] * scale;
                } else {
                    newY += this.getBoundsOffset()[2] * scale;
                }
                newHeight -= this.getBoundsOffset()[2] + this.getBoundsOffset()[3];
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
        return bounds;
    }

    public Rectangle2D getBaseBounds() {
        if (this.boundsDirty) {
            this.getBounds();
        }
        return this.baseBounds;
    }
    
    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    protected int getScale() {
        return this.scale;
    }
    
    public boolean getVisible() {
        return visible;
    }

    protected Image getImage() {
        if (this.overrideImage != null) {
            return this.overrideImage;
        }
        return this.image;
    }

    public double getWidth() {
        return this.width;
    }
    
    public double getHeight() {
        return this.height;
    }
    
    protected double getFrameInterval() {
        if (this.overrideFrameInterval > 0) {
            return this.overrideFrameInterval;
        }
        return frameInterval;
    }
    
    public boolean isFrameAutoReset() {
        return isFrameAutoReset;
    }

    protected boolean isFrameSequenceDone() {
        return this.isFrameSequenceDone;
    }
    
    protected int[] getBoundsOffset() {
        return this.boundsOffset;
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
        this.setX(this.x + x);
    }
    
    public void addY(int y) {
        this.setY(this.y + y);
    }

    protected void setScale(int scale) {
        this.scale = scale;
        this.boundsDirty = true;
    }
    
    public void setVisible(boolean value) {
        this.visible = value;
    }

    protected void flipHorizontal(boolean value) {
        if (this.flipHorizontal != value) {
            this.flipHorizontal = value;
            this.boundsDirty = true;
        }
    }
    
    protected void flipVertical(boolean value) {
        if (this.flipVertical != value) {
            this.flipVertical = value;
            this.boundsDirty = true;
        }
    }
    
    protected void setImage(Image image) {
        this.image = image;
        this.boundsDirty = (this.width != image.getWidth()
                || this.height != image.getHeight());
        if (boundsDirty) {
            this.width = image.getWidth();
            this.height = image.getHeight();
        }
    }
    
    protected void setMinMaxFrame(int min, int max) {
        boolean shouldReset =
                this.minFrame != min || this.maxFrame != max;
        this.minFrame = min;
        this.maxFrame = max;
        if (shouldReset) {
            this.clearFrameOverride();
            this.isFrameSequenceDone = false;
            this.currentFrame = min;
        }
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
            this.isFrameSequenceDone = false;
            this.currentFrame = min;
        }
    }
    
    private void clearFrameOverride() {
        this.overrideMinFrame = -1;
        this.overrideMaxFrame = -1;
        this.overrideFrameInterval = -1;
        this.overrideImage = null;
        this.hasFrameOverride = false;
        this.isFrameSequenceDone = true;
        this.currentFrame = this.minFrame;
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
                        this.width * column,
                        this.height * row,
                        this.width,
                        this.height);
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
    
    public void setWidth(double val) {
        this.width = val;
        this.boundsDirty = true;
    }

    public void setHeight(double val) {
        this.height = val;
        this.boundsDirty = true;
    }
    
    protected void setFrame(int frameId) {
        this.currentFrame = frameId;
    }
    
    protected void setFrameInterval(long interval) {
        this.frameInterval = interval;
    }

    public void setFrameAutoReset(boolean frameAutoReset) {
        this.isFrameAutoReset = frameAutoReset;
    }

    protected void setBoundsOffset(int boundsOffset[]) {
        this.boundsOffset = boundsOffset;
        this.boundsDirty = true;
    }

}
