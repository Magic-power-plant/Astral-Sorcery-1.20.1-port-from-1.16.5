/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.perk;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.perk.AllocationStatus;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreeGem;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;

/** Dynamic socket contents are drawn after the point and halo batches. */
public final class PerkGemRenderer extends PerkHaloRenderer implements DynamicPerkRender {
    @Override
    public void renderAt(PerkTreePoint<?> point, AllocationStatus status, PoseStack renderStack,
                         long spriteOffsetTick, float pTicks, float x, float y, float zLevel, float scale) {
        PerkTreeGem<?> gem = (PerkTreeGem<?>) point;
        ItemStack stack = gem.getPerk().getContainedItem(Minecraft.getInstance().player, LogicalSide.CLIENT);
        if (!stack.isEmpty()) {
            renderStack.pushPose();
            try {
                renderStack.translate(x - 8 * scale, y - 8 * scale, zLevel - 50F);
                renderStack.scale(scale, scale, 1F);
                RenderingUtils.renderItemStackGUI(renderStack, stack, null);
            } finally {
                renderStack.popPose();
            }
        }
    }
}
