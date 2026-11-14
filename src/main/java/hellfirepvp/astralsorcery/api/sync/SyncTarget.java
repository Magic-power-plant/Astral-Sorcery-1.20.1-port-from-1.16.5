package hellfirepvp.astralsorcery.api.sync;

/** An owner-controlled persistence and synchronization capability. Implementations define thread confinement. */
@FunctionalInterface
public interface SyncTarget {
    void markForUpdate();
}
