/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import hellfirepvp.astralsorcery.common.crystal.CalculationContext;
import hellfirepvp.astralsorcery.api.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.item.base.TypeEnchantableItem;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalSword
 * Created by HellFirePvP
 * Date: 17.08.2019 / 18:34
 */
public class ItemCrystalSword extends SwordItem implements CrystalAttributeItem, TypeEnchantableItem, CreativeTabItem {

    /**
     * 1.16.5 built the whole {@code Properties} itself:
     * {@code new Properties().setNoRepair().maxDamage(tier.getMaxUses()).group(CommonProxy.ITEM_GROUP_AS)}.
     * In 1.20.1 the properties are handed in by {@code ItemsAS} (creative tabs are data driven), so only the
     * no-repair flag and the crystal tier durability are re-applied here.
     */
    public ItemCrystalSword(Properties properties) {
        super(CrystalToolTier.getInstance(),
                0,
                0F,
                properties
                        .setNoRepair()
                        .durability(CrystalToolTier.getInstance().getUses()));
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            attr.addTooltip(tooltip, CalculationContext.Builder.newBuilder()
                    .addUsage(CrystalPropertiesAS.Usages.USE_TOOL_DURABILITY)
                    .addUsage(CrystalPropertiesAS.Usages.USE_TOOL_EFFECTIVENESS)
                    .build());
        }
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            return CrystalCalculations.getToolDurability(super.getMaxDamage(stack), stack);
        }
        return super.getMaxDamage(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        //1.16.5: enchantment.type - the field is called category in 1.20.1
        EnchantmentCategory type = enchantment.category;
        return type == EnchantmentCategory.WEAPON || type == EnchantmentCategory.BREAKABLE;
    }

    //1.16.5: canHarvestBlock(stack, state) - renamed to the ItemStack sensitive isCorrectToolForDrops in 1.20.1
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.getBlock() instanceof WebBlock;
    }

    /**
     * 1.16.5 overrode {@code SwordItem#getAttackDamage()}; the very same getter is called {@code getDamage()} in
     * 1.20.1 (the tier contribution moved into {@code Tier#getAttackDamageBonus()}).
     */
    @Override
    public float getDamage() {
        return CrystalToolTier.getInstance().getAttackDamageBonus();
    }

    public float getAttackDamage(ItemStack stack) {
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            return CrystalCalculations.getToolEfficiency(this.getDamage(), stack);
        } else {
            return this.getDamage();
        }
    }

    private double getAttackSpeed() {
        return -2.4;
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes(ItemStack stack) {
        return CrystalAttributes.getCrystalAttributes(stack);
    }

    @Override
    public void setAttributes(ItemStack stack, @Nullable CrystalAttributes attributes) {
        if (attributes != null) {
            attributes.store(stack);
        } else {
            CrystalAttributes.storeNull(stack);
        }
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    //1.16.5: getIsRepairable(toRepair, repair) - renamed to isValidRepairItem in 1.20.1
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack stack, EnchantmentCategory type) {
        return type == EnchantmentCategory.BREAKABLE || type == EnchantmentCategory.WEAPON;
    }

    //1.16.5: getItemEnchantability(stack) - renamed to the ItemStack sensitive getEnchantmentValue in 1.20.1
    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return CrystalToolTier.getInstance().getEnchantmentValue();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            //1.16.5: Item.ATTACK_DAMAGE_MODIFIER / Item.ATTACK_SPEED_MODIFIER - renamed to BASE_ATTACK_*_UUID
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", this.getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", this.getAttackSpeed(), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }
}
