/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.model.builtin.ModelLens;
import hellfirepvp.astralsorcery.client.model.builtin.ModelLensColored;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.blockentity.LensBlockEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;

import java.awt.*;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderLens
 * Created by HellFirePvP
 * Date: 21.09.2019 / 15:01
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context,
 * IRenderTypeBuffer -&gt; MultiBufferSource, IVertexBuilder -&gt; VertexConsumer, MatrixStack -&gt; PoseStack
 * (push/pop/rotate -&gt; pushPose/popPose/mulPose), Vector3f.XP/YP/ZP -&gt; com.mojang.math.Axis.XP/YP/ZP and
 * ModelRenderer#rotateAngleX -&gt; ModelPart#xRot.
 */
public class RenderLens extends CustomBlockEntityRenderer<LensBlockEntity> {

    private static final ModelLens MODEL_LENS = new ModelLens();
    private static final ModelLensColored MODEL_LENS_COLORED = new ModelLensColored();

    public RenderLens(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(LensBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        List<BlockPos> linked = blockEntity.getLinkedPositions();
        float degYaw = 0;
        float degPitch = 0;

        renderStack.pushPose();
        switch (blockEntity.getPlacedAgainst()) {
            case DOWN:
                if (!linked.isEmpty() && linked.size() == 1) {
                    BlockPos to = linked.get(0);
                    BlockPos from = blockEntity.getTrPos();
                    Vector3 dir = new Vector3(to).subtract(new Vector3(from));

                    degPitch = (float) Math.atan2(dir.getY(), Math.sqrt(dir.getX() * dir.getX() + dir.getZ() * dir.getZ()));

                    degYaw = (float) Math.atan2(dir.getX(), dir.getZ());

                    degYaw = 180F + (float) Math.toDegrees(-degYaw);
                    degPitch = (float) Math.toDegrees(degPitch);
                }

                renderStack.translate(0.5F, 1.5F, 0.5F);

                renderStack.mulPose(Axis.XP.rotationDegrees(180));
                renderStack.mulPose(Axis.YP.rotationDegrees(degYaw % 360));

                if (blockEntity.getColorType() != null) {
                    renderStack.pushPose();
                    renderStack.mulPose(Axis.YP.rotationDegrees(180));
                    renderLensColored(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, blockEntity.getColorType().getColor(), -degPitch);
                    renderStack.popPose();
                }
                renderLens(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, degPitch);
                break;
            case UP:
                if (!linked.isEmpty() && linked.size() == 1) {
                    BlockPos to = linked.get(0);
                    BlockPos from = blockEntity.getTrPos();
                    Vector3 dir = new Vector3(to).subtract(new Vector3(from));

                    degPitch = (float) Math.atan2(dir.getY(), Math.sqrt(dir.getX() * dir.getX() + dir.getZ() * dir.getZ()));

                    degYaw = (float) Math.atan2(dir.getX(), dir.getZ());

                    degYaw = 180F + (float) Math.toDegrees(-degYaw);
                    degPitch = (float) Math.toDegrees(degPitch);
                }

                renderStack.translate(0.5F, -0.5F, 0.5F);

                renderStack.mulPose(Axis.YP.rotationDegrees((-degYaw + 180) % 360));

                if (blockEntity.getColorType() != null) {
                    renderStack.pushPose();
                    renderStack.mulPose(Axis.YP.rotationDegrees(180));
                    renderLensColored(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, blockEntity.getColorType().getColor(), degPitch);
                    renderStack.popPose();
                }
                renderLens(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, -degPitch);
                break;
            case NORTH:
                if (!linked.isEmpty() && linked.size() == 1) {
                    BlockPos to = linked.get(0);
                    BlockPos from = blockEntity.getTrPos();
                    Vector3 dir = new Vector3(to).subtract(new Vector3(from));

                    degPitch = (float) Math.atan2(dir.getZ(), Math.sqrt(dir.getX() * dir.getX() + dir.getY() * dir.getY()));

                    degYaw = (float) Math.atan2(dir.getX(), dir.getY());

                    degYaw = 180F + (float) Math.toDegrees(-degYaw);
                    degPitch = (float) Math.toDegrees(degPitch);
                }

                renderStack.translate(0.5F, 0.5F, 1.5F);

                renderStack.mulPose(Axis.XP.rotationDegrees(270));
                renderStack.mulPose(Axis.YP.rotationDegrees((-degYaw + 180) % 360));

                if (blockEntity.getColorType() != null) {
                    renderStack.pushPose();
                    renderStack.mulPose(Axis.YP.rotationDegrees(180));
                    renderLensColored(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, blockEntity.getColorType().getColor(), -degPitch);
                    renderStack.popPose();
                }
                renderLens(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, degPitch);
                break;
            case SOUTH:
                if (!linked.isEmpty() && linked.size() == 1) {
                    BlockPos to = linked.get(0);
                    BlockPos from = blockEntity.getTrPos();
                    Vector3 dir = new Vector3(to).subtract(new Vector3(from));

                    degPitch = (float) Math.atan2(dir.getZ(), Math.sqrt(dir.getX() * dir.getX() + dir.getY() * dir.getY()));

                    degYaw = (float) Math.atan2(dir.getX(), dir.getY());

                    degYaw = 180F + (float) Math.toDegrees(-degYaw);
                    degPitch = (float) Math.toDegrees(degPitch);
                }

                renderStack.translate(0.5F, 0.5F, -0.5F);

                renderStack.mulPose(Axis.XP.rotationDegrees(90));
                renderStack.mulPose(Axis.YP.rotationDegrees(degYaw % 360));

                if (blockEntity.getColorType() != null) {
                    renderStack.pushPose();
                    renderStack.mulPose(Axis.YP.rotationDegrees(180));
                    renderLensColored(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, blockEntity.getColorType().getColor(), degPitch);
                    renderStack.popPose();
                }
                renderLens(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, -degPitch);
                break;
            case WEST:
                if (!linked.isEmpty() && linked.size() == 1) {
                    BlockPos to = linked.get(0);
                    BlockPos from = blockEntity.getTrPos();
                    Vector3 dir = new Vector3(to).subtract(new Vector3(from));

                    degPitch = (float) Math.atan2(dir.getX(), Math.sqrt(dir.getZ() * dir.getZ() + dir.getY() * dir.getY()));

                    degYaw = (float) Math.atan2(dir.getZ(), dir.getY());

                    degYaw = 180F + (float) Math.toDegrees(-degYaw);
                    degPitch = (float) Math.toDegrees(degPitch);
                }

                renderStack.translate(1.5F, 0.5F, 0.5F);

                renderStack.mulPose(Axis.ZP.rotationDegrees(90));
                renderStack.mulPose(Axis.YP.rotationDegrees((degYaw + 270 % 360)));

                if (blockEntity.getColorType() != null) {
                    renderStack.pushPose();
                    renderStack.mulPose(Axis.YP.rotationDegrees(180));
                    renderLensColored(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, blockEntity.getColorType().getColor(), -degPitch);
                    renderStack.popPose();
                }
                renderLens(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, degPitch);
                break;
            case EAST:
                if (!linked.isEmpty() && linked.size() == 1) {
                    BlockPos to = linked.get(0);
                    BlockPos from = blockEntity.getTrPos();
                    Vector3 dir = new Vector3(to).subtract(new Vector3(from));

                    degPitch = (float) Math.atan2(dir.getX(), Math.sqrt(dir.getZ() * dir.getZ() + dir.getY() * dir.getY()));

                    degYaw = (float) Math.atan2(dir.getZ(), dir.getY());

                    degYaw = 180F + (float) Math.toDegrees(-degYaw);
                    degPitch = (float) Math.toDegrees(degPitch);
                }

                renderStack.translate(-0.5F, 0.5F, 0.5F);

                renderStack.mulPose(Axis.ZP.rotationDegrees(270));
                renderStack.mulPose(Axis.YP.rotationDegrees((-degYaw + 90 % 360)));

                if (blockEntity.getColorType() != null) {
                    renderStack.pushPose();
                    renderStack.mulPose(Axis.YP.rotationDegrees(180));
                    renderLensColored(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, blockEntity.getColorType().getColor(), degPitch);
                    renderStack.popPose();
                }
                renderLens(renderStack, renderTypeBuffer, combinedLight, combinedOverlay, -degPitch);
                break;
            default:
                break;
        }
        renderStack.popPose();
    }

    private void renderLensColored(PoseStack renderStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, Color c, float pitch) {
        MODEL_LENS_COLORED.glass.    xRot = pitch * 0.017453292F;
        MODEL_LENS_COLORED.fitting1. xRot = pitch * 0.017453292F;
        MODEL_LENS_COLORED.fitting2. xRot = pitch * 0.017453292F;
        MODEL_LENS_COLORED.detail1_1.xRot = pitch * 0.017453292F;
        MODEL_LENS_COLORED.detail1.  xRot = pitch * 0.017453292F;

        VertexConsumer vb = buffer.getBuffer(RenderTypesAS.MODEL_LENS_COLORED_GLASS);
        MODEL_LENS_COLORED.renderGlass(renderStack, vb, combinedLight, combinedOverlay, c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F, 1F);
        RenderingUtils.refreshDrawing(vb, RenderTypesAS.MODEL_LENS_COLORED_GLASS);
        MODEL_LENS_COLORED.render(renderStack, buffer, combinedLight, combinedOverlay);
    }

    private void renderLens(PoseStack renderStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, float pitch) {
        MODEL_LENS.lens.xRot = pitch * 0.017453292F;

        MODEL_LENS.render(renderStack, buffer, combinedLight, combinedOverlay);
    }
}
