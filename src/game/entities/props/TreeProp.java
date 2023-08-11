package game.entities.props;

import javafx.geometry.Rectangle2D;

/**
 * This class represents the tree prop.
 * @author Francis Dominic Fajardo
 */
public final class TreeProp extends Prop {

    /** Frame set: prop. */
    private static final String TREE = "a_tree.png";
    /** Custom collider. */
    private static final Rectangle2D TREE_COLLIDER =
            new Rectangle2D(16, 66, 32, 30);

    /**
     * Constructs an instance of TreeProp.
     * @param x the x-coordinate position.
     * @param y the y-coordinate position.
     */
    public TreeProp(int xPos, int yPos) {
        super(xPos, yPos, TREE);
        this.setCustomCollider(TREE_COLLIDER);
    }

}
