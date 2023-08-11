package game.entities.props;

import javafx.geometry.Rectangle2D;

/**
 * This class represents the western house prop.
 * @author Francis Dominic Fajardo
 */
public final class HouseProp extends Prop {

    /** Frame set: prop. */
    private static final String HOUSE = "a_house.png";
    /** Custom collider. */
    private static final Rectangle2D HOUSE_COLLIDER =
            new Rectangle2D(0, 0, 111, 120);

    /**
     * Constructs an instance of HouseProp.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     */
    public HouseProp(int xPos, int yPos) {
        super(xPos, yPos, HOUSE);
        this.setCustomCollider(HOUSE_COLLIDER);
    }

}
