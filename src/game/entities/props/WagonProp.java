package game.entities.props;

import javafx.geometry.Rectangle2D;

/**
 * This class represents the covered wagon prop.
 * @author Francis Dominic Fajardo
 */
public final class WagonProp extends Prop {

    /** Frame set: prop. */
    private static final String WAGON = "a_coveredwagon.png";
    /** Custom collider. */
    private static final Rectangle2D WAGON_COLLIDER =
            new Rectangle2D(0, 27, 64, 20);

    /**
     * Constructs an instance of WagonProp.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     */
    public WagonProp(int xPos, int yPos) {
        super(xPos, yPos, WAGON);
        this.setCustomCollider(WAGON_COLLIDER);
    }

}
