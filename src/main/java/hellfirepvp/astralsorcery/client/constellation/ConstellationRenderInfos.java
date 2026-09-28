/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.constellation;

import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import net.minecraft.client.renderer.RenderType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConstellationRenderInfos
 * Created by HellFirePvP
 * Date: 25.12.2020 / 20:45
 */
public class ConstellationRenderInfos {

    private static final Map<IConstellation, ConstellationBackgroundInfo> backgroundRenderMap = new HashMap<>();

    public static void registerBackground(IConstellation cst, RenderType renderType, AbstractRenderableTexture texture) {
        backgroundRenderMap.put(cst, new ConstellationBackgroundInfo(renderType, texture));
    }

    // VERIFY_PORT[AS-CST-BG-01]: This is NOT a porting stub - the 1.16.5 reference
    // (hellfirepvp.astralsorcery.client.constellation.ConstellationRenderInfos#getBackgroundRenderInfo)
    // deliberately returns null with the map lookup commented out, which disables constellation
    // background rendering in every call site of RenderingConstellationUtils. Reproduced verbatim for
    // behavioural fidelity; re-enabling the lookup would be an upstream behaviour change, not a port fix.
    @Nullable
    public static ConstellationBackgroundInfo getBackgroundRenderInfo(IConstellation cst) {
        return null;//backgroundRenderMap.get(cst);
    }
}
