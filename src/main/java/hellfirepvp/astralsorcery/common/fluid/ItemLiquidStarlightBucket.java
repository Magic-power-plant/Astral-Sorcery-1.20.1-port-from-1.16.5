package hellfirepvp.astralsorcery.common.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Port of 1.16.5 {@code ItemLiquidStarlightBucket}: a bucket that exposes the fluid handler capability.
 */
public class ItemLiquidStarlightBucket extends BucketItem {

    public ItemLiquidStarlightBucket(Supplier<? extends Fluid> fluidSupplier) {
        super(fluidSupplier, new Item.Properties()
                .craftRemainder(Items.BUCKET)
                .stacksTo(1));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidBucketWrapper(stack);
    }
}
