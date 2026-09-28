package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.client.screen.ScreenTelescope;
import hellfirepvp.astralsorcery.common.tile.TileTelescope;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktRotateTelescope {

    private boolean isClockwise = false;
    private ResourceKey<Level> dim = null;
    private BlockPos pos = BlockPos.ZERO;

    public PktRotateTelescope() {}

    public PktRotateTelescope(boolean isClockwise, ResourceKey<Level> dim, BlockPos pos) {
        this.isClockwise = isClockwise;
        this.dim = dim;
        this.pos = pos;
    }

    public static void encode(PktRotateTelescope msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isClockwise);
        ByteBufUtils.writeOptional(buf, msg.dim, ByteBufUtils::writeVanillaRegistryEntry);
        ByteBufUtils.writePos(buf, msg.pos);
    }

    public static PktRotateTelescope decode(FriendlyByteBuf buf) {
        PktRotateTelescope pkt = new PktRotateTelescope();
        pkt.isClockwise = buf.readBoolean();
        pkt.dim = ByteBufUtils.readOptional(buf, ByteBufUtils::readVanillaRegistryEntry);
        pkt.pos = ByteBufUtils.readPos(buf);
        return pkt;
    }

    public static void handle(PktRotateTelescope msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        } else if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || msg.dim == null || msg.pos == null) {
                    return;
                }
                MinecraftServer server = player.getServer();
                if (server == null) {
                    return;
                }
                ServerLevel world = server.getLevel(msg.dim);
                if (world == null) {
                    return;
                }
                TileTelescope telescope = MiscUtils.getTileAt(world, msg.pos,
                        TileTelescope.class, false);
                if (telescope != null) {
                    telescope.setRotation(msg.isClockwise
                            ? telescope.getRotation().nextClockWise()
                            : telescope.getRotation().nextCounterClockWise());
                    // TileTelescope.setRotation() already sends a block-entity update on the server;
                    // echoing this packet would apply the same rotation twice on the client.
                }
            });
        }
        context.setPacketHandled(true);
    }

    //Kept out of handle(): reading Minecraft#level there made the dedicated server verifier load ClientLevel
    //as soon as PacketChannel registered this handler.
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktRotateTelescope msg) {
        Level world = Minecraft.getInstance().level;
        TileTelescope telescope = MiscUtils.getTileAt(world, msg.pos,
                TileTelescope.class, false);
        if (telescope != null) {
            telescope.setRotation(msg.isClockwise
                    ? telescope.getRotation().nextClockWise()
                    : telescope.getRotation().nextCounterClockWise());
        }
        //1.16.5 PktRotateTelescope.java:91-93, outside the tile block on purpose: the open screen is told about
        //the rotation even if the tile lookup missed. ScreenTelescope#handleRotationChange had zero callers, so
        //the in-GUI rotation arrows did nothing once the screen could finally be opened (AS-GUI-03).
        if (Minecraft.getInstance().screen instanceof ScreenTelescope telescopeScreen) {
            telescopeScreen.handleRotationChange(msg.isClockwise);
        }
    }

}
