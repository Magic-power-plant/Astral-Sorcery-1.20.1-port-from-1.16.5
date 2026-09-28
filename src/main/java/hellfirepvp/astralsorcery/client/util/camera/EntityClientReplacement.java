package hellfirepvp.astralsorcery.client.util.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of 1.16.5 {@code EntityClientReplacement}: a client-only copy of the local player that is rendered in the
 * player's place while the real player entity acts as the camera during a camera flight.
 */
@OnlyIn(Dist.CLIENT)
public class EntityClientReplacement extends AbstractClientPlayer {

    public EntityClientReplacement() {
        super(Minecraft.getInstance().level, Minecraft.getInstance().player.getGameProfile());
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.isModelPartShown(part);
    }
}
