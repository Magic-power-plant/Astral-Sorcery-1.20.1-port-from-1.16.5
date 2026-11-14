package hellfirepvp.astralsorcery.common.util.tile;

import hellfirepvp.astralsorcery.api.sync.SyncTarget;

/** Compile-checked example: a non-BlockEntity owner composes either inventory.
 * Construction needs bootstrapped Minecraft registries, so it cannot run in plain JUnit. */
final class InventoryCapabilityExample implements SyncTarget {
    private int updates;

    TileInventory inventory() {
        return new TileInventory(this, () -> 4);
    }

    TileInventoryFiltered filteredInventory() {
        return new TileInventoryFiltered(this, () -> 4);
    }

    @Override
    public void markForUpdate() {
        updates++;
    }
}
