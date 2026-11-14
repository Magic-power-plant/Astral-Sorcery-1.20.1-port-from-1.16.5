/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.effect;
import hellfirepvp.astralsorcery.client.render.entity.layer.StarryLayerRenderer;
import hellfirepvp.astralsorcery.common.patreon.effect.FlareColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeStarryPlayerLayer
 * Created by HellFirePvP
 * Date: 09.01.2021 / 11:01
 */
public class TypeStarryPlayerLayer extends ClientPatreonEffect {

    private final UUID playerUUID;
    private final java.util.function.BiPredicate<net.minecraft.world.entity.player.Player, net.minecraft.world.entity.EquipmentSlot> renderTest;

    public TypeStarryPlayerLayer(UUID effectUUID, @Nullable FlareColor flareColor, UUID playerUUID) {
        super(effectUUID, flareColor);
        this.playerUUID = playerUUID;
        this.renderTest = (player, slot) -> this.playerUUID.equals(player.getUUID());
    }

    @Override
    public void initialize() {
        super.initialize();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerPlayerLayer();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void registerPlayerLayer() {
        //1.16.5: Entity#getUniqueID() -> Entity#getUUID()
        StarryLayerRenderer.addRender(renderTest);
    }
    @Override
    public void close() {
        StarryLayerRenderer.removeRender(renderTest);
    }
}
