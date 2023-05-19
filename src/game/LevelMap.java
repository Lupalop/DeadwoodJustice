package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import game.entities.Mob;
import game.entities.Outlaw;
import game.entities.Powerup;
import game.entities.Prop;
import game.entities.Sprite;
import game.entities.Tileset;
import javafx.scene.canvas.GraphicsContext;

public class LevelMap {

    private static final Tileset TILESET_DESERT =
            new Tileset("tilemap_desert.png", 3, 4, 1);

    private static final String PROP_HOUSE = "a_house.png";
    private static final String PROP_COVERED_WAGON = "a_coveredwagon.png";
    private static final String PROP_TREE = "a_tree.png";

    private static final int TILEGEN_MATCH = 1;
    private static final int TILEGEN_FREQ_GRASS_OR_ROCK = 6;
    private static final int TILEGEN_FREQ_CACTUS = 50;
    private static final int TILEGEN_FREQ_PROPS = 150;

    private static final int TILES_VERTICAL =
            (Game.WINDOW_MAX_HEIGHT + 8) / Tileset.TILE_SIZE_MID;
    private static final int TILES_HORIZONTAL =
            (Game.WINDOW_MAX_WIDTH) / Tileset.TILE_SIZE_MID;
    private static final int TILES_TOTAL =
            TILES_VERTICAL * TILES_HORIZONTAL;

    private static final double TILE_LAYER1_ALPHA = 0.5;

    private int[] tileLayer1;
    private int[] tileLayer2;

    private ArrayList<Sprite> sprites;
    private ArrayList<Sprite> pendingSpriteAdds;
    private ArrayList<Sprite> pendingSpriteRemoves;

    private boolean doneGenerating;

    public LevelMap() {
        this.doneGenerating = false;

        this.sprites = new ArrayList<Sprite>();
        this.pendingSpriteAdds = new ArrayList<Sprite>();
        this.pendingSpriteRemoves = new ArrayList<Sprite>();
    }

    public void generate(boolean regenerate) {
        if (this.doneGenerating && !regenerate) {
            return;
        }

        tileLayer1 = new int[TILES_TOTAL];
        tileLayer2 = new int[TILES_TOTAL];

        // Holds the current tile ID.
        int tileId = 0;
        // Iterate through each tile.
        for (int i = 0; i < TILES_VERTICAL; i++) {
            for (int j = 0; j < TILES_HORIZONTAL; j++) {
                // The tile generator.
                Random rand = new Random();
                // Generate: land tile.
                tileLayer1[tileId] = rand.nextInt(0, 4);
                // Generate: grass.
                if (rand.nextInt(TILEGEN_FREQ_GRASS_OR_ROCK) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = rand.nextInt(6, 8);
                // Generate: rocks.
                } else if (rand.nextInt(TILEGEN_FREQ_GRASS_OR_ROCK) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = rand.nextInt(8, 10);
                // Generate: cactus.
                } else if (rand.nextInt(TILEGEN_FREQ_CACTUS) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = rand.nextInt(4, 6);
                // Generate: sign.
                } else if (rand.nextInt(TILEGEN_FREQ_PROPS) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = 10;
                // Generate: fossil.
                } else if (rand.nextInt(TILEGEN_FREQ_PROPS) == TILEGEN_MATCH) {
                    tileLayer2[tileId] = 11;
                }
                tileId++;
            }
        }

        this.doneGenerating = true;
    }

    public void generate() {
        generate(false);
    }

