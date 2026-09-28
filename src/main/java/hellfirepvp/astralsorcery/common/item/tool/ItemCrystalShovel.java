/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import hellfirepvp.astralsorcery.common.item.base.TypeEnchantableItem;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;

import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemCrystalShovel
 * Created by HellFirePvP
 * Date: 17.08.2019 / 18:26
 */
public class ItemCrystalShovel extends ItemCrystalTierItem implements TypeEnchantableItem, CreativeTabItem {

    //Unused in 1.16.5 as well (the paving is done through the vanilla tool-modified state below).
    //1.16.5: Blocks.GRASS_PATH - the block is called DIRT_PATH in 1.20.1
    private static final Map<Block, BlockState> BLOCK_PAVE_MAP = new ImmutableMap.Builder<Block, BlockState>()
            .put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.defaultBlockState())
            .build();

    /**
     * 1.16.5: {@code super(ToolType.SHOVEL, new Properties(), Sets.newHashSet(Material.SNOW, Material.SNOW_BLOCK))}.
     * 1.20.1 has neither tool types nor block materials: the tool type became the {@code mineable/shovel} tag plus the
     * shovel tool actions (dig for harvesting, flatten for the path making implemented in {@link #useOn(UseOnContext)}),
     * and both snow materials are exactly the contents of the vanilla {@code minecraft:snow} block tag.
     */
    public ItemCrystalShovel(Properties properties) {
        super(BlockTags.MINEABLE_WITH_SHOVEL,
                Sets.newHashSet(ToolActions.SHOVEL_DIG, ToolActions.SHOVEL_FLATTEN),
                properties,
                Sets.newHashSet(BlockTags.SNOW));
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
        return 3;
    }

    @Override
    double getAttackSpeed() {
        return -1.5;
    }

    //1.16.5: onItemUse(ItemUseContext) - renamed to useOn(UseOnContext) in 1.20.1
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        } else {
            Player playerentity = context.getPlayer();
            //1.16.5: state.getToolModifiedState(world, pos, player, context.getItem(), ToolType.SHOVEL) - the
            //1.20.1 signature takes the whole use context plus the tool action that used to be implied by the type
            BlockState modifiedState = state.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
            BlockState targetState = null;
            if (modifiedState != null && world.isEmptyBlock(pos.above())) {
                //1.16.5: SoundEvents.ITEM_SHOVEL_FLATTEN
                world.playSound(playerentity, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                targetState = modifiedState;
            } else if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
                if (!world.isClientSide()) {
                    //1.16.5: world.playEvent(null, 1009, pos, 0)
                    world.levelEvent(null, 1009, pos, 0);
                }

                //1.16.5: CampfireBlock.extinguish(world, pos, state) - renamed to dowse, which additionally takes
                //the entity that put the campfire out (null-safe, used for the game event dispatch only)
                CampfireBlock.dowse(playerentity, world, pos, state);
                targetState = state.setValue(CampfireBlock.LIT, false);
            }

            if (targetState != null) {
                if (!world.isClientSide()) {
                    world.setBlock(pos, targetState, 11);
                    if (playerentity != null) {
                        context.getItemInHand().hurtAndBreak(1, playerentity, (player) -> {
                            player.broadcastBreakEvent(context.getHand());
                        });
                    }
                }

                //1.16.5: ActionResultType.func_233537_a_(world.isRemote())
                return InteractionResult.sidedSuccess(world.isClientSide());
            } else {
                return InteractionResult.PASS;
            }
        }
    }
}
