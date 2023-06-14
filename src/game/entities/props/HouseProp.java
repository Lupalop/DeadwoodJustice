package game.entities.props;

import javafx.geometry.Rectangle2D;

/**
 * The HouseProp is a prop used in the level map.
 * @author Francis Dominic Fajardo
 */
public final class HouseProp extends Prop {

    private static final String HOUSE = "a_house.png";
    private static final Rectangle2D HOUSE_COLLIDER =
            new Rectangle2D(0, 0, 111, 120);

    public HouseProp(int xPos, int yPos) {
        super(xPos, yPos, HOUSE);
        this.setCustomCollider(HOUSE_COLLIDER);
    }

}
