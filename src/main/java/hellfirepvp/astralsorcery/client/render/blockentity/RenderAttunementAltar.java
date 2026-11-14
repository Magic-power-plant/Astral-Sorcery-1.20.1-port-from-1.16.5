/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.model.builtin.ModelAttunementAltar;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.blockentity.altar.AttunementAltarBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderAttunementAltar
 * Created by HellFirePvP
 * Date: 17.11.2019 / 07:52
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, MatrixStack#push/pop/rotate -&gt; PoseStack#pushPose/popPose/mulPose,
 * Vector3f.XP -&gt; com.mojang.math.Axis.XP and MathHelper -&gt; Mth.
 */
public class RenderAttunementAltar extends CustomBlockEntityRenderer<AttunementAltarBlockEntity> {

    private static final ModelAttunementAltar MODEL_ATTUNEMENT_ALTAR = new ModelAttunementAltar();

    public RenderAttunementAltar(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(AttunementAltarBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        renderStack.pushPose();
        renderStack.translate(0.5, 0.5, 0.5);
        renderStack.mulPose(Axis.XP.rotationDegrees(180));
        MODEL_ATTUNEMENT_ALTAR.render(renderStack, renderTypeBuffer, combinedLight, combinedOverlay);
        renderStack.popPose();

        float spinDur = AttunementAltarBlockEntity.MAX_START_ANIMATION_SPIN;
        float spinStart = AttunementAltarBlockEntity.MAX_START_ANIMATION_TICK;

        float startY = -1.2F;
        float endY   = -0.5F;
        float tickPartY = (endY - startY) / spinStart;
        float prevPosY = endY + (blockEntity.prevActivationTick * tickPartY);
        float posY     = endY + (blockEntity.activationTick     * tickPartY);
        float framePosY = RenderingVectorUtils.interpolate(prevPosY, posY, pTicks);

        double generalAnimationTick = (ClientScheduler.getClientTick() + pTicks) / 4D;
        if (blockEntity.animate) {
            if (blockEntity.tesrLocked) {
                blockEntity.tesrLocked = false;
            }
        } else {
            if (blockEntity.tesrLocked) {
                generalAnimationTick = 7.25D;
            } else {
                if (Math.abs((generalAnimationTick % spinDur) - 7.25D) <= 0.3125) {
                    generalAnimationTick = 7.25D;
                    blockEntity.tesrLocked = true;
                }
            }
        }

        for (int i = 1; i < 9; i++) {
            float incrementer = (spinDur / 8F) * i;

            double aFrame =     generalAnimationTick + incrementer;
            double prevAFrame = generalAnimationTick + incrementer - 1;
            double renderFrame = RenderingVectorUtils.interpolate(prevAFrame, aFrame, 0);

            double partRenderFrame = (renderFrame % spinDur) / spinDur;
            float normalized = (float) (partRenderFrame * 2F * Math.PI);

            float xOffset = Mth.cos(normalized);
            float zOffset = Mth.sin(normalized);
            float rotation = RenderingVectorUtils.interpolate(blockEntity.prevActivationTick / spinStart, blockEntity.activationTick / spinStart, pTicks);

            renderStack.pushPose();
            renderStack.translate(0.5, framePosY, 0.5);
            renderStack.mulPose(Axis.XP.rotationDegrees(180));
            MODEL_ATTUNEMENT_ALTAR.renderHovering(renderStack, renderTypeBuffer.getBuffer(MODEL_ATTUNEMENT_ALTAR.getGeneralType()), combinedLight, combinedOverlay, 1F, 1F, 1F, 1F, xOffset, zOffset, rotation);
            renderStack.popPose();
        }
    }
}
