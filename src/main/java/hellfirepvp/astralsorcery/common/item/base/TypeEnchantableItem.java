package hellfirepvp.astralsorcery.common.item.base;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public interface TypeEnchantableItem {

    boolean canEnchantItem(ItemStack stack, EnchantmentCategory type);

}
