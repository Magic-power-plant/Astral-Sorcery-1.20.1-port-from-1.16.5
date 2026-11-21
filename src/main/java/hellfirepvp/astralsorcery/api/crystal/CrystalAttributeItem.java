package hellfirepvp.astralsorcery.api.crystal;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Per-stack crystal properties. Reading must not mutate the stack; null means absent or clear.
 * The returned value type remains in {@code common.crystal} for API 1.0 compatibility.
 */
public interface CrystalAttributeItem {

    @Nullable
    CrystalAttributes getAttributes(ItemStack stack);

    void setAttributes(ItemStack stack, @Nullable CrystalAttributes attributes);
}
