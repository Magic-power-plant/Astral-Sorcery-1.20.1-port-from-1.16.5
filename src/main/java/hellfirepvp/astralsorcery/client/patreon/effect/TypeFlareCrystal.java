/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.effect;
import hellfirepvp.astralsorcery.client.patreon.entity.PatreonEntityRenderer;
import hellfirepvp.astralsorcery.client.resource.query.TextureQuery;
import hellfirepvp.astralsorcery.common.patreon.effect.FlareColor;
import hellfirepvp.astralsorcery.client.patreon.entity.PatreonCrystalFlareRenderer;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonPartialEntity;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeFlareCrystal
 * Created by HellFirePvP
 * Date: 05.04.2020 / 16:46
 */
public class TypeFlareCrystal extends ClientPatreonEffect {

    private final Color colorTheme;
    private final TextureQuery crystalTexture;

    public TypeFlareCrystal(UUID effectUUID, @Nullable FlareColor flareColor, Color colorTheme, TextureQuery crystalTexture) {
        super(effectUUID, flareColor);
        this.colorTheme = colorTheme;
        this.crystalTexture = crystalTexture;
    }

    @Nullable
    @Override
    public PatreonEntityRenderer createRenderer(PatreonPartialEntity entity) {
        //TODO fix layering flare before crystal
        return new PatreonCrystalFlareRenderer(entity, this)
                .setQueryTexture(this.crystalTexture)
                .setColorTheme(this.colorTheme);
    }
}
