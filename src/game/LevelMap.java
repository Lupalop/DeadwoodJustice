package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import game.entities.Bullet;
import game.entities.Mote;
import game.entities.Outlaw;
import game.entities.Sprite;
import game.entities.Tile;
import game.entities.mobs.Mob;
import game.entities.powerups.Powerup;
import game.entities.props.HouseProp;
import game.entities.props.Prop;
import game.entities.props.TreeProp;
import game.entities.props.WagonProp;
import javafx.scene.canvas.GraphicsContext;

public class LevelMap {

    private static final Tile TILE_DESERT =
            new Tile("tilemap_desert.png", 3, 4, 1);

    private static final int TILEGEN_MATCH = 1;
    private static final int TILEGEN_FREQ_GRASS_OR_ROCK = 6;
    private static final int TILEGEN_FREQ_CACTUS = 50;
    private static final int TILEGEN_FREQ_PROPS = 150;

    private static final double TILE_LAYER1_ALPHA = 0.5;

    private int[] tileLayer1;
    private int[] tileLayer2;

    private ArrayList<Sprite> sprites;
    private ArrayList<Sprite> spritesLayer1;
    private ArrayList<Sprite> spritesLayer2;
    private ArrayList<Sprite> pendingSpriteAdds;
    private ArrayList<Sprite> pendingSpriteRemoves;
    private ArrayList<Sprite> generatedProps;

    private boolean tilesGenerated;
    private boolean propsGenerated;

    public LevelMap(boolean excludeProps) {
        this.tilesGenerated = false;
        this.propsGenerated = excludeProps;

        this.sprites = new ArrayList<Sprite>();
        this.spritesLayer1 = new ArrayList<Sprite>();
        this.spritesLayer2 = new ArrayList<Sprite>();
        this.pendingSpriteAdds = new ArrayList<Sprite>();
        this.pendingSpriteRemoves = new ArrayList<Sprite>();
        this.generatedProps = new ArrayList<Sprite>();
    }

    public LevelMap() {
        this(false);
    }

    public void generate(boolean regenerate) {
        if (this.tilesGenerated && !regenerate) {
            return;
        }

        tileLayer1 = new int[Tile.ALL];
        tileLayer2 = new int[Tile.ALL];

        // Holds the current tile ID.
        int tileId = 0;
        // Iterate through each tile.
        for (int i = 0; i < Tile.ALL_VERTICAL; i++) {
            for (int j = 0; j < Tile.ALL_HORIZONTAL; j++) {
                // Generate: land tile.
                tileLayer1[tileId] = Game.RNG.nextInt(0, 4);
                // Generate: grass.
                if (Game.RNG.nextInt(TILEGEN_FREQ_GRASS_OR_ROCK) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = Game.RNG.nextInt(6, 8);
                // Generate: rocks.
                } else if (Game.RNG.nextInt(TILEGEN_FREQ_GRASS_OR_ROCK) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = Game.RNG.nextInt(8, 10);
                // Generate: cactus.
                } else if (Game.RNG.nextInt(TILEGEN_FREQ_CACTUS) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = Game.RNG.nextInt(4, 6);
                // Generate: sign.
                } else if (Game.RNG.nextInt(TILEGEN_FREQ_PROPS) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = 10;
                // Generate: fossil.
                } else if (Game.RNG.nextInt(TILEGEN_FREQ_PROPS) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = 11;
                }
                tileId++;
            }
        }

        this.tilesGenerated = true;
    }

    public void generate() {
        generate(false);
    }

    public void generateProps(boolean regenerate) {
        if (this.propsGenerated && !regenerate) {
            return;
        }

        if (regenerate) {
            this.pendingSpriteRemoves.addAll(generatedProps);
            generatedProps.clear();
        }

        for (int i = 0; i < 3; i++) {
            Prop tree = new TreeProp(
                    Game.RNG.nextInt(1, 8) * Game.RNG.nextInt(1, 3) * 60,
                    Game.RNG.nextInt(2, 6) * Game.RNG.nextInt(1, 3) * 60);
            this.generatedProps.add(tree);
        }

        Prop wagon = new WagonProp(
                Game.RNG.nextInt(50, Game.WINDOW_MAX_WIDTH / 2),
                Game.RNG.nextInt(3, 6) * 100);
        this.generatedProps.add(wagon);

        Prop house = new HouseProp(
                Game.RNG.nextInt(Game.WINDOW_MAX_WIDTH / 2, Game.WINDOW_MAX_WIDTH),
                -100);
        this.generatedProps.add(house);

        this.pendingSpriteAdds.addAll(generatedProps);
        this.spritesLayer1.addAll(generatedProps);
        this.propsGenerated = true;
    }

