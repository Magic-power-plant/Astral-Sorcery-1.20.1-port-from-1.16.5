/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EnchantmentScorchingHeat
 * Created by HellFirePvP
 * Date: 07.03.2021 / 13:01
 */
public class EnchantmentScorchingHeat extends Enchantment {

    //1.16.5 EnchantmentType -> 1.20.1 EnchantmentCategory; EquipmentSlotType -> EquipmentSlot
    public EnchantmentScorchingHeat() {
        super(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    //1.16.5 Enchantment#canApplyTogether -> 1.20.1 Enchantment#checkCompatibility
    protected boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench) && ench != Enchantments.SILK_TOUCH;
    }

    @Override
    //1.16.5 Enchantment#canApply -> 1.20.1 Enchantment#canEnchant
    public boolean canEnchant(ItemStack stack) {
        return stack.canApplyAtEnchantingTable(this);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }
}
