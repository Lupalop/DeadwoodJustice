package game.entities;

public abstract class Effect extends Sprite {

    private Sprite spriteTarget;

    public Effect(int xPos, int yPos) {
        super(xPos, yPos);
        initialize();
    }

    public Effect(Sprite spriteTarget) {
        super();
        this.spriteTarget = spriteTarget;
        this.setScale(2);
        initialize();
    }

    public abstract void initialize();

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
        return (int) spriteTarget.getY();
    }

}
