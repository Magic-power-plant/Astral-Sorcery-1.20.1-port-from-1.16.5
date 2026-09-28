package hellfirepvp.astralsorcery.common.util.world;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestSeed;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WorldSeedCache {

    private static long lastServerQuery = 0L;
    private static int activeSession = 0;

    private static final Map<ResourceKey<Level>, Long> cacheSeedLookup = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void clearClient() {
        activeSession++;
        cacheSeedLookup.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public static void updateSeedCache(ResourceKey<Level> dim, int session, long seed) {
        if (activeSession == session) {
            cacheSeedLookup.put(dim, seed);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<Long> getSeedIfPresent(ResourceKey<Level> dim) {
        if (dim == null) {
            return Optional.empty();
        }
        if (!cacheSeedLookup.containsKey(dim)) {
            long current = System.currentTimeMillis();
            if (current - lastServerQuery > 5_000) {
                lastServerQuery = current;
                activeSession++;
                PktRequestSeed req = new PktRequestSeed(activeSession, dim);
                PacketChannel.sendToServer(req);
            }
            return Optional.empty();
        }
        return Optional.of(cacheSeedLookup.get(dim));
    }
}
