/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyDigTypes
 * Created by HellFirePvP
 * Date: 31.08.2019 / 17:30
 */
public class KeyDigTypes extends KeyPerk {

    public KeyDigTypes(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide side, IEventBus bus) {
        super.attachListeners(side, bus);

        bus.addListener(this::onHarvest);
        bus.addListener(this::onHarvestSpeed);
    }

    //Restored from 1.16.5 (common/perk/node/key/KeyDigTypes.java:48-64), where it was dropped in the port.
    //onHarvestSpeed alone only makes the block break FAST; this is the half that makes it actually drop, so
    //without it the perk let you chew through logs and dirt with a pickaxe and get nothing for it.
    //1.16.5 tested ToolType.PICKAXE on the held stack and the block's getHarvestTool(); both were deleted in
    //1.19, so the 1.20.1 equivalents are ToolActions.PICKAXE_DIG and the MINEABLE_WITH_* block tags. A block with
    //no MINEABLE_WITH_* tag needs no tool at all and already arrives with canHarvest true, which is 1.16.5's
    //"requiredTool == null" branch, so the early return covers it.
    //1.21.1 KeyPerkAllToolTypes additionally covers MINEABLE_WITH_HOE; deliberately not carried over, because the
    //shipped lang for this perk promises exactly "A pickaxe can be used instead of an axe or shovel".
    private void onHarvest(PlayerEvent.HarvestCheck event) {
        if (event.canHarvest()) {
            return;
        }

        Player player = (Player) event.getEntity();
        LogicalSide side = this.getSide(player);
        PlayerProgress prog = ResearchHelper.getProgress(player, side);
        if (prog.getPerkData().hasPerkEffect(this)) {
            ItemStack heldMainHand = player.getMainHandItem();
            if (!heldMainHand.isEmpty() && heldMainHand.canPerformAction(ToolActions.PICKAXE_DIG)) {
                BlockState target = event.getTargetBlock();
                if (!target.is(BlockTags.MINEABLE_WITH_PICKAXE)
                        && (target.is(BlockTags.MINEABLE_WITH_AXE) || target.is(BlockTags.MINEABLE_WITH_SHOVEL))) {
                    event.setCanHarvest(true);
                }
            }
        }
    }

    private void onHarvestSpeed(PlayerEvent.BreakSpeed event) {
        Player player = (Player) event.getEntity();
        LogicalSide side = this.getSide(player);
        PlayerProgress prog = ResearchHelper.getProgress(player, side);
        if (prog.getPerkData().hasPerkEffect(this)) {
            BlockState broken = event.getState();
            ItemStack playerMainHand = player.getMainHandItem();
            if (!playerMainHand.isEmpty()) {
                //Was `if (true)`, which handed the speed bonus to any held item - a sword dug dirt at pickaxe
                //speed. 1.16.5 required ToolType.PICKAXE on the held stack; ToolActions.PICKAXE_DIG is the
                //1.20.1 spelling of that test and is what 1.21.1 checks too (ItemAbilities.PICKAXE_DIG).
                if (playerMainHand.canPerformAction(ToolActions.PICKAXE_DIG)) {
                    if (!(broken.is(BlockTags.MINEABLE_WITH_PICKAXE)) && (broken.is(BlockTags.MINEABLE_WITH_AXE) || broken.is(BlockTags.MINEABLE_WITH_SHOVEL))) {
                        EventFlags.CHECK_BREAK_SPEED.executeWithFlag(() -> {
                            //1.16.5 tried the real block position first and then null, so that a failure coming
                            //from a position-dependent modifier still had a fallback. The port passed
                            //player.blockPosition() to BOTH attempts, making the second identical to the first
                            //and the chain degenerate. PlayerEvent.BreakSpeed#getPosition is the 1.20.1 source of
                            //that position, and Player#getDigSpeed accepts a null BlockPos.
                            MiscUtils.tryMultiple(
                                    () -> player.getDigSpeed(Blocks.STONE.defaultBlockState(), event.getPosition().orElse(null)),
                                    () -> player.getDigSpeed(Blocks.STONE.defaultBlockState(), null),
                                    () -> BlockUtils.getSimpleBreakSpeed(player, playerMainHand, Blocks.STONE.defaultBlockState())
                            ).ifPresent(speed -> event.setNewSpeed(Math.max(event.getNewSpeed(), speed)));
                        });
                    }
                }
            }
        }
    }
}

