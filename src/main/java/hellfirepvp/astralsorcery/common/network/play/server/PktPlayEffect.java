package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.client.util.MiscPlayEffect;
import hellfirepvp.astralsorcery.common.auxiliary.BlockBreakHelper;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffect;
import hellfirepvp.astralsorcery.common.constellation.effect.aoe.CEffectAevitas;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLensFire;
import hellfirepvp.astralsorcery.common.item.wand.ItemWand;
import hellfirepvp.astralsorcery.common.starlight.network.handler.BlockTransmutationHandler;
import hellfirepvp.astralsorcery.common.blockentity.*;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.CelestialStrike;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.time.TimeStopEffectHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PktPlayEffect {

    private Type type;
    private FriendlyByteBuf data = null;

    public PktPlayEffect() {}

    public PktPlayEffect(Type type) {
        this.type = type;
    }

    public PktPlayEffect addData(Consumer<FriendlyByteBuf> dataWriter) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        dataWriter.accept(buf);
        this.data = buf;
        return this;
    }

    public FriendlyByteBuf getExtraData() {
        return data;
    }

    public static void encode(PktPlayEffect msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeEnumValue(buf, msg.type);
        if (msg.data != null) {
            buf.writeBytes(msg.data);
        }
    }

    public static PktPlayEffect decode(FriendlyByteBuf buf) {
        Type type = ByteBufUtils.readEnumValue(buf, Type.class);
        PktPlayEffect pkt = new PktPlayEffect(type);
        ByteBuf b = Unpooled.buffer(buf.readableBytes());
        buf.readBytes(b);
        pkt.data = new FriendlyByteBuf(b);
        return pkt;
    }

    public static void handle(PktPlayEffect msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktPlayEffect packet) {
        packet.type.runEffect().accept(packet);
    }

    public enum Type {
        LIGHTNING,
        BEAM_BREAK,
        BLOCK_EFFECT,
        BLOCK_EFFECT_TUMBLE,
        ROCK_CRYSTAL_COLUMN,
        ROCK_CRYSTAL_SPARKS,
        SMALL_CRYSTAL_BREAK,
        GEM_CRYSTAL_BREAK,
        CROP_GROWTH,
        MELT_BLOCK,
        CELESTIAL_STRIKE,
        ALTAR_RECIPE_FINISH,
        INFUSER_RECIPE_FINISH,
        BLOCK_TRANSMUTATION_TICK,
        TIME_FREEZE_EFFECT,
        LIQUID_FOUNTAIN,
        CONSTELLATION_EFFECT_PING,
        BLOCK_HARVEST_DRAW,
        GATEWAY_REVOKE_EFFECT,
        LIQUID_INTERACTION_LINE,
        FOUNTAIN_TRANSITION_SEGMENT,
        FOUNTAIN_REPLACE_EFFECT,
        ;

        @OnlyIn(Dist.CLIENT)
        private Consumer<PktPlayEffect> runEffect() {
            switch (this) {
                case LIGHTNING:
                    return MiscPlayEffect::fireLightning;
                case ROCK_CRYSTAL_COLUMN:
                    return ItemWand::playEffect;
                case ROCK_CRYSTAL_SPARKS:
                    return ItemWand::playUndergroundEffect;
                case SMALL_CRYSTAL_BREAK:
                    return MiscPlayEffect::catalystBurst;
                case GEM_CRYSTAL_BREAK:
                    return MiscPlayEffect::gemCrystalBurst;
                case CROP_GROWTH:
                    return CEffectAevitas::playParticles;
                case TIME_FREEZE_EFFECT:
                    return TimeStopEffectHelper::playEntityParticles;
                case BEAM_BREAK:
                    return BlockBreakHelper::blockBreakAnimation;
                case BLOCK_EFFECT:
                    return MiscPlayEffect::playBlockEffects;
                case BLOCK_EFFECT_TUMBLE:
                    return MiscPlayEffect::playTumbleBlockEffects;
                case MELT_BLOCK:
                    return ItemColoredLensFire::playParticles;
                case CELESTIAL_STRIKE:
                    return CelestialStrike::playEffect;
                case ALTAR_RECIPE_FINISH:
                    return AltarBlockEntity::finishCraftingEffects;
                case INFUSER_RECIPE_FINISH:
                    return InfuserBlockEntity::finishCraftingEffects;
                case BLOCK_TRANSMUTATION_TICK:
                    return BlockTransmutationHandler::playTransmutation;
                case LIQUID_FOUNTAIN:
                    return MiscPlayEffect::liquidFountain;
                case CONSTELLATION_EFFECT_PING:
                    return ConstellationEffect::playConstellationPing;
                case BLOCK_HARVEST_DRAW:
                    return TreeBeaconBlockEntity::playDrawParticles;
                case GATEWAY_REVOKE_EFFECT:
                    return CelestialGatewayBlockEntity::playAccessRevokeEffect;
                case LIQUID_INTERACTION_LINE:
                    return ChaliceBlockEntity::drawLiquidLine;
                case FOUNTAIN_TRANSITION_SEGMENT:
                    return FountainBlockEntity::playTransitionEffect;
                case FOUNTAIN_REPLACE_EFFECT:
                    return FountainBlockEntity::replaceEffect;
            }
            return (pkt) -> {};
        }

    }

}
