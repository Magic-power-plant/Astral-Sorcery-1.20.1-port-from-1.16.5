package hellfirepvp.astralsorcery.common.crystal;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/** Per-stack crystal properties. Reading must not mutate the stack; null means absent or clear. */
public interface CrystalAttributeItem {

    @Nullable
    CrystalAttributes getAttributes(ItemStack stack);

    void setAttributes(ItemStack stack, @Nullable CrystalAttributes attributes);
}
