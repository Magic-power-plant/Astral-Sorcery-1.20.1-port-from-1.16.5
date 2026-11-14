/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.blockentity.SpectralRelayBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderSpectralRelay
 * Created by HellFirePvP
 * Date: 22.09.2019 / 18:33
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource and MatrixStack#push/pop -&gt; PoseStack#pushPose/popPose.
 */
public class RenderSpectralRelay extends CustomBlockEntityRenderer<SpectralRelayBlockEntity> {

    public RenderSpectralRelay(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(SpectralRelayBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        ItemStack stack = blockEntity.getInventory().getStackInSlot(0);
        if (!stack.isEmpty()) {
            renderStack.pushPose();
            renderStack.translate(0.5F, 0.1F, 0.5F);
            //AS-TER-05 (resolved): the note claimed TileSpectralRelay extends TileEntitySynchronized. Stale - it
            //extends TileEntityTick directly (TileSpectralRelay.java:59) and calls super.tick() before its
            //isClientSide guard, so getTicksExisted() advances on the client. Passing 0 froze the lens.
            //(1.16.5 client/render/tile/RenderSpectralRelay.java:37)
            RenderingUtils.renderItemAsEntity(stack, renderStack, renderTypeBuffer, 0, 0, 0, combinedLight, pTicks, blockEntity.getTicksExisted());
            renderStack.popPose();
        }
    }
}
