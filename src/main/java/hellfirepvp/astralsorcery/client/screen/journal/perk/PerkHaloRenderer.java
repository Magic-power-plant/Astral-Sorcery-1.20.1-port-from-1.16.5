/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.perk;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.screen.journal.perk.group.PerkPointHaloRenderGroup;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.draw.BufferContext;
import hellfirepvp.astralsorcery.common.perk.AllocationStatus;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import net.minecraft.util.Tuple;

import java.awt.Rectangle;
import java.util.Collection;

/** Shared halo pass for major, gem and constellation points. */
public class PerkHaloRenderer extends PerkPointRenderer {
    @Override
    public void addGroups(Collection<PerkRenderGroup> groups) {
        super.addGroups(groups);
        groups.add(PerkPointHaloRenderGroup.INSTANCE);
    }

    protected float getHaloSize(PerkTreePoint<?> point, AllocationStatus status, float scale) {
        float size = point.getRenderSize() * 0.8F * scale;
        if (status.isAllocated()) size *= 1.5;
        return size;
    }

    @Override
    public Rectangle.Float renderPerkAtBatch(PerkTreePoint<?> point, BatchPerkContext drawCtx, PoseStack renderStack,
                                           AllocationStatus status, long spriteOffsetTick, float pTicks,
                                           float x, float y, float zLevel, float scale) {
        SpriteSheetResource tex = PerkSprites.halo(status);
        BatchPerkContext.TextureObjectGroup group = PerkPointHaloRenderGroup.INSTANCE.getGroup(tex);
        if (group == null) return new Rectangle.Float();
        BufferContext buffer = drawCtx.getContext(group);
        float size = getHaloSize(point, status, scale);
        Tuple<Float, Float> uv = tex.getUVOffset(spriteOffsetTick);
        RenderingGuiUtils.rect(buffer, renderStack, x - size, y - size, zLevel, size * 2F, size * 2F)
                .color(1F, 1F, 1F, 0.85F)
                .tex(uv.getA(), uv.getB(), tex.getULength(), tex.getVLength())
                .draw();
        super.renderPerkAtBatch(point, drawCtx, renderStack, status, spriteOffsetTick, pTicks, x, y, zLevel, scale);
        return getBounds(point, scale);
    }
}
