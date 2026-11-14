/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.block.util.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemInfusedCrystalShovel
 * Created by HellFirePvP
 * Date: 04.04.2020 / 11:05
 */
public class ItemInfusedCrystalShovel extends ItemCrystalShovel {

    //1.16.5 had no constructor here at all; in 1.20.1 the item properties come from ItemsAS and are passed through
    public ItemInfusedCrystalShovel(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        //1.16.5: player.getEntityWorld() -> Entity#level() in 1.20.1
        Level world = player.level();
        if (!world.isClientSide() &&
                !player.isShiftKeyDown() &&
                !player.getCooldowns().isOnCooldown(itemstack.getItem()) &&
                player instanceof ServerPlayer) {

            PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);
            if (prog.doPerkAbilities()) {
                EventFlags.CHAIN_MINING.executeWithFlag(() -> {
                    //1.16.5: state.isAir(world, pos) - the world sensitive overload is gone in 1.20.1
                    if (!world.getBlockState(pos).isAir()) {
                        List<BlockPos> foundBlocks = BlockDiscoverer.discoverBlocksWithSameStateAround(world, pos, true, 8, 200, false);
                        if (!foundBlocks.isEmpty()) {
                            ServerPlayer serverPlayer = (ServerPlayer) player;

                            foundBlocks.forEach(at -> {
                                BlockState currentState = world.getBlockState(at);
                                //1.16.5: serverPlayer.interactionManager.tryHarvestBlock(at)
                                if (!currentState.isAir() && serverPlayer.gameMode.destroyBlock(at)) {
                                    PktPlayEffect ev = new PktPlayEffect(PktPlayEffect.Type.BLOCK_EFFECT)
                                            .addData(buf -> {
                                                ByteBufUtils.writePos(buf, at);
                                                ByteBufUtils.writeBlockState(buf, currentState);
                                            });
                                    //1.16.5: PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(world, at, 32));
                                    PacketChannel.sendToAllAround(ev, world, Vec3.atLowerCornerOf(at), 32);
                                }
                            });

                            serverPlayer.getCooldowns().addCooldown(itemstack.getItem(), 120);
                        }
                    }
                });
            }
        }
        return super.onBlockStartBreak(itemstack, pos, player);
    }
}
