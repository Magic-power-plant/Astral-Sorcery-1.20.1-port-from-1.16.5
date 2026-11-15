/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.util.dispenser.FluidContainerDispenseBehavior;
import net.minecraft.world.level.block.DispenserBlock;

/** Common item behavior bindings; item declarations live in ItemsAS. */
public final class RegistryItems {
    private RegistryItems() {}

    public static void registerDispenseBehaviors() {
        // 1.16.5: DispenserBlock.registerDispenseBehavior(BUCKET_LIQUID_STARLIGHT, ...) - the bucket is called
        // FluidsAS.LIQUID_STARLIGHT_BUCKET in the 1.20.1 target, and the method is DispenserBlock#registerBehavior.
        DispenserBlock.registerBehavior(FluidsAS.LIQUID_STARLIGHT_BUCKET.get(), FluidContainerDispenseBehavior.getInstance());
    }

}
