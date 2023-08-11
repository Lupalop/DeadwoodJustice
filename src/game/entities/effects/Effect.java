package game.entities.effects;

import game.entities.Sprite;

/**
 * This class represents an animated sprite that either follows
 * another Sprite object or is at a fixed position.
 * @author Francis Dominic Fajardo
 */
public abstract class Effect extends Sprite {

    /** The sprite followed by this effect. */
    private Sprite spriteTarget;

    /**
     * Constructs an instance of Effect.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     */
    public Effect(int x, int y) {
        super(x, y);
        initialize();
    }

    /**
     * Constructs an instance of Effect that follows
     * the specified Sprite.
     * @param spriteTarget the sprite to be followed.
     */
    public Effect(Sprite spriteTarget) {
        super();
        this.spriteTarget = spriteTarget;
        initialize();
    }

    /**
     * Initializes this Effect.
     */
    protected abstract void initialize();

    @Override
    public int getX() {
        if (this.spriteTarget == null) {
            return super.getX();
        }
        return (int) (this.spriteTarget.getBounds().getMinX()
                + (this.spriteTarget.getBounds().getWidth() / 2)
                - this.getWidth());
    }

    @Override
    public int getY() {
        if (this.spriteTarget == null) {
            return super.getY();
        }
        return spriteTarget.getY();
    }

}
