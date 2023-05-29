package game.entities.props;

import javafx.geometry.Rectangle2D;

public final class TreeProp extends Prop {

    private static final String TREE = "a_tree.png";
    private static final Rectangle2D TREE_COLLIDER =
            new Rectangle2D(16, 66, 32, 30);

    public TreeProp(int xPos, int yPos) {
        super(xPos, yPos, TREE);
        this.setCustomCollider(TREE_COLLIDER);
    }

}
