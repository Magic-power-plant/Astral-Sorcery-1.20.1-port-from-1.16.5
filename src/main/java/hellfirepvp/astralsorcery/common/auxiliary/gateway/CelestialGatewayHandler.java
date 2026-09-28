package hellfirepvp.astralsorcery.common.auxiliary.gateway;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktUpdateGateways;
import hellfirepvp.astralsorcery.common.util.SidedReference;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.level.LevelEvent;

import javax.annotation.Nullable;
import java.util.*;

public class CelestialGatewayHandler {

    public static final CelestialGatewayHandler INSTANCE = new CelestialGatewayHandler();
    private CelestialGatewayFilter filter = null;
    private boolean startUp = false;

    private final SidedReference<Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>>> cache = new SidedReference<>();

    private CelestialGatewayHandler() {}

    private CelestialGatewayFilter getFilter() {
        if (filter == null) {
            filter = new CelestialGatewayFilter();
        }
        return filter;
    }

    public void addPosition(Level world, GatewayCache.GatewayNode node) {
        if (world.isClientSide()) {
            return;
        }

        ResourceKey<Level> dimKey = world.dimension();
        if (!cache.getData(Dist.DEDICATED_SERVER).map(map -> map.get(dimKey)).isPresent()) {
            forceLoad(world.dimension());
        }

        Optional<Collection<GatewayCache.GatewayNode>> worldData = cache.getData(Dist.DEDICATED_SERVER).map(map -> map.get(dimKey));
        if (!worldData.isPresent()) {
            AstralSorcery.LOG.info("Couldn't add gateway at " + node.getPos() + " - loading the world failed.");
            return;
        }
        Collection<GatewayCache.GatewayNode> nodes = worldData.get();

        getFilter().addDim(dimKey);
        if (!nodes.contains(node)) {
            nodes.add(node);
            syncToAll();
        }
    }

    public void removePosition(Level world, BlockPos pos) {
        if (world.isClientSide()) {
            return;
        }

        ResourceKey<Level> dimKey = world.dimension();
        Optional<Collection<GatewayCache.GatewayNode>> worldData = cache.getData(Dist.DEDICATED_SERVER).map(map -> map.get(dimKey));
        if (!worldData.isPresent()) {
            return;
        }
        Collection<GatewayCache.GatewayNode> nodes = worldData.get();
        if (nodes.removeIf(node -> node.getPos().equals(pos))) {
            if (nodes.isEmpty()) {
                getFilter().removeDim(dimKey);
            }
            syncToAll();
        }
    }

    private void forceLoad(ResourceKey<Level> world) {
        // TODO_STUB: forceLoad - reimplement when server access is available
    }

    public void onServerStart() {
        startUp = true;
        startUp = false;
    }

    public void onServerStop() {
        this.cache.setData(Dist.DEDICATED_SERVER, null);
    }

    public void onWorldInit(LevelEvent.Load event) {
        if (this.startUp) {
            return;
        }

        LevelAccessor world = event.getLevel();
        if (world.isClientSide() || !(world instanceof Level)) {
            return;
        }

        this.loadIntoCache((Level) world);
        this.syncToAll();
    }

    public void syncToAll() {
        PktUpdateGateways pkt = new PktUpdateGateways(this.getGatewayCache(Dist.DEDICATED_SERVER));
        PacketChannel.sendToAll(pkt);
    }

    public Collection<GatewayCache.GatewayNode> getGatewaysForWorld(Level world, Dist side) {
        return this.cache.getData(side)
                .map(data -> data.getOrDefault(world.dimension(), Collections.emptyList()))
                .orElse(Collections.emptyList());
    }

    public Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> getGatewayCache(Dist side) {
        return this.cache.getData(side).orElse(Collections.emptyMap());
    }

    @Nullable
    public GatewayCache.GatewayNode getGatewayNode(Level world, Dist side, BlockPos pos) {
        return this.cache.getData(side)
                .map(data -> data.get(world.dimension()))
                .orElse(Collections.emptyList())
                .stream()
                .filter(node -> node.getPos().equals(pos))
                .findFirst()
                .orElse(null);
    }

    @OnlyIn(Dist.CLIENT)
    public void updateClientCache(@Nullable Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> positions) {
        this.cache.setData(Dist.CLIENT, positions);
    }

    private void loadIntoCache(Level world) {
        GatewayCache cache = DataAS.DOMAIN_AS.getData(world, DataAS.KEY_GATEWAY_CACHE);
        Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> gatewayCache = this.cache.getData(Dist.DEDICATED_SERVER).orElse(new HashMap<>());
        gatewayCache.put(world.dimension(), new HashSet<>(cache.getGatewayPositions()));
        this.cache.setData(Dist.DEDICATED_SERVER, gatewayCache);
    }

}
