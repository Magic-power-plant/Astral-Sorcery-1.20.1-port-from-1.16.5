package hellfirepvp.astralsorcery.common.network.play;

import hellfirepvp.astralsorcery.common.network.play.client.PktToggleClientOption;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link PktToggleClientOption} had a complete server handler (it toggles the player's perk abilities and sends
 * the chat feedback) but no sender at all: its only caller in 1.16.5 is the "disable perk abilities" key binding,
 * and both {@code client/input/KeyBindingWrapper} and {@code client/input/KeyDisablePerkAbilities} were
 * three-line TODO_STUBs, so {@code RegistryKeyBindings} registered no binding.
 * <p>
 * The key press itself needs a client, but the wire format can be pinned here.
 */
class PlayPacketCodecTest {

    @Test
    void toggleClientOptionRoundTripsItsOption() {
        for (PktToggleClientOption.Option option : PktToggleClientOption.Option.values()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            PktToggleClientOption.encode(new PktToggleClientOption(option), buf);
            PktToggleClientOption received = PktToggleClientOption.decode(buf);

            assertEquals(0, buf.readableBytes(), "decoder must consume exactly what the encoder wrote");
            assertEquals(option, optionOf(received));
        }
    }

    /**
     * The option travels as an ordinal ({@code ByteBufUtils#writeEnumValue}), so reordering or inserting a
     * constant silently changes what an older client asks the server to toggle.
     */
    @Test
    void theOptionOrdinalIsPartOfTheWireFormat() {
        assertEquals(0, PktToggleClientOption.Option.DISABLE_PERK_ABILITIES.ordinal());

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktToggleClientOption.encode(
                new PktToggleClientOption(PktToggleClientOption.Option.DISABLE_PERK_ABILITIES), buf);

        assertEquals(4, buf.readableBytes(), "a single int ordinal");
        assertEquals(0, buf.readInt());
    }

    private static PktToggleClientOption.Option optionOf(PktToggleClientOption pkt) {
        try {
            Field f = PktToggleClientOption.class.getDeclaredField("option");
            f.setAccessible(true);
            return (PktToggleClientOption.Option) f.get(pkt);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("expected field 'option' on PktToggleClientOption", e);
        }
    }
}
