package hellfirepvp.astralsorcery.common.constellation;

import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

//1.16.5 registers SkyHandler.getInstance() as an ITickHandler from CommonProxy#attachTickListeners
//(1.16.5 common/CommonProxy.java:245). This class had the three handler methods but did not implement the
//interface and was never registered, so no server-side WorldContext was ever created and the constellation
//day cycle never advanced: every getContext(level, SERVER) returned null and no constellation was ever active.
public class SkyHandler implements hellfirepvp.astralsorcery.common.ITickHandler {

    private static final SkyHandler instance = new SkyHandler();

    private final Map<ResourceKey<Level>, WorldContext> worldHandlersServer = Maps.newHashMap();
    private final Map<ResourceKey<Level>, WorldContext> worldHandlersClient = Maps.newHashMap();
    private final Map<ResourceKey<Level>, Boolean> skyRevertMap = Maps.newHashMap();

    private SkyHandler() {}

    public static SkyHandler getInstance() {
        return instance;
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        if (type == TickEvent.Type.LEVEL) {
            Level w = (Level) context[0];
            if (!w.isClientSide() && w instanceof ServerLevel) {
                ResourceKey<Level> dimKey = w.dimension();
                skyRevertMap.put(dimKey, false);

                WorldContext ctx = worldHandlersServer.get(dimKey);
                if (ctx == null) {
                    ctx = createContext(MiscUtils.getRandomWorldSeed((ServerLevel) w));
                    worldHandlersServer.put(dimKey, ctx);
                }
                ctx.tick(w);
            }
        } else {
            handleClientTick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClientTick() {
        Level w = Minecraft.getInstance().level;
        if (w != null) {
            ResourceKey<Level> dimKey = w.dimension();
            WorldContext ctx = worldHandlersClient.get(dimKey);
            if (ctx == null) {
                Optional<Long> seedOpt = WorldSeedCache.getSeedIfPresent(dimKey);
                if (seedOpt.isEmpty()) {
                    return;
                }
                ctx = createContext(seedOpt.get());
                worldHandlersClient.put(dimKey, ctx);
            }
            ctx.tick(w);
        }
    }

    private WorldContext createContext(long seed) {
        return new WorldContext(seed);
    }

    @Nullable
    public static WorldContext getContext(Level world) {
        return getContext(world, world.isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER);
    }

    @Nullable
    public static WorldContext getContext(Level world, LogicalSide dist) {
        if (world == null) {
            return null;
        }
        ResourceKey<Level> dimKey = world.dimension();
        if (dist.isClient()) {
            return getInstance().worldHandlersClient.getOrDefault(dimKey, null);
        } else {
            return getInstance().worldHandlersServer.getOrDefault(dimKey, null);
        }
    }

    public void revertWorldTimeTick(ServerLevel world) {
        ResourceKey<Level> dimKey = world.dimension();
        Boolean state = skyRevertMap.get(dimKey);
        if (!world.isClientSide && state != null && !state) {
            skyRevertMap.put(dimKey, true);
            world.setDayTime(world.getDayTime() - 1);
        }
    }

    public void clientClearCache() {
        worldHandlersClient.clear();
    }

    public void informWorldUnload(Level world) {
        worldHandlersServer.remove(world.dimension());
        worldHandlersClient.remove(world.dimension());
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.LEVEL, TickEvent.Type.CLIENT);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "ConstellationSkyhandler";
    }
}
