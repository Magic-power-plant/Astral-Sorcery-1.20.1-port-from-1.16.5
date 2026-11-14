/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.blockentity.crystal.CollectorCrystalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderCollectorCrystal
 * Created by HellFirePvP
 * Date: 28.05.2020 / 18:37
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, MatrixStack#push/pop -&gt; PoseStack#pushPose/popPose and TileEntity#getPos -&gt;
 * BlockEntity#getBlockPos.
 */
public class RenderCollectorCrystal extends CustomBlockEntityRenderer<CollectorCrystalBlockEntity> {

    public RenderCollectorCrystal(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(CollectorCrystalBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        if (!blockEntity.doesSeeSky()) {
            return;
        }
        Color color = blockEntity.getCollectorType().getDisplayColor();
        long seed = RenderingUtils.getPositionSeed(blockEntity.getBlockPos());

        renderStack.pushPose();
        renderStack.translate(0.5F, 0.5F, 0.5F);

        RenderingDrawUtils.renderLightRayFan(renderStack, renderTypeBuffer, color, seed, 24, 24, 12);

        seed ^= 0x54FF129A4B11C382L;
        if (blockEntity.isEnhanced() && blockEntity.getAttunedConstellation() != null) {
            color = blockEntity.getAttunedConstellation().getConstellationColor();
        }

        RenderingDrawUtils.renderLightRayFan(renderStack, renderTypeBuffer, color, seed, 24, 24, 12);
        renderStack.popPose();
    }
}
