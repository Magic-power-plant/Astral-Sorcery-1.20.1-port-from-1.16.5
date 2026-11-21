/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktShootEntity
 * Created by HellFirePvP
 * Date: 02.06.2019 / 00:14
 */
public class PktShootEntity {

    //1.16.5 inherits this from ASPacket; the target registers this packet through PacketChannel's
    //static encode/decode/handle style, so the shared random lives here.
    private static final Random rand = new Random();

    private int entityId = -1;
    private Vector3 motionVector = null;
    private boolean hasEffect = false;
    private float effectLength = 0;

    public PktShootEntity() {}

    public PktShootEntity(int entityId, Vector3 motionVector) {
        this.entityId = entityId;
        this.motionVector = motionVector;
    }

    public PktShootEntity setEffectLength(float length) {
        this.hasEffect = true;
        this.effectLength = length;
        return this;
    }

    public static void encode(PktShootEntity msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        ByteBufUtils.writeOptional(buf, msg.motionVector, ByteBufUtils::writeVector);
        buf.writeBoolean(msg.hasEffect);
        buf.writeFloat(msg.effectLength);
    }

    public static PktShootEntity decode(FriendlyByteBuf buf) {
        PktShootEntity pkt = new PktShootEntity(
                buf.readInt(),
                ByteBufUtils.readOptional(buf, ByteBufUtils::readVector));
        pkt.hasEffect = buf.readBoolean();
        pkt.effectLength = buf.readFloat();
        return pkt;
    }

    public static void handle(PktShootEntity msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktShootEntity packet) {
        //1.16.5 resolved the client world through LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT);
        //the ported tree reads it off the Minecraft instance the same way the other client-bound packets do.
        Optional<Level> world = Optional.ofNullable(Minecraft.getInstance().level);
        // 1.16.5 World#getEntityByID -> 1.20.1 Level#getEntity
        Entity entity = world.map(w -> w.getEntity(packet.entityId)).orElse(null);
        if (entity != null) {
            // 1.16.5 Entity#setMotion -> 1.20.1 Entity#setDeltaMovement
            entity.setDeltaMovement(packet.motionVector.toVector3d());

            if (packet.hasEffect) {
                // 1.16.5 Entity#getPosY/#getHeight -> 1.20.1 Entity#getY/#getBbHeight
                Vector3 origin = Vector3.atEntityCenter(entity)
                        .setY(entity.getY() + entity.getBbHeight());
                // 1.16.5 Entity#getLookVec -> 1.20.1 Entity#getLookAngle
                Vector3 look = new Vector3(entity.getLookAngle()).normalize().multiply(packet.effectLength * 18);
                Vector3 motionReverse = look.clone().normalize().multiply(-0.4 * packet.effectLength);

                Vector3 perp = look.clone().perpendicular().normalize().multiply(6F);
                for (int i = 0; i < 300; i++) {
                    Vector3 at = look.clone()
                            .multiply(0.5F + rand.nextFloat() * 2F)
                            .add(perp.clone().rotate(rand.nextFloat() * 360, look).multiply(0.5F + rand.nextFloat()))
                            .add(origin);

                    FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                            .spawn(at)
                            .alpha(VFXAlphaFunction.FADE_OUT)
                            .setAlphaMultiplier(0.75F)
                            .setScaleMultiplier(0.7F + rand.nextFloat() * 0.35F)
                            .setMaxAge(20 + rand.nextInt(15));

                    if (rand.nextBoolean()) {
                        p.color(VFXColorFunction.WHITE)
                                .setScaleMultiplier(0.3F + rand.nextFloat() * 0.15F);
                    } else {
                        p.color(VFXColorFunction.constant(ColorsAS.CONSTELLATION_VICIO));
                    }
                    p.setMotion(motionReverse);
                }
            }
        }
    }

}
