package hellfirepvp.astralsorcery.api.sync;

/** An owner-controlled persistence and synchronization capability. Implementations define thread confinement. */
@FunctionalInterface
public interface SyncTarget {
    /** Request owner-controlled persistence/sync; this need not immediately save or send a packet. */
    void markForUpdate();
}
