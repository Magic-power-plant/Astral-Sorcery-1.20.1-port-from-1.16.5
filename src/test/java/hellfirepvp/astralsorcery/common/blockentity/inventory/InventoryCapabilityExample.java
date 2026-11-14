package hellfirepvp.astralsorcery.common.blockentity.inventory;

import hellfirepvp.astralsorcery.api.sync.SyncTarget;

/** Compile-checked example: a non-BlockEntity owner composes either inventory.
 * Construction needs bootstrapped Minecraft registries, so it cannot run in plain JUnit. */
final class InventoryCapabilityExample implements SyncTarget {
    private int updates;

    BlockEntityInventory inventory() {
        return new BlockEntityInventory(this, () -> 4);
    }

    FilteredBlockEntityInventory filteredInventory() {
        return new FilteredBlockEntityInventory(this, () -> 4);
    }

    @Override
    public void markForUpdate() {
        updates++;
    }
}
