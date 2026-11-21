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
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolAction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalTierItem
 * Created by HellFirePvP
 * Date: 17.08.2019 / 16:10
 * <p>
 * 1.16.5 extended plain {@code Item} and looked the tier up statically; 1.20.1 has {@link TieredItem} which does
 * exactly that (it only adds {@code getTier()} plus tier-based {@code getEnchantmentValue()}/{@code isValidRepairItem}
 * defaults, both of which are overridden below with the very same 1.16.5 values), so the crystal tier is handed to it.
 */
public abstract class ItemCrystalTierItem extends TieredItem implements CrystalAttributeItem {

    //1.16.5: Set<Material> effectiveMaterials - block materials are gone in 1.20.1, the closest equivalent are block tags
    private final Set<TagKey<Block>> effectiveBlocks;
    //1.16.5: ToolType toolType - both the harvestability check and Properties#addToolType(tool, harvestLevel)
    private final TagKey<Block> mineableTag;
    //1.16.5: implied by the single ToolType above; 1.20.1 expresses "acts like a pickaxe/axe/shovel" as tool actions
    private final Set<ToolAction> toolActions;

    /**
     * 1.16.5: {@code super(withTool(toolType, prop.maxDamage(tier.getMaxUses()).setNoRepair().group(ITEM_GROUP_AS)))}
     * where {@code withTool} called {@code Properties#addToolType(toolType, tier.getHarvestLevel())}.
     * <p>
     * 1.20.1 dropped {@code Properties#addToolType} (replaced by {@link #canPerformAction(ItemStack, ToolAction)}) and
     * {@code Properties#group} (creative tabs are data driven now, see the subclasses' {@code createCreativeStack}),
     * so only the durability and the no-repair flag remain here.
     */
    protected ItemCrystalTierItem(@Nullable TagKey<Block> mineableTag, Set<ToolAction> toolActions, Properties prop, Set<TagKey<Block>> effectiveBlocks) {
        super(CrystalToolTier.getInstance(), prop
                .durability(CrystalToolTier.getInstance().getUses())
                .setNoRepair());
        this.effectiveBlocks = effectiveBlocks;
        this.mineableTag = mineableTag;
        this.toolActions = toolActions;
    }

    abstract double getAttackDamage();

    abstract double getAttackSpeed();

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

    /**
     * 1.16.5 {@code getToolTypes(stack)}: the tool type handed to the constructor was registered on the item
     * properties, 1.20.1 answers the same question through the tool actions.
     */
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return this.toolActions.contains(toolAction);
    }

    /**
     * 1.16.5 {@code canHarvestBlock(stack, state)}:
     * <pre>
     * int i = tier.getHarvestLevel();
     * if (toolType != null && state.getHarvestTool() == toolType) return i >= state.getHarvestLevel();
     * return effectiveMaterials.contains(state.getMaterial());
     * </pre>
     * 1.20.1 has neither per-block harvest tools/levels nor materials: "the block is harvested by this tool type"
     * became the {@code mineable/*} tag and "the tier is high enough" became
     * {@code TierSortingRegistry#isCorrectTierForDrops} (which for this unregistered tier compares the tier level
     * against the vanilla {@code needs_*_tool} tags, i.e. harvest level 3 as in 1.16.5).
     */
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (this.mineableTag != null && state.is(this.mineableTag)) {
            return TierSortingRegistry.isCorrectTierForDrops(CrystalToolTier.getInstance(), state);
        }
        return this.isEffectiveBlock(state);
    }

    //Same as above, for the vanilla (stack-less) query path
    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        if (this.mineableTag != null && state.is(this.mineableTag)) {
            return TierSortingRegistry.isCorrectTierForDrops(CrystalToolTier.getInstance(), state);
        }
        return this.isEffectiveBlock(state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float str = super.getDestroySpeed(stack, state);
        //1.16.5: getToolTypes(stack).stream().noneMatch(state::isToolEffective) - 1.20.1 asks the mineable tag instead
        if ((this.mineableTag == null || !state.is(this.mineableTag)) &&
                !isToolEfficientAgainst(state) &&
                !this.isEffectiveBlock(state)) {
            return str;
        }
        str *= CrystalToolTier.getInstance().getSpeed();
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            return CrystalCalculations.getToolEfficiency(str, stack);
        }
        return str;
    }

    //1.16.5: this.effectiveMaterials.contains(state.getMaterial())
    private boolean isEffectiveBlock(BlockState state) {
        for (TagKey<Block> tag : this.effectiveBlocks) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isToolEfficientAgainst(BlockState state) {
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, (entity) -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!worldIn.isClientSide() && state.getDestroySpeed(worldIn, pos) != 0.0F) {
            stack.hurtAndBreak(1, entityLiving, (p_220038_0_) -> p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
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
    public boolean isValidRepairItem(ItemStack p_82789_1_, ItemStack p_82789_2_) {
        return false;
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
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", this.getAttackDamage(), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", this.getAttackSpeed(), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }
}
