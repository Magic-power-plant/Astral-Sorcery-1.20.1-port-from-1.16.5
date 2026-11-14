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
import hellfirepvp.astralsorcery.client.model.builtin.ModelRefractionTable;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.blockentity.RefractionTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderRefractionTable
 * Created by HellFirePvP
 * Date: 26.04.2020 / 21:17
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, IVertexBuilder -&gt; VertexConsumer, MatrixStack#push/pop/rotate -&gt;
 * PoseStack#pushPose/popPose/mulPose, Vector3f.XP -&gt; com.mojang.math.Axis.XP,
 * ItemCameraTransforms.TransformType -&gt; ItemDisplayContext and ItemRenderer#renderItem(..) -&gt;
 * ItemRenderer#renderStatic(..), which additionally wants the Level and a model seed - the tile's own level and
 * seed 0 reproduce the vanilla defaults the 1.16.5 overload filled in.
 */
public class RenderRefractionTable extends CustomBlockEntityRenderer<RefractionTableBlockEntity> {

    private static final ModelRefractionTable MODEL_REFRACTION_TABLE = new ModelRefractionTable();

    public RenderRefractionTable(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(RefractionTableBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        if (!blockEntity.hasParchment() && !blockEntity.getInputStack().isEmpty()) {
            ItemStack input = blockEntity.getInputStack();

            renderStack.pushPose();
            renderStack.translate(0.5F, 0.85F, 0.5F);
            renderStack.scale(0.625F, 0.625F, 0.625F);

            Minecraft.getInstance().getItemRenderer().renderStatic(input, ItemDisplayContext.GROUND, combinedLight, combinedOverlay, renderStack, renderTypeBuffer, blockEntity.getLevel(), 0);

            renderStack.popPose();
        }

        renderStack.pushPose();
        renderStack.translate(0.5F, 1.5F, 0.5F);
        renderStack.mulPose(Axis.XP.rotationDegrees(180F));

        RenderType type = MODEL_REFRACTION_TABLE.getGeneralType();
        VertexConsumer vb = renderTypeBuffer.getBuffer(type);
        MODEL_REFRACTION_TABLE.renderFrame(renderStack, vb,
                combinedLight, combinedOverlay, 1F, 1F, 1F, 1F, blockEntity.hasParchment());
        RenderingUtils.refreshDrawing(vb, type);

        if (!blockEntity.getGlassStack().isEmpty()) {
            type = RenderTypesAS.MODEL_REFRACTION_TABLE_GLASS;
            vb = renderTypeBuffer.getBuffer(type);
            MODEL_REFRACTION_TABLE.renderGlass(renderStack, vb,
                    combinedLight, combinedOverlay, 1F, 1F, 1F, 1F);
            RenderingUtils.refreshDrawing(vb, type);
        }

        renderStack.popPose();
    }
}
