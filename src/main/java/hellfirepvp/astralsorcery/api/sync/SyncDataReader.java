package hellfirepvp.astralsorcery.api.sync;

import net.minecraft.nbt.CompoundTag;

/** Stateless decoder called on the receiving client's game thread. */
public interface SyncDataReader<C> {
    /** Replace prior state. Treat input as read-only and copy retained data. */
    void readFromIncomingFullSync(C data, CompoundTag compound);
    /** Apply an ordered delta, including removals, to the last full snapshot. */
    void readFromIncomingDiff(C data, CompoundTag compound);
}
