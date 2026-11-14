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
import hellfirepvp.astralsorcery.common.blockentity.InfuserBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderInfuser
 * Created by HellFirePvP
 * Date: 09.11.2019 / 21:51
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource and MatrixStack#push/pop -&gt; PoseStack#pushPose/popPose.
 */
public class RenderInfuser extends CustomBlockEntityRenderer<InfuserBlockEntity> {

    public RenderInfuser(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(InfuserBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        ItemStack stack = blockEntity.getItemInput();
        if (!stack.isEmpty()) {
            renderStack.pushPose();
            renderStack.translate(0.5F, 0.75F, 0.5F);
            //1.16.5 client/render/tile/RenderInfuser.java:37 passes the tile's tick counter as the ItemEntity
            //age, which drives the input stack's bob and spin. TileInfuser now extends TileEntityTick again, so
            //the counter exists; previously a literal 0 was passed and the stack rendered frozen.
            RenderingUtils.renderItemAsEntity(stack, renderStack, renderTypeBuffer, 0, 0, 0, combinedLight, pTicks, blockEntity.getTicksExisted());
            renderStack.popPose();
        }
    }
}
