/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.model.builtin.ModelTelescope;
import hellfirepvp.astralsorcery.common.tile.TileTelescope;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderTelescope
 * Created by HellFirePvP
 * Date: 15.01.2020 / 17:11
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, MatrixStack#push/pop/rotate -&gt; PoseStack#pushPose/popPose/mulPose and Vector3f.XP/YP -&gt;
 * com.mojang.math.Axis.XP/YP.
 */
public class RenderTelescope extends CustomTileEntityRenderer<TileTelescope> {

    private static final ModelTelescope MODEL_TELESCOPE = new ModelTelescope();

    public RenderTelescope(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(TileTelescope tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        renderStack.pushPose();
        renderStack.translate(0.5F, 1.5F, 0.5F);
        renderStack.mulPose(Axis.XP.rotationDegrees(180F));
        renderStack.mulPose(Axis.YP.rotationDegrees(180F + tile.getRotation().ordinal() * 45F));

        MODEL_TELESCOPE.render(renderStack, renderTypeBuffer, combinedLight, combinedOverlay);

        renderStack.popPose();
    }
}
