package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.constellation.DrawnConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.blockentity.refraction.RefractionTableBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class PktEngraveGlass {

    /**
     * The refraction table accepts at most three drawn constellations per engraving.
     * Keep the wire decoder bounded so a client cannot force an unbounded allocation.
     */
    private static final int MAX_CONSTELLATIONS = 3;

    private ResourceKey<Level> dim;
    private BlockPos pos;
    private List<DrawnConstellation> constellations = new LinkedList<>();

    public PktEngraveGlass() {}

    public PktEngraveGlass(ResourceKey<Level> dim, BlockPos pos) {
        this.dim = dim;
        this.pos = pos;
    }

    public PktEngraveGlass(ResourceKey<Level> dim, BlockPos pos,
                           List<DrawnConstellation> constellations) {
        this.dim = dim;
        this.pos = pos;
        this.constellations = constellations != null ? constellations : new LinkedList<>();
    }

    public static void encode(PktEngraveGlass msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeVanillaRegistryEntry(buf, msg.dim);
        ByteBufUtils.writePos(buf, msg.pos);
        ByteBufUtils.writeCollection(buf, msg.constellations, (buffer, drawn) -> {
            buffer.writeInt(drawn.getPoint().x);
            buffer.writeInt(drawn.getPoint().y);
            ByteBufUtils.writeResourceLocation(buffer, drawn.getConstellation().getRegistryName());
        });
    }

    public static PktEngraveGlass decode(FriendlyByteBuf buf) {
        PktEngraveGlass pkt = new PktEngraveGlass();
        pkt.dim = ByteBufUtils.readVanillaRegistryEntry(buf);
        pkt.pos = ByteBufUtils.readPos(buf);
        int size = buf.readInt();
        if (size == -1) {
            pkt.constellations = new LinkedList<>();
            return pkt;
        }
        if (size < 0 || size > MAX_CONSTELLATIONS) {
            throw new IllegalArgumentException("Invalid refraction-table constellation count: " + size);
        }

        List<DrawnConstellation> decoded = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Point point = new Point(buf.readInt(), buf.readInt());
            IConstellation constellation = ConstellationRegistry.getConstellation(
                    ByteBufUtils.readResourceLocation(buf));
            if (constellation != null) {
                decoded.add(new DrawnConstellation(point, constellation));
            }
        }
        pkt.constellations = decoded != null
                ? decoded.stream().filter(Objects::nonNull).collect(java.util.stream.Collectors.toList())
                : new LinkedList<>();
        return pkt;
    }

    public static void handle(PktEngraveGlass msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || msg.dim == null || msg.pos == null
                        || msg.constellations == null || msg.constellations.isEmpty()) {
                    return;
                }
                MinecraftServer server = player.getServer();
                if (server == null) {
                    return;
                }
                ServerLevel world = server.getLevel(msg.dim);
                RefractionTableBlockEntity table = MiscUtils.getBlockEntityAt(world, msg.pos,
                        RefractionTableBlockEntity.class, false);
                if (table != null) {
                    table.engraveGlass(new ArrayList<>(msg.constellations.subList(
                            0, Math.min(3, msg.constellations.size()))));
                }
            });
        }
        context.setPacketHandled(true);
    }

}
