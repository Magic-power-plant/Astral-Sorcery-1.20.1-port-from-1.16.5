/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.blockentity.PrismBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderPrism
 * Created by HellFirePvP
 * Date: 21.09.2019 / 22:39
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, MatrixStack -&gt; PoseStack. The empty render body is verbatim from the reference - the TESR
 * exists only so TilePrism is registered with a renderer.
 */
public class RenderPrism extends CustomBlockEntityRenderer<PrismBlockEntity> {

    public RenderPrism(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(PrismBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {}

}