    public void generateProps() {
        Random rand = new Random();
        for (int i = 0; i < 3; i++) {
            Prop tree = new Prop(
                    rand.nextInt(1, 8) * rand.nextInt(1, 3) * 60,
                    rand.nextInt(2, 6) * rand.nextInt(1, 3) * 60,
                    PROP_TREE);
            this.sprites.add(tree);
        }

        Prop wagon = new Prop(
                rand.nextInt(50, Game.WINDOW_MAX_WIDTH / 2),
                rand.nextInt(3, 6) * 100,
                PROP_COVERED_WAGON);
        Prop house = new Prop(
                rand.nextInt(Game.WINDOW_MAX_WIDTH / 2, Game.WINDOW_MAX_WIDTH),
                -100,
                PROP_HOUSE);

        this.sprites.add(wagon);
        this.sprites.add(house);
    }

    public void draw(GraphicsContext gc) {
        this.drawTiles(gc);
        this.drawSprites(gc);
    }

    private void drawSprites(GraphicsContext gc) {
        for (Sprite sprite : this.sprites) {
            sprite.draw(gc);
        }
    }

    private void drawTiles(GraphicsContext gc) {
        if (!this.doneGenerating) {
            return;
        }

        int tileX = 0;
        int tileY = 0;
        int tileId = 0;
        for (int i = 0; i < TILES_VERTICAL; i++) {
            tileY = Tileset.TILE_SIZE_MID * i;
            for (int j = 0; j < TILES_HORIZONTAL; j++) {
                tileX = Tileset.TILE_SIZE_MID * j;
                // Draw from the desert tileset.
                gc.save();
                gc.setGlobalAlpha(TILE_LAYER1_ALPHA);
                TILESET_DESERT.draw(
                        gc, tileX, tileY, tileLayer1[tileId]);
                gc.restore();
                if (tileLayer2[tileId] != 0) {
                    TILESET_DESERT.draw(
                            gc, tileX, tileY, tileLayer2[tileId]);
                }
                tileId++;
            }
        }
    }

    public void update(long now) {
        if (Game.FLAG_FIX_DRAW_ORDER) {
            Collections.sort(this.sprites);
        }

        if (this.pendingSpriteAdds.size() > 0) {
            this.getSprites().addAll(this.pendingSpriteAdds);
            this.pendingSpriteAdds.clear();
        }

        if (this.pendingSpriteRemoves.size() > 0) {
            this.getSprites().removeAll(this.pendingSpriteRemoves);
            this.pendingSpriteRemoves.clear();
        }
    }

    public boolean[] getPassability(Sprite source) {
        boolean passability[] = new boolean[4];
        passability[0] = source.getBounds().getMinX() >= Game.WINDOW_MIN_WIDTH;
        passability[1] = source.getBounds().getMaxX() <= Game.WINDOW_MAX_WIDTH;
        passability[2] = source.getBounds().getMinY() >= Game.WINDOW_MIN_HEIGHT;
        passability[3] = source.getBounds().getMaxY() <= Game.WINDOW_MAX_HEIGHT;

        for (Sprite sprite : this.getSprites()) {
            if (!Game.FLAG_MOBS_CHECK_PASSABILITY
                    || (Game.FLAG_IGNORE_PROP_COLLISION && sprite instanceof Prop)
                    || (sprite instanceof Mob && !((Mob)sprite).isAlive())
                    || sprite instanceof Powerup
                    || sprite instanceof Outlaw
                    || sprite == source) {
                continue;
            }

            int side = source.baseIntersectsSide(sprite.getBaseBounds());
            if (passability[0] && side == 0) {
                passability[0] = false;
            } else if (passability[1] && side == 1) {
                passability[1] = false;
            } else if (passability[2] && side == 2) {
                passability[2] = false;
            } else if (passability[3] && side == 3) {
                passability[3] = false;
            }
        }

        return passability;
    }

    public boolean isDoneGenerating() {
        return doneGenerating;
    }

    public ArrayList<Sprite> getSprites() {
        return this.sprites;
    }

    public void addSpriteOnUpdate(Sprite sprite) {
        this.pendingSpriteAdds.add(sprite);
    }

    public void removeSpriteOnUpdate(Sprite sprite) {
        this.pendingSpriteRemoves.add(sprite);
    }

}
