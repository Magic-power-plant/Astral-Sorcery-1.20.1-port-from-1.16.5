package hellfirepvp.astralsorcery.common.data.sync.base;

import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class AbstractData {

    private final ResourceLocation key;

    protected AbstractData(ResourceLocation key) {
        this.key = key;
    }

    public final void markDirty() {
        SyncDataHolder.markForUpdate(this.key);
    }

    public abstract void clear(ResourceKey<Level> dim);

    public abstract void clearServer();

    public abstract void writeAllDataToPacket(CompoundTag compound);

    public abstract void writeDiffDataToPacket(CompoundTag compound);

}
