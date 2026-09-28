package hellfirepvp.astralsorcery.common.network;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.network.base.ASLoginPacket;
import hellfirepvp.astralsorcery.common.network.login.client.PktLoginAcknowledge;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncDataHolder;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncGateway;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncPerkInformation;
import hellfirepvp.astralsorcery.common.network.packet.PktSyncKnowledge;
import hellfirepvp.astralsorcery.common.network.play.client.*;
import hellfirepvp.astralsorcery.common.network.play.server.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.HandshakeHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketChannel {

    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            AstralSorcery.key("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int index = 0;

    public static void registerPackets() {
        // LOGIN DEDICATED_SERVER -> CLIENT
        //1.16.5 PacketChannel:57-63. These must be login-phase registrations: buildLoginPacketList is what makes
        //Forge call makeLogin() and push the packet during the handshake. The previous port registered them with
        //the plain play-phase register(...) below, so makeLogin() had no caller and a joining client received no
        //sync data, no gateways and no perk tree at all.
        registerLoginMessage(PktLoginSyncDataHolder::new, PktLoginSyncDataHolder::makeLogin);
        registerLoginMessage(PktLoginSyncGateway::new, PktLoginSyncGateway::makeLogin);
        registerLoginMessage(PktLoginSyncPerkInformation::new, PktLoginSyncPerkInformation::makeLogin);

        // LOGIN CLIENT -> DEDICATED_SERVER
        registerLoginMessage(PktLoginAcknowledge::new, PktLoginAcknowledge::new);

        // PLAY DEDICATED_SERVER -> CLIENT
        registerASPacket(PktSyncKnowledge.class, PktSyncKnowledge::new);
        register(PktOreScan.class, PktOreScan::encode, PktOreScan::decode, PktOreScan::handle);
        register(PktPlayEffect.class, PktPlayEffect::encode, PktPlayEffect::decode, PktPlayEffect::handle);
        register(PktProgressionUpdate.class, PktProgressionUpdate::encode, PktProgressionUpdate::decode, PktProgressionUpdate::handle);
        register(PktShootEntity.class, PktShootEntity::encode, PktShootEntity::decode, PktShootEntity::handle);
        register(PktSyncCharge.class, PktSyncCharge::encode, PktSyncCharge::decode, PktSyncCharge::handle);
        register(PktSyncData.class, PktSyncData::encode, PktSyncData::decode, PktSyncData::handle);
        register(PktSyncModifierSource.class, PktSyncModifierSource::encode, PktSyncModifierSource::decode, PktSyncModifierSource::handle);
        register(PktSyncPerkActivity.class, PktSyncPerkActivity::encode, PktSyncPerkActivity::decode, PktSyncPerkActivity::handle);
        register(PktSyncStepAssist.class, PktSyncStepAssist::encode, PktSyncStepAssist::decode, PktSyncStepAssist::handle);
        register(PktUpdateGateways.class, PktUpdateGateways::encode, PktUpdateGateways::decode, PktUpdateGateways::handle);
        register(PktOpenGui.class, PktOpenGui::encode, PktOpenGui::decode, PktOpenGui::handle);

        // PLAY CLIENT -> DEDICATED_SERVER
        register(PktAttunePlayerConstellation.class, PktAttunePlayerConstellation::encode, PktAttunePlayerConstellation::decode, PktAttunePlayerConstellation::handle);
        register(PktClearBlockStorageStack.class, PktClearBlockStorageStack::encode, PktClearBlockStorageStack::decode, PktClearBlockStorageStack::handle);
        register(PktDiscoverConstellation.class, PktDiscoverConstellation::encode, PktDiscoverConstellation::decode, PktDiscoverConstellation::handle);
        register(PktEngraveGlass.class, PktEngraveGlass::encode, PktEngraveGlass::decode, PktEngraveGlass::handle);
        register(PktPerkGemModification.class, PktPerkGemModification::encode, PktPerkGemModification::decode, PktPerkGemModification::handle);
        register(PktRequestPerkSealAction.class, PktRequestPerkSealAction::encode, PktRequestPerkSealAction::decode, PktRequestPerkSealAction::handle);
        register(PktRequestSeed.class, PktRequestSeed::encode, PktRequestSeed::decode, PktRequestSeed::handle);
        register(PktRequestTeleport.class, PktRequestTeleport::encode, PktRequestTeleport::decode, PktRequestTeleport::handle);
        register(PktRevokeGatewayAccess.class, PktRevokeGatewayAccess::encode, PktRevokeGatewayAccess::decode, PktRevokeGatewayAccess::handle);
        register(PktRotateTelescope.class, PktRotateTelescope::encode, PktRotateTelescope::decode, PktRotateTelescope::handle);
        register(PktToggleClientOption.class, PktToggleClientOption::encode, PktToggleClientOption::decode, PktToggleClientOption::handle);
        register(PktUnlockPerk.class, PktUnlockPerk::encode, PktUnlockPerk::decode, PktUnlockPerk::handle);
    }

    /**
     * 1.16.5 PacketChannel#registerLoginMessage (PacketChannel.java:94-113). Forge 1.20.1 kept the whole API, only
     * moving {@code FMLHandshakeHandler} to {@link HandshakeHandler} and renaming {@code consumer(...)} to
     * {@code consumerNetworkThread(...)}.
     * <p>
     * {@code loginIndex} requires the message type to be an {@code IntSupplier} ({@link ASLoginPacket} is one) and
     * {@code buildLoginPacketList} is what registers the generator Forge invokes during the handshake.
     * Client -> server replies are routed through {@link HandshakeHandler#indexFirst} so the handshake's login
     * index bookkeeping runs before the packet's own handler.
     */
    private static <T extends ASLoginPacket<T>> void registerLoginMessage(Supplier<T> pktSupplier, Supplier<T> makeLoginPacket) {
        T packet = pktSupplier.get();
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) packet.getClass();
        CHANNEL.messageBuilder(type, index++)
                .loginIndex(ASLoginPacket::getLoginIndex, ASLoginPacket::setLoginIndex)
                .encoder(packet.encoder())
                .decoder(packet.decoder())
                .consumerNetworkThread((t, contextSupplier) -> {
                    BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
                    if (contextSupplier.get().getDirection().getReceptionSide().isServer()) {
                        handler = HandshakeHandler.indexFirst((handshakeHandler, pkt, ctxSupplier) -> packet.handler().accept(pkt, ctxSupplier));
                    } else {
                        handler = packet.handler();
                    }

                    handler.accept(t, contextSupplier);
                })
                .buildLoginPacketList(local -> Collections.singletonList(Pair.of(type.getName(), makeLoginPacket.get())))
                .add();
    }

    /**
     * 1.16.5 PacketChannel#registerMessage (PacketChannel.java:115-122) hands the packet's own handler to the
     * channel unchanged and leaves the enqueueWork/setPacketHandled decision to the packet. Every packet
     * registered here already calls {@code context.enqueueWork(...)} and {@code context.setPacketHandled(true)}
     * in its own {@code handle}, so wrapping them again enqueued the work a second time (one extra tick of
     * latency per packet) and marked the packet handled twice.
     */
    private static <T> void register(Class<T> type,
                                      BiConsumer<T, FriendlyByteBuf> encoder,
                                      Function<FriendlyByteBuf, T> decoder,
                                      BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        CHANNEL.registerMessage(index++, type, encoder, decoder, handler);
    }

    /**
     * 1.16.5 registers every {@link hellfirepvp.astralsorcery.common.network.base.ASPacket} through its own
     * encoder()/decoder()/handler() instances and leaves the enqueueWork/setPacketHandled decision to the
     * packet itself. The generic register(...) above adds that wrapper for the target's static-style packets,
     * so ASPacket subclasses need their own un-wrapped registration.
     */
    private static <T extends hellfirepvp.astralsorcery.common.network.base.ASPacket<T>> void registerASPacket(
            Class<T> type, Supplier<T> factory) {
        T template = factory.get();
        CHANNEL.registerMessage(index++, type,
                (msg, buf) -> template.encoder().accept(msg, buf),
                buf -> template.decoder().apply(buf),
                (msg, ctx) -> msg.handler().accept(msg, ctx));
    }

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToAll(Object msg) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }

    /**
     * 1.16.5 {@code PacketChannel.CHANNEL.sendToAllAround(msg, pointFromPos(...))} equivalent.
     */
    public static void sendToAllAround(Object msg, net.minecraft.world.level.Level level, net.minecraft.world.phys.Vec3 pos, double range) {
        if (level.isClientSide()) {
            return;
        }
        CHANNEL.send(targetFromPos(level, pos, range), msg);
    }

    public static PacketDistributor.TargetPoint pointFromPos(net.minecraft.world.level.Level level, net.minecraft.world.phys.Vec3 pos, double range) {
        return new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, range, ((net.minecraft.server.level.ServerLevel) level).dimension());
    }

    public static PacketDistributor.PacketTarget targetFromPos(net.minecraft.world.level.Level level, net.minecraft.world.phys.Vec3 pos, double range) {
        return PacketDistributor.NEAR.with(() -> pointFromPos(level, pos, range));
    }
}