    public void generateProps() {
        generateProps(false);
    }

    public void draw(GraphicsContext gc) {
        this.drawTiles(gc);
        this.drawSprites(gc);
    }

    private void drawSprites(GraphicsContext gc) {
        for (Sprite sprite : this.spritesLayer1) {
            sprite.draw(gc);
        }

        for (Sprite sprite : this.spritesLayer2) {
            sprite.draw(gc);
        }
    }

    private void drawTiles(GraphicsContext gc) {
        if (!this.tilesGenerated) {
            return;
        }

        int tileX = 0;
        int tileY = 0;
        int tileId = 0;
        for (int i = 0; i < Tile.ALL_VERTICAL; i++) {
            tileY = Tile.SIZE_MID * i;
            for (int j = 0; j < Tile.ALL_HORIZONTAL; j++) {
                tileX = Tile.SIZE_MID * j;
                // Draw from the desert tileset.
                gc.save();
                gc.setGlobalAlpha(TILE_LAYER1_ALPHA);
                TILE_DESERT.draw(
                        gc, tileX, tileY, tileLayer1[tileId]);
                gc.restore();
                if (tileLayer2[tileId] != 0) {
                    TILE_DESERT.draw(
                            gc, tileX, tileY, tileLayer2[tileId]);
                }
                tileId++;
            }
        }
    }

    public void update(long now) {
        // Ensure sprites are sorted by y-order.
        Collections.sort(this.spritesLayer1);

        if (this.pendingSpriteAdds.size() > 0) {
            this.sprites.addAll(this.pendingSpriteAdds);
            this.pendingSpriteAdds.clear();
        }

        if (this.pendingSpriteRemoves.size() > 0) {
            this.sprites.removeAll(this.pendingSpriteRemoves);
            this.spritesLayer1.removeAll(this.pendingSpriteRemoves);
            this.spritesLayer2.removeAll(this.pendingSpriteRemoves);
            this.pendingSpriteRemoves.clear();
        }
    }

    public boolean[] getPassability(Sprite source) {
        boolean passability[] = new boolean[4];
        passability[Sprite.SIDE_LEFT] =
                source.getBounds().getMinX() >= Game.WINDOW_MIN_WIDTH;
        passability[Sprite.SIDE_RIGHT] =
                source.getBounds().getMaxX() <= Game.WINDOW_MAX_WIDTH;
        passability[Sprite.SIDE_TOP] =
                source.getBounds().getMinY() >= Game.WINDOW_MIN_HEIGHT;
        passability[Sprite.SIDE_BOTTOM] =
                source.getBounds().getMaxY() <= Game.WINDOW_MAX_HEIGHT;

        for (Sprite sprite : this.sprites) {
            if (!Game.FLAG_CHECK_ALL_COLLIDERS
                    || (!Game.FLAG_CHECK_PROP_COLLIDERS && sprite instanceof Prop)
                    || (sprite instanceof Mob && !((Mob)sprite).isAlive())
                    || sprite instanceof Powerup
                    || sprite instanceof Outlaw
                    || sprite instanceof Bullet
                    || sprite == source) {
                continue;
            }

            boolean[] sides = source.intersectsSide(sprite, true);
            if (sides != null) {
                if (passability[Sprite.SIDE_LEFT] && sides[Sprite.SIDE_LEFT]) {
                    passability[Sprite.SIDE_LEFT] = false;
                }
                if (passability[Sprite.SIDE_RIGHT] && sides[Sprite.SIDE_RIGHT]) {
                    passability[Sprite.SIDE_RIGHT] = false;
                }
                if (passability[Sprite.SIDE_TOP] && sides[Sprite.SIDE_TOP]) {
                    passability[Sprite.SIDE_TOP] = false;
                }
                if (passability[Sprite.SIDE_BOTTOM] && sides[Sprite.SIDE_BOTTOM]) {
                    passability[Sprite.SIDE_BOTTOM] = false;
                }
            }
        }

        return passability;
    }

    public boolean isDoneGenerating() {
        return tilesGenerated;
    }

    public List<Sprite> getSprites() {
        return Collections.unmodifiableList(this.sprites);
    }

    public void addSpriteOnUpdate(Sprite sprite) {
        if (sprite instanceof Powerup) {
            this.spritesLayer2.add(0, sprite);
        } else if (sprite instanceof Mote) {
            this.spritesLayer2.add(sprite);
        } else {
            this.spritesLayer1.add(sprite);
        }
        this.pendingSpriteAdds.add(sprite);
    }

    public void removeSpriteOnUpdate(Sprite sprite) {
        this.pendingSpriteRemoves.add(sprite);
    }

}
