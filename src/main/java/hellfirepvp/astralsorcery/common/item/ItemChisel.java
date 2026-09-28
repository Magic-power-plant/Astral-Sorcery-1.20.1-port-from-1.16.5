/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemChisel
 * Created by HellFirePvP
 * Date: 15.05.2020 / 15:01
 */
public class ItemChisel extends Item {

    // 1.16.5 built its own Properties here with maxDamage(72).group(CommonProxy.ITEM_GROUP_AS).
    // 1.20.1 has no Properties#group, so the properties (incl. the durability) are supplied by ItemsAS
    // and the creative tab is applied centrally.
    public ItemChisel(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 3;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        // 1.16.5 Enchantments.FORTUNE is named BLOCK_FORTUNE in official mappings.
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment == Enchantments.BLOCK_FORTUNE;
    }
}
