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
import hellfirepvp.astralsorcery.client.screen.journal.perk.group.PerkPointRenderGroup;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.draw.BufferContext;
import hellfirepvp.astralsorcery.common.perk.AllocationStatus;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import net.minecraft.util.Tuple;

import java.awt.Rectangle;
import java.util.Collection;

/** Stateless point renderer; model instances never own rendering capabilities. */
public class PerkPointRenderer implements PerkRender {
    @Override
    public void addGroups(Collection<PerkRenderGroup> groups) {
        groups.add(PerkPointRenderGroup.INSTANCE);
    }

    public Rectangle.Float getBounds(PerkTreePoint<?> point, float scale) {
        float size = point.getRenderSize() * scale;
        return new Rectangle.Float(-size, -size, size * 2F, size * 2F);
    }

    @Override
    public Rectangle.Float renderPerkAtBatch(PerkTreePoint<?> point, BatchPerkContext drawCtx, PoseStack renderStack,
                                           AllocationStatus status, long spriteOffsetTick, float pTicks,
                                           float x, float y, float zLevel, float scale) {
        SpriteSheetResource tex = PerkSprites.point(status);
        BatchPerkContext.TextureObjectGroup group = PerkPointRenderGroup.INSTANCE.getGroup(tex);
        if (group == null) return new Rectangle.Float();
        BufferContext buffer = drawCtx.getContext(group);
        float size = point.getRenderSize() * scale;
        Tuple<Float, Float> uv = tex.getUVOffset(spriteOffsetTick);
        RenderingGuiUtils.rect(buffer, renderStack, x - size, y - size, zLevel, size * 2F, size * 2F)
                .tex(uv.getA(), uv.getB(), tex.getULength(), tex.getVLength())
                .draw();
        return getBounds(point, scale);
    }
}
