package hellfirepvp.astralsorcery.common.network.play;

import hellfirepvp.astralsorcery.common.network.play.client.PktRequestPerkSealAction;
import hellfirepvp.astralsorcery.common.network.play.client.PktUnlockPerk;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncCharge;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the wire format of the three packets whose {@code handleClient} bodies were empty or absent.
 * <p>
 * All three were being SENT by the server and silently dropped by the client:
 * {@link PktSyncCharge} every player tick (so {@code AlignmentChargeHandler#receiveCharge}, the only writer of the
 * client-side charge maps, had zero callers and the HUD believed it was permanently at full charge), and
 * {@link PktUnlockPerk} / {@link PktRequestPerkSealAction} as a success echo (so the three
 * {@code ScreenJournalPerkTree} animation methods had zero callers).
 * <p>
 * The client branches now switch on {@code serverAccept} and {@code doSealing}, which makes those two booleans
 * load-bearing rather than write-only - that is what the round-trip assertions below protect. The handlers
 * themselves need a running client and are verified there.
 */
class ServerFeedbackPacketCodecTest {

    @Test
    void syncChargeRoundTripsBothFloats() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncCharge.encode(new PktSyncCharge(1000.0F, 37.5F), buf);

        assertEquals(8, buf.readableBytes(), "two floats, max then current");
        PktSyncCharge received = PktSyncCharge.decode(buf);

        assertEquals(0, buf.readableBytes(), "decoder must consume exactly what the encoder wrote");
        assertEquals(1000.0F, received.getMaxCharge());
        assertEquals(37.5F, received.getCharge());
    }

    /**
     * The order matters: {@code receiveCharge} feeds {@code getMaxCharge} into the maximum map and
     * {@code getCharge} into the current map, so swapping them would report a full bar at any charge.
     */
    @Test
    void syncChargeWritesMaximumBeforeCurrent() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncCharge.encode(new PktSyncCharge(800.0F, 12.0F), buf);

        assertEquals(800.0F, buf.readFloat(), "maximum charge is written first");
        assertEquals(12.0F, buf.readFloat());
    }

    @Test
    void unlockPerkRoundTripsTheAcceptFlagAndKey() {
        ResourceLocation key = new ResourceLocation("astralsorcery", "key_dig_types");

        for (boolean accepted : new boolean[]{true, false}) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            PktUnlockPerk.encode(new PktUnlockPerk(accepted, key), buf);
            PktUnlockPerk received = PktUnlockPerk.decode(buf);

            assertEquals(0, buf.readableBytes(), "decoder must consume exactly what the encoder wrote");
            assertEquals(key, field(PktUnlockPerk.class, received, "perkKey"));
            assertEquals(accepted, field(PktUnlockPerk.class, received, "serverAccept"),
                    "the client branch only plays the unlock animation when serverAccept survives the wire");
        }
    }

    /**
     * The perk key travels through {@code ByteBufUtils#writeOptional}, so a null key must survive as null rather
     * than throwing - the server sends that shape when it rejects the request.
     */
    @Test
    void unlockPerkToleratesAnAbsentKey() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktUnlockPerk.encode(new PktUnlockPerk(false, null), buf);
        PktUnlockPerk received = PktUnlockPerk.decode(buf);

        assertEquals(0, buf.readableBytes());
        assertNull(field(PktUnlockPerk.class, received, "perkKey"));
    }

    @Test
    void perkSealActionRoundTripsTheSealingFlag() {
        ResourceLocation key = new ResourceLocation("astralsorcery", "key_bleed");

        for (boolean sealing : new boolean[]{true, false}) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            PktRequestPerkSealAction.encode(new PktRequestPerkSealAction(key, sealing), buf);
            PktRequestPerkSealAction received = PktRequestPerkSealAction.decode(buf);

            assertEquals(0, buf.readableBytes(), "decoder must consume exactly what the encoder wrote");
            assertEquals(key, received.getPerkKey());
            assertEquals(sealing, received.isDoSealing(),
                    "doSealing chooses between playSealApplyAnimation and playSealBreakAnimation on the client");
        }
    }

    private static Object field(Class<?> owner, Object instance, String name) {
        try {
            Field f = owner.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(instance);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("expected field '" + name + "' on " + owner.getSimpleName(), e);
        }
    }
}
