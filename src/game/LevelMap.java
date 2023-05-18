package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import game.entities.Prop;
import game.entities.Sprite;
import game.entities.Tileset;
import javafx.scene.canvas.GraphicsContext;

public class LevelMap {

    private static final int TILEGEN_MATCH = 1;
    private static final int TILEGEN_FREQ_GRASS_OR_ROCK = 6;
    private static final int TILEGEN_FREQ_CACTUS = 50;
    private static final int TILEGEN_FREQ_PROPS = 150;

    private static final int TILE_SIZE = 32;
    private static final int TILES_VERTICAL =
            (Game.WINDOW_HEIGHT + 8) / TILE_SIZE;
    private static final int TILES_HORIZONTAL =
            (Game.WINDOW_WIDTH) / TILE_SIZE;
    private static final int TILES_TOTAL =
            TILES_VERTICAL * TILES_HORIZONTAL;

    private static final Tileset TILESET_DESERT =
            new Tileset("tilemap_desert.png", 3, 4);

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
                    "a_tree.png");
            this.sprites.add(tree);
        }

        Prop wagon = new Prop(
                rand.nextInt(50, Game.WINDOW_WIDTH / 2),
                rand.nextInt(3, 6) * 100,   
                "a_coveredwagon.png");
        Prop house = new Prop(
                rand.nextInt(Game.WINDOW_WIDTH / 2, Game.WINDOW_WIDTH),
                -100,
                "a_house.png");

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
        if (this.doneGenerating) {
            int tileId = 0;
            for (int i = 0; i < TILES_VERTICAL; i++) {
                for (int j = 0; j < TILES_HORIZONTAL; j++) {
                    // Draw from the desert tileset.
                    gc.save();
                    gc.setGlobalAlpha(0.5);
                    TILESET_DESERT.draw(
                            gc, TILE_SIZE * j, TILE_SIZE * i, tileLayer1[tileId]);
                    gc.restore();
                    if (tileLayer2[tileId] != 0) {
                        TILESET_DESERT.draw(
                                gc, TILE_SIZE * j, TILE_SIZE * i, tileLayer2[tileId]);
                    }
                    tileId++;
                }
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
