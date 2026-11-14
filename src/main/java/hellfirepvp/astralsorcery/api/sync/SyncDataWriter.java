package hellfirepvp.astralsorcery.api.sync;

import net.minecraft.nbt.CompoundTag;

/** Serialization independent of lifecycle and dirty-state tracking. */
public interface SyncDataWriter {
    /** Write a full snapshot without consuming pending changes. Do not retain the supplied tag. */
    void writeAllDataToPacket(CompoundTag compound);
    /** Write changes since the previous diff and consume them. Do not retain the supplied tag. */
    void writeDiffDataToPacket(CompoundTag compound);
}
