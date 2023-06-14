package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import game.entities.Bullet;
import game.entities.Entity;
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

    private ArrayList<Entity> entities;
    private ArrayList<Sprite> overlays;
    private ArrayList<Prop> generatedProps;

    private ListIterator<Entity> entityIterator;
    private ListIterator<Sprite> overlayIterator;

    private boolean tilesGenerated;
    private boolean propsGenerated;

    public LevelMap(boolean excludeProps) {
        this.tilesGenerated = false;
        this.propsGenerated = excludeProps;

        this.entities = new ArrayList<Entity>();
        this.overlays = new ArrayList<Sprite>();
        this.generatedProps = new ArrayList<Prop>();

        this.entityIterator = this.entities.listIterator();
        this.overlayIterator = this.overlays.listIterator();
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
            for (Prop prop : this.generatedProps) {
                prop.remove();
            }
            generatedProps.clear();
        }

        for (int i = 0; i < 3; i++) {
            Prop tree = new TreeProp(
                    Game.RNG.nextInt(1, 8) * Game.RNG.nextInt(1, 3) * 60,
                    Game.RNG.nextInt(2, 6) * Game.RNG.nextInt(1, 3) * 60);
            this.generatedProps.add(tree);
            this.entityIterator.add(tree);
        }

        Prop wagon = new WagonProp(
                Game.RNG.nextInt(50, Game.WINDOW_MAX_WIDTH / 2),
                Game.RNG.nextInt(3, 6) * 100);
        this.generatedProps.add(wagon);
        this.entityIterator.add(wagon);

        Prop house = new HouseProp(
                Game.RNG.nextInt(Game.WINDOW_MAX_WIDTH / 2, Game.WINDOW_MAX_WIDTH),
                -100);
        this.generatedProps.add(house);
        this.entityIterator.add(house);

        this.propsGenerated = true;
    }

    public void generateProps() {
        generateProps(false);
    }

    public void draw(GraphicsContext gc) {
        this.drawTiles(gc);
        this.drawSprites(gc);
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

    private void drawSprites(GraphicsContext gc) {
        this.entityIterator = this.entities.listIterator();
        while (this.entityIterator.hasNext()) {
            Entity entity = this.entityIterator.next();
            entity.draw(gc);
        }

        this.overlayIterator = this.overlays.listIterator();
        while (this.overlayIterator.hasNext()) {
            Sprite sprite = this.overlayIterator.next();
            sprite.draw(gc);
        }
    }

    public void update(long now) {
        // Ensure sprites are sorted by y-order.
        Collections.sort(this.entities);

        this.entityIterator = this.entities.listIterator();
        while (this.entityIterator.hasNext()) {
            Entity entity = this.entityIterator.next();
            if (entity.getRemoved()) {
                this.entityIterator.remove();
            } else {
                entity.update(now);
            }
        }

        this.overlayIterator = this.overlays.listIterator();
        while (this.overlayIterator.hasNext()) {
            Sprite sprite = this.overlayIterator.next();
            if (sprite.getRemoved()) {
                this.overlayIterator.remove();
            } else {
                sprite.update(now);
            }
        }
    }

    public boolean[] getPassability(Entity source) {
        boolean passability[] = new boolean[4];
        passability[Entity.SIDE_LEFT] =
                source.getBounds().getMinX() >= Game.WINDOW_MIN_WIDTH;
        passability[Entity.SIDE_RIGHT] =
                source.getBounds().getMaxX() <= Game.WINDOW_MAX_WIDTH;
        passability[Entity.SIDE_TOP] =
                source.getBounds().getMinY() >= Game.WINDOW_MIN_HEIGHT;
        passability[Entity.SIDE_BOTTOM] =
                source.getBounds().getMaxY() <= Game.WINDOW_MAX_HEIGHT;

        for (Entity entity : this.entities) {
            if ((entity instanceof Mob && !((Mob)entity).isAlive())
                    || entity instanceof Powerup
                    || entity instanceof Outlaw
                    || entity instanceof Bullet
                    || entity == source) {
                continue;
            }

            boolean[] sides = source.intersectsSide(entity, true);
            if (sides != null) {
                if (passability[Entity.SIDE_LEFT] && sides[Entity.SIDE_LEFT]) {
                    passability[Entity.SIDE_LEFT] = false;
                }
                if (passability[Entity.SIDE_RIGHT] && sides[Entity.SIDE_RIGHT]) {
                    passability[Entity.SIDE_RIGHT] = false;
                }
                if (passability[Entity.SIDE_TOP] && sides[Entity.SIDE_TOP]) {
                    passability[Entity.SIDE_TOP] = false;
                }
                if (passability[Entity.SIDE_BOTTOM] && sides[Entity.SIDE_BOTTOM]) {
                    passability[Entity.SIDE_BOTTOM] = false;
                }
            }
        }

        return passability;
    }

    public boolean isDoneGenerating() {
        return tilesGenerated;
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(this.entities);
    }

    public void addOverlay(Sprite sprite) {
        this.overlayIterator.add(sprite);
    }

    public void addEntity(Entity entity) {
        this.entityIterator.add(entity);
    }

}
