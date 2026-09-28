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
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalAxe
 * Created by HellFirePvP
 * Date: 17.08.2019 / 18:10
 */
public class ItemCrystalAxe extends ItemCrystalTierItem implements TypeEnchantableItem, CreativeTabItem {

    /**
     * 1.16.5: {@code super(ToolType.AXE, new Properties(), Sets.newHashSet(Material.WOOD, Material.PLANTS,
     * Material.TALL_PLANTS, Material.BAMBOO, Material.LEAVES))}.
     * <p>
     * 1.20.1 has neither tool types nor block materials. The tool type became the {@code mineable/axe} tag plus the
     * axe tool actions (dig for harvesting, strip for the log stripping implemented in {@link #useOn(UseOnContext)}),
     * and the effective materials map onto block tags: {@code Material.WOOD} is the {@code mineable/axe} tag itself,
     * {@code Material.LEAVES} is {@code minecraft:leaves}, {@code Material.BAMBOO} is {@code minecraft:bamboo_blocks}
     * and {@code Material.PLANTS}/{@code Material.TALL_PLANTS} are what vanilla now groups as
     * {@code minecraft:sword_efficient} (which notably does *not* contain cobweb, so the axe stays as (in)effective
     * against webs as it was in 1.16.5).
     */
    public ItemCrystalAxe(Properties properties) {
        super(BlockTags.MINEABLE_WITH_AXE,
                Sets.newHashSet(ToolActions.AXE_DIG, ToolActions.AXE_STRIP),
                properties,
                Sets.newHashSet(BlockTags.MINEABLE_WITH_AXE, BlockTags.LEAVES, BlockTags.BAMBOO_BLOCKS, BlockTags.SWORD_EFFICIENT));
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
        return 11;
    }

    @Override
    double getAttackSpeed() {
        return -3;
    }

    @Override
    protected boolean isToolEfficientAgainst(BlockState state) {
        //1.16.5: state.getMaterial() == Material.LEAVES
        return state.is(BlockTags.LEAVES);
    }

    /**
     * 1.16.5: {@code onItemUse(ItemUseContext)} - renamed to {@code useOn(UseOnContext)} in 1.20.1.
     * <p>
     * Only the stripping action is handled, exactly as in 1.16.5 where {@code ToolType.AXE} on
     * {@code getToolModifiedState} meant "strip this log". The copper-era {@code AXE_SCRAPE}/{@code AXE_WAX_OFF}
     * actions that vanilla's {@code AxeItem} additionally performs in 1.20.1 have no 1.16.5 counterpart and are
     * therefore deliberately not claimed here either (see the tool actions passed to the constructor).
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = world.getBlockState(blockpos);
        //1.16.5: blockstate.getToolModifiedState(world, blockpos, player, context.getItem(), ToolType.AXE) - which
        //was the stripping transformation; 1.20.1 asks for that transformation by tool action instead
        BlockState block = blockstate.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
        if (block != null) {
            Player playerentity = context.getPlayer();
            //1.16.5: SoundEvents.ITEM_AXE_STRIP
            world.playSound(playerentity, blockpos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide()) {
                world.setBlock(blockpos, block, 11);
                if (playerentity != null) {
                    context.getItemInHand().hurtAndBreak(1, playerentity, (stack) ->
                            stack.broadcastBreakEvent(context.getHand()));
                }
            }

            //1.16.5: ActionResultType.func_233537_a_(world.isRemote())
            return InteractionResult.sidedSuccess(world.isClientSide());
        } else {
            return InteractionResult.PASS;
        }
    }
}
