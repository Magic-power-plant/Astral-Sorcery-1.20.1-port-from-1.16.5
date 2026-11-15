/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.perk;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.AllocationStatus;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreeConstellation;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;

import java.awt.Color;
import java.awt.Rectangle;

/** Constellation halos, hit bounds and discovery-gated constellation overlays. */
public final class PerkConstellationRenderer extends PerkHaloRenderer implements DynamicPerkRender {
    @Override
    public Rectangle.Float getBounds(PerkTreePoint<?> point, float scale) {
        float size = ((PerkTreeConstellation<?>) point).getPerkSpriteSize() * scale;
        return new Rectangle.Float(-size, -size, size * 2F, size * 2F);
    }

    @Override
    protected float getHaloSize(PerkTreePoint<?> point, AllocationStatus status, float scale) {
        float size = ((PerkTreeConstellation<?>) point).getPerkSpriteSize() * scale;
        if (status.isAllocated()) size *= 1.3F;
        return size;
    }

    @Override
    public void renderAt(PerkTreePoint<?> point, AllocationStatus status, PoseStack renderStack,
                         long spriteOffsetTick, float pTicks, float x, float y, float zLevel, float scale) {
        PerkTreeConstellation<?> constellationPoint = (PerkTreeConstellation<?>) point;
        IConstellation constellation = constellationPoint.getAssociatedConstellation();
        if (constellation == null || !ResearchHelper.getClientProgress().hasConstellationDiscovered(constellation)) return;
        float size = constellationPoint.getPerkSpriteSize() * scale * 0.85F;
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        try {
            RenderingConstellationUtils.renderConstellationIntoGUI(Color.WHITE, constellation, renderStack,
                    x - size, y - size, 0, size * 2, size * 2, 3 * scale,
                    () -> 0.8F, true, false);
        } finally {
            RenderSystem.disableBlend();
        }
    }
}
