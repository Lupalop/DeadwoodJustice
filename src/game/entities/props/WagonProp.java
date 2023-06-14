package game.entities.props;

import javafx.geometry.Rectangle2D;

/**
 * The WagonProp is a prop used in the level map.
 * @author Francis Dominic Fajardo
 */
public final class WagonProp extends Prop {

    private static final String WAGON = "a_coveredwagon.png";
    private static final Rectangle2D WAGON_COLLIDER =
            new Rectangle2D(0, 27, 64, 20);

    public WagonProp(int xPos, int yPos) {
        super(xPos, yPos, WAGON);
        this.setCustomCollider(WAGON_COLLIDER);
    }

}
