package hellfirepvp.astralsorcery.common.storage;

import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface IStorageNetworkTile<T extends IStorageNetworkTile<T>> extends ILocatable {

    T getAssociatedCore();

    Level getNetworkWorld();

    void receiveMappingChange(StorageNetworkHandler.MappingChange newMapping);

    @Nullable
    default StorageNetwork getNetwork() {
        return StorageNetworkHandler.getHandler(getNetworkWorld())
                .getNetwork(getAssociatedCore().getLocationPos());
    }

    default T resolveMasterCore() {
        T assoc = getAssociatedCore();
        T next;
        while (assoc != (next = getAssociatedCore())) {
            assoc = next;
        }
        return assoc;
    }

}
