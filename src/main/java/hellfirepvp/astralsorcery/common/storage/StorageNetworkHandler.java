package hellfirepvp.astralsorcery.common.storage;

import hellfirepvp.astralsorcery.common.data.world.StorageNetworkBuffer;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StorageNetworkHandler
 * Created by HellFirePvP
 * Date: 30.05.2019 / 15:02
 *
 * <b>Unfinished upstream.</b> Nothing calls {@link #getHandler(Level)} in this tree, in 1.16.5, or in
 * 1.21.1 - there is no storage-core block or tile to drive it. 1.16.5 leaves the fusion/division logic
 * commented out inside {@link NetworkHelper} (1.16.5 common/storage/StorageNetworkHandler.java:52-58) and
 * 1.21.1 deleted common/storage outright. This class is therefore <em>not</em> a working feature and must
 * not be reported as ported; it only keeps the persistence wiring honest, see below.
 */
public class StorageNetworkHandler {

    private static final Map<ResourceKey<Level>, NetworkHelper> mappingHelpers = new HashMap<>();

    public static NetworkHelper getHandler(Level world) {
        return mappingHelpers.computeIfAbsent(world.dimension(), id -> new NetworkHelper(world));
    }

    public static void clearHandler(Level world) {
        clearHandler(world.dimension());
    }

    public static void clearHandler(ResourceKey<Level> dimKey) {
        mappingHelpers.remove(dimKey);
    }

    public static class NetworkHelper {

        //1.16.5 resolves the networks out of the persisted StorageNetworkBuffer
        //(1.16.5 common/storage/StorageNetworkHandler.java:50). An earlier stage of this port replaced that
        //with a per-runtime HashMap plus putNetwork/removeNetwork setters that nothing called, which left
        //DataAS.KEY_STORAGE_NETWORK registered but never instantiated: the "storagenetwork" save file was
        //neither written nor read, so anything the map held was lost on restart. Reading through the buffer
        //again keeps the registered save key on a real code path.
        private final StorageNetworkBuffer buffer;

        private NetworkHelper(Level world) {
            this.buffer = DataAS.DOMAIN_AS.getData(world, DataAS.KEY_STORAGE_NETWORK);
        }

        @Nullable
        public StorageNetwork getNetwork(BlockPos networkMaster) {
            return this.buffer.getNetwork(networkMaster);
        }
    }

    public static class MappingChange {
    }

}
