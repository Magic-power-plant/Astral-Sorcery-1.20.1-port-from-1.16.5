/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.client.util.MiscPlayEffect;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktOreScan
 * Created by HellFirePvP
 * Date: 01.06.2019 / 23:55
 */
//1.16.5 extends ASPacket<PktOreScan>; the 1.20.1 tree registers this packet through PacketChannel#register
//with static encode/decode/handle method references, so the Encoder/Decoder/Handler bodies live there.
public class PktOreScan {

    //1.16.5 inherits this Random from ASPacket
    private static final Random rand = new Random();

    private List<BlockPos> positions = new ArrayList<>();

    public PktOreScan() {}

    public PktOreScan(List<BlockPos> positions) {
        this.positions = positions;
    }

    public static void encode(PktOreScan msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeCollection(buf, msg.positions, ByteBufUtils::writePos);
    }

    public static PktOreScan decode(FriendlyByteBuf buf) {
        return new PktOreScan(ByteBufUtils.readList(buf, ByteBufUtils::readPos));
    }

    public static void handle(PktOreScan msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktOreScan packet) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        for (BlockPos at : packet.positions) {
            Vector3 atPos = new Vector3(at).add(0.5, 0.5, 0.5);
            atPos.add(rand.nextFloat() - rand.nextFloat(), rand.nextFloat() - rand.nextFloat(), rand.nextFloat() - rand.nextFloat());
            //1.16.5 Minecraft#world -> 1.20.1 Minecraft#level
            BlockState state = Minecraft.getInstance().level.getBlockState(at);
            //if (Mods.ORESTAGES.isPresent()) {
            //    if(changed.contains(state) || !ModIntegrationOreStages.canSeeOreClient(state)) {
            //        changed.add(state);
            //        continue;
            //    }
            //}
            MiscPlayEffect.playSingleBlockTumbleDepthEffect(atPos, state);
        }
    }

}
