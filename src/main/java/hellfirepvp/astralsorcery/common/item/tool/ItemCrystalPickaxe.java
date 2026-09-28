/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import com.google.common.collect.Sets;
import hellfirepvp.astralsorcery.common.item.base.TypeEnchantableItem;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import net.minecraft.tags.BlockTags;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ToolActions;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalPickaxe
 * Created by HellFirePvP
 * Date: 17.08.2019 / 18:03
 */
public class ItemCrystalPickaxe extends ItemCrystalTierItem implements TypeEnchantableItem, CreativeTabItem {

    /**
     * 1.16.5: {@code super(ToolType.PICKAXE, new Properties(), Sets.newHashSet(Material.ROCK, Material.IRON,
     * Material.ANVIL))}.
     * <p>
     * 1.20.1 has neither tool types nor block materials: the tool type became the {@code mineable/pickaxe} tag plus
     * the pickaxe dig action, and all three effective materials (stone, metal and anvils) are precisely what vanilla
     * now collects in that same {@code mineable/pickaxe} tag, so it doubles as the effective-blocks set.
     */
    public ItemCrystalPickaxe(Properties properties) {
        super(BlockTags.MINEABLE_WITH_PICKAXE,
                Sets.newHashSet(ToolActions.PICKAXE_DIG),
                properties,
                Sets.newHashSet(BlockTags.MINEABLE_WITH_PICKAXE));
    }

    /**
     * 1.16.5 {@code fillItemGroup}: a single creative entry carrying the creative crystal tool attributes.
     */
    @Override
    public void fillCreativeTab(CreativeModeTab.Output output) {
        output.accept(this.createCreativeStack());
    }

    public ItemStack createCreativeStack() {
        ItemStack stack = new ItemStack(this);
        CrystalPropertiesAS.CREATIVE_CRYSTAL_TOOL_ATTRIBUTES.store(stack);
        return stack;
    }

    @Override
    public boolean canEnchantItem(ItemStack stack, EnchantmentCategory type) {
        return type == EnchantmentCategory.BREAKABLE || type == EnchantmentCategory.DIGGER;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        //1.16.5: enchantment.type - the field is called category in 1.20.1
        EnchantmentCategory type = enchantment.category;
        return type == EnchantmentCategory.DIGGER || type == EnchantmentCategory.BREAKABLE;
    }

    @Override
    double getAttackDamage() {
        return 5;
    }

    @Override
    double getAttackSpeed() {
        return -1;
    }
}
