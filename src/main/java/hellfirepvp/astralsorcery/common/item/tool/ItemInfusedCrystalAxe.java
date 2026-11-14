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
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.perk.modifier.DynamicAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.source.provider.equipment.EquipmentAttributeModifierProvider;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import hellfirepvp.astralsorcery.common.structure.match.util.BlockArray;
import hellfirepvp.astralsorcery.common.block.util.TreeDiscoverer;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.object.CacheReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemInfusedCrystalAxe
 * Created by HellFirePvP
 * Date: 03.04.2020 / 18:18
 */
public class ItemInfusedCrystalAxe extends ItemCrystalAxe implements EquipmentAttributeModifierProvider {

    private static final UUID MODIFIER_ID = UUID.fromString("85c65b91-f44c-4aba-841d-7785eae32831");
    private static final CacheReference<DynamicAttributeModifier> MINING_SPEED_MODIFIER =
            new CacheReference<>(() -> new DynamicAttributeModifier(MODIFIER_ID, PerkAttributeTypesAS.ATTR_TYPE_INC_HARVEST_SPEED, ModifierType.ADDED_MULTIPLY, 0.1F));

    //1.16.5 had no constructor here at all; in 1.20.1 the item properties come from ItemsAS and are passed through
    public ItemInfusedCrystalAxe(Properties properties) {
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
                    //1.16.5 used ObserverLib's BlockArray; the embedded copy lives in common.structure.match.util
                    BlockArray tree = TreeDiscoverer.findTreeAt(world, pos, true, 9);
                    if (!tree.getContents().isEmpty()) {
                        ServerPlayer serverPlayer = (ServerPlayer) player;

                        tree.getContents().keySet().forEach(at -> {
                            BlockState currentState = world.getBlockState(at);
                            //1.16.5: serverPlayer.interactionManager.tryHarvestBlock(at)
                            if (serverPlayer.gameMode.destroyBlock(at)) {
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
                });
            }
        }
        return super.onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public Collection<PerkAttributeModifier> getModifiers(ItemStack stack, Player player, LogicalSide side, boolean ignoreRequirements) {
        return Collections.singletonList(MINING_SPEED_MODIFIER.get());
    }
}
