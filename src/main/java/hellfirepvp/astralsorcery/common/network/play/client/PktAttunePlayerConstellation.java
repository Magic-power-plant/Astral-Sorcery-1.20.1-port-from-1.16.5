package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunePlayerRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active.ActivePlayerAttunementRecipe;
import hellfirepvp.astralsorcery.common.blockentity.AttunementAltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server request to finish the player's active attunement early, sent by the client camera flight
 * when it expires ({@code ActivePlayerAttunementRecipe#createAttunementListener} in 1.16.5).
 * <p>
 * The server validates that the altar still runs a player attunement for the sender and that the sender is still an
 * eligible attunement candidate before finishing the active recipe.
 * <p>
 * 1.16.5 serialized the constellation through the custom Forge registry; the target registry is not populated yet,
 * so the constellation key is resolved through the local {@link ConstellationRegistry} instead.
 */
public class PktAttunePlayerConstellation {

    private IMajorConstellation attunement = null;
    private ResourceKey<Level> world = null;
    private BlockPos at = BlockPos.ZERO;

    public PktAttunePlayerConstellation() {}

    public PktAttunePlayerConstellation(IMajorConstellation attunement, ResourceKey<Level> world, BlockPos at) {
        this.attunement = attunement;
        this.world = world;
        this.at = at;
    }

    public static void encode(PktAttunePlayerConstellation msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeOptional(buf, msg.attunement, (buffer, cst) -> ByteBufUtils.writeResourceLocation(buffer, cst.getRegistryName()));
        ByteBufUtils.writeOptional(buf, msg.world, ByteBufUtils::writeVanillaRegistryEntry);
        ByteBufUtils.writePos(buf, msg.at);
    }

    public static PktAttunePlayerConstellation decode(FriendlyByteBuf buf) {
        PktAttunePlayerConstellation pkt = new PktAttunePlayerConstellation();
        ResourceLocation key = ByteBufUtils.readOptional(buf, ByteBufUtils::readResourceLocation);
        if (key != null) {
            IConstellation cst = ConstellationRegistry.getConstellation(key);
            if (cst instanceof IMajorConstellation major) {
                pkt.attunement = major;
            }
        }
        pkt.world = ByteBufUtils.readOptional(buf, ByteBufUtils::readVanillaRegistryEntry);
        pkt.at = ByteBufUtils.readPos(buf);
        return pkt;
    }

    public static void handle(PktAttunePlayerConstellation msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                IMajorConstellation cst = msg.attunement;
                if (sender == null || cst == null || msg.world == null || msg.at == null) {
                    return;
                }
                MinecraftServer server = sender.getServer();
                if (server == null) {
                    return;
                }
                ServerLevel world = server.getLevel(msg.world);
                if (world == null) {
                    return;
                }
                AttunementAltarBlockEntity ta = MiscUtils.getBlockEntityAt(world, msg.at, AttunementAltarBlockEntity.class, false);
                if (ta != null && ta.getActiveRecipe() instanceof ActivePlayerAttunementRecipe activeRecipe) {
                    if (sender.getUUID().equals(activeRecipe.getPlayerUUID()) &&
                            AttunePlayerRecipe.isEligablePlayer(sender, ta.getActiveConstellation())) {

                        ta.finishActiveRecipe();
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }

}
