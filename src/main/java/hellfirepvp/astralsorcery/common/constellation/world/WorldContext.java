package hellfirepvp.astralsorcery.common.constellation.world;

import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Random;

public class WorldContext {

    private final long seed;

    private final CelestialEventHandler celestialHandler;
    private final ActiveCelestialsHandler activeCelestialsHandler;

    private final ConstellationHandler constellationHandler;
    private final DistributionHandler distributionHandler;

    public WorldContext(long seed) {
        this.seed = seed;
        this.celestialHandler = new CelestialEventHandler(this);
        this.activeCelestialsHandler = new ActiveCelestialsHandler();

        this.constellationHandler = new ConstellationHandler(this);
        this.distributionHandler = new DistributionHandler(this);
    }

    public long getSeed() {
        return seed;
    }

    @Nonnull
    public Random getRandom() {
        return getRandom(0L);
    }

    @Nonnull
    public Random getRandom(long seedModifier) {
        return new Random(this.seed + seedModifier);
    }

    @Nonnull
    public Random getDayRandom() {
        int track = this.getConstellationHandler().getLastTrackedDay();
        return new Random(this.getSeed() * 31 + track * 31);
    }

    @Nonnull
    public CelestialEventHandler getCelestialEventHandler() {
        return celestialHandler;
    }

    @Nonnull
    public ConstellationHandler getConstellationHandler() {
        return constellationHandler;
    }

    @Nonnull
    public DistributionHandler getDistributionHandler() {
        return distributionHandler;
    }

    @Nonnull
    public ActiveCelestialsHandler getActiveCelestialsHandler() {
        return activeCelestialsHandler;
    }

    public void tick(Level world) {
        this.celestialHandler.tick(world);
        this.constellationHandler.tick(world);
        this.distributionHandler.tick(world);
    }
}
