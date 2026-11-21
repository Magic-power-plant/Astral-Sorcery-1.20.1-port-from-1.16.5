/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.entity;
import hellfirepvp.astralsorcery.client.patreon.effect.ClientPatreonEffect;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonPartialEntity;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.effect.vfx.FXCrystal;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.query.TextureQuery;
import hellfirepvp.astralsorcery.api.effect.Effects;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PatreonCrystalFlareRenderer
 * Created by HellFirePvP
 * Date: 05.04.2020 / 16:53
 */
public class PatreonCrystalFlareRenderer extends PatreonFlareRenderer {

    private TextureQuery queryTexture = new TextureQuery(AssetLoader.TextureLocation.MODEL, "crystal_blue");
    private Color colorTheme = Color.WHITE;

    private FXCrystal crystalEffect = null;

    public PatreonCrystalFlareRenderer(PatreonPartialEntity entity, ClientPatreonEffect effect) {
        super(entity, effect);
    }

    public PatreonCrystalFlareRenderer setQueryTexture(TextureQuery queryTexture) {
        this.queryTexture = queryTexture;
        return this;
    }

    public PatreonCrystalFlareRenderer setColorTheme(Color colorTheme) {
        this.colorTheme = colorTheme;
        return this;
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (this.crystalEffect != null) {
            FXCrystal crystal = this.crystalEffect;
            if (crystal.isRemoved() && RenderingConfig.CONFIG.patreonEffects.get()) {
                Effects.refresh(crystal, EffectTemplatesAS.CRYSTAL);
            }
        } else {
            this.crystalEffect = Effects.of(EffectTemplatesAS.CRYSTAL)
                    .spawn(getPos())
                    .setTexture(this.queryTexture)
                    .setLightRayColor(this.colorTheme)
                    .setScaleMultiplier(0.03F)
                    .position((fx, position, motionToBeMoved) -> this.getPos().clone())
                    .refresh((fx) -> !this.isRemoved() && RenderingConfig.CONFIG.patreonEffects.get());
        }
    }
    @Override
    public void close() {
        super.close();
        if (crystalEffect != null) {
            crystalEffect.requestRemoval();
            crystalEffect = null;
        }
    }
}
