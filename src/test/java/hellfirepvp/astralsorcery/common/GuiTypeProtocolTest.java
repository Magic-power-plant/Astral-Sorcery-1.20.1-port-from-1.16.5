package hellfirepvp.astralsorcery.common;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.network.play.server.PktOpenGui;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuiTypeProtocolTest {
    @Test
    void serializedIdsAndNoArgumentPayloadsRemainCompatible() {
        assertEquals(List.of("CONSTELLATION_PAPER", "TOME", "REFRACTION_TABLE", "TELESCOPE", "HAND_TELESCOPE"),
                Arrays.stream(GuiType.values()).map(Enum::name).toList());
        assertTrue(GuiType.TOME.serializeArguments(new Object[0]).isEmpty());
        assertTrue(GuiType.HAND_TELESCOPE.serializeArguments(new Object[0]).isEmpty());
    }

    @Test
    void blockPositionAndConstellationArgumentsKeepTheirOriginalNbtShape() {
        BlockPos position = new BlockPos(-400, 72, 1024);
        for (GuiType type : List.of(GuiType.REFRACTION_TABLE, GuiType.TELESCOPE)) {
            var tag = type.serializeArguments(new Object[]{position});
            assertEquals(position, NBTHelper.readBlockPosFromNBT(tag));
        }
        ResourceLocation id = ResourceLocation.parse("astralsorcery:discidia");
        IConstellation constellation = new Constellation.Weak("discidia");
        assertEquals(id.toString(), GuiType.CONSTELLATION_PAPER.serializeArguments(new Object[]{constellation}).getString("cst"));
        assertThrows(IllegalArgumentException.class, () -> GuiType.TELESCOPE.serializeArguments(new Object[0]));
        assertThrows(IllegalArgumentException.class, () -> GuiType.CONSTELLATION_PAPER.serializeArguments(new Object[]{position}));
    }

    @Test
    void openGuiPacketRetainsOrdinalAndPayloadThroughRoundTrip() {
        for (GuiType type : GuiType.values()) {
            var tag = GuiType.TELESCOPE.serializeArguments(new Object[]{new BlockPos(-7, 65, 99)});
            FriendlyByteBuf encoded = new FriendlyByteBuf(Unpooled.buffer());
            FriendlyByteBuf reencoded = new FriendlyByteBuf(Unpooled.buffer());
            try {
                PktOpenGui.encode(new PktOpenGui(type, tag), encoded);
                assertEquals(type.ordinal(), encoded.getInt(0));
                PktOpenGui decoded = PktOpenGui.decode(encoded);
                assertEquals(0, encoded.readableBytes());
                PktOpenGui.encode(decoded, reencoded);
                assertEquals(encoded.writerIndex(), reencoded.writerIndex());
                for (int i = 0; i < encoded.writerIndex(); i++) assertEquals(encoded.getByte(i), reencoded.getByte(i));
            } finally {
                encoded.release();
                reencoded.release();
            }
        }
    }
}
