/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomTileEntityRenderer
 * Created by HellFirePvP
 * Date: 21.09.2019 / 15:29
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRenderer was turned into the BlockEntityRenderer interface (1.17), so this is an
 * {@code implements} instead of an {@code extends} and there is no super constructor to call - the
 * BlockEntityRendererProvider.Context parameter is kept because every subclass is registered through a
 * {@code ::new} reference against EntityRenderersEvent.RegisterRenderers. TileEntity -&gt; BlockEntity,
 * MatrixStack -&gt; PoseStack, IRenderTypeBuffer -&gt; MultiBufferSource.
 */
public abstract class CustomTileEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    public CustomTileEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public abstract void render(T tile, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay);

}
