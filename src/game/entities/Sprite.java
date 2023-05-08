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
    private int scale;
    private boolean visible;

    private Rectangle2D sourceRectangles[];
    private int currentFrame;
    private int totalFrames;
    private long lastFrameTime;
    private long frameInterval;

    private int minFrame;
    private int maxFrame;

    private boolean hasFrameOverride;
    private Image overrideImage;
    private int overrideMinFrame;
    private int overrideMaxFrame;
    private long overrideFrameInterval;

    private final static long DEFAULT_FRAME_INTERVAL =
            TimeUnit.MILLISECONDS.toNanos(100);

    protected int dx, dy;
    protected boolean boundsDirty;

    public Sprite(int xPos, int yPos) {
        this.x = xPos;
        this.y = yPos;
        this.scale = 1;
        this.visible = true;
        this.boundsDirty = true;

        this.currentFrame = -1;
        this.totalFrames = 0;
        this.lastFrameTime = -1;
        this.frameInterval = DEFAULT_FRAME_INTERVAL;
        
        this.minFrame = -1;
        this.maxFrame = -1;

        this.hasFrameOverride = false;
        this.overrideImage = null;
        this.overrideMinFrame = -1;
        this.overrideMaxFrame = -1;
        this.overrideFrameInterval = -1;
    }

    public void draw(GraphicsContext gc) {
        if (currentFrame != -1 && totalFrames != 0) {
            Rectangle2D source = this.sourceRectangles[currentFrame];
            gc.drawImage(
                    this.getImage(),
                    source.getMinX(), source.getMinY(),
                    source.getWidth(), source.getHeight(),
                    this.x, this.y,
                    this.width * this.scale, this.height * this.scale);
        } else {
            gc.drawImage(
                    this.getImage(),
                    this.x, this.y,
                    this.width * this.scale,
                    this.height * this.scale);
        }
        
        if (game.Game.DEBUG_MODE) {
            gc.setStroke(javafx.scene.paint.Color.RED);
            gc.strokeRect(
                    this.getBounds().getMinX(), this.getBounds().getMinY(),
                    this.getBounds().getWidth(), this.getBounds().getHeight());
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
        
        this.currentFrame++;
        if (this.hasFrameOverride) {
            if (this.currentFrame == this.overrideMaxFrame) {
                this.overrideMinFrame = -1;
                this.overrideMaxFrame = -1;
                this.overrideFrameInterval = -1;
                this.overrideImage = null;
                this.hasFrameOverride = false;
                this.currentFrame = minFrame;
            }
        }
        else if (this.currentFrame == maxFrame) {
            this.currentFrame = minFrame;
        }
        this.lastFrameTime = currentNanoTime;
    }

    public boolean intersects(Sprite rect2) {
        Rectangle2D rectangle1 = this.getBounds();
        Rectangle2D rectangle2 = rect2.getBounds();

        return rectangle1.intersects(rectangle2);
    }

    public Rectangle2D getBounds() {
        if (this.boundsDirty) {
            this.bounds = new Rectangle2D(
                    this.x, this.y,
                    this.width * this.scale, this.height * this.scale); 
        }
        return bounds;
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
            if (this.hasFrameOverride) {
                return;
            }
            this.currentFrame = min;
        }
    }
    
    protected void playFrames(int min, int max, Image frameSetOverride, long frameIntervalOverride) {
        // An existing frame range is already being played temporarily.
        if (this.hasFrameOverride) {
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
            this.currentFrame = min;
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
    
    protected void setFrameInterval(long interval) {
        this.frameInterval = interval;
    }

}
