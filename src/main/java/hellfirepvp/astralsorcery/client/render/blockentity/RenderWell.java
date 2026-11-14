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
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.blockentity.WellBlockEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.blockentity.PrecisionSingleFluidTank;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderWell
 * Created by HellFirePvP
 * Date: 22.09.2019 / 15:54
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, IVertexBuilder -&gt; VertexConsumer, TileEntity#getWorld/getPos -&gt;
 * BlockEntity#getLevel/getBlockPos and TextureAtlasSprite#getMinU/getMaxU/getMinV/getMaxV -&gt;
 * #getU0/getU1/getV0/getV1. Forge deleted FluidAttributes in 1.19.3, so
 * {@code fluid.getAttributes().getColor(world, pos)} becomes
 * {@code IClientFluidTypeExtensions.of(fluid).getTintColor(FluidState, BlockAndTintGetter, BlockPos)} - the same
 * world/position-sensitive tint hook, fed the fluid's default state since the tank only stores a FluidStack
 * (this matches the convention already used in client/effect/source/FXSourceLiquidFountain).
 */
public class RenderWell extends CustomBlockEntityRenderer<WellBlockEntity> {

    public RenderWell(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(WellBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        PrecisionSingleFluidTank tank = blockEntity.getTank();
        if (!tank.getFluid().isEmpty() && tank.getFluidAmount() > 0) {
            FluidStack contained = tank.getFluid();
            TextureAtlasSprite tas = RenderingUtils.getParticleTexture(contained);
            Color fluidColor = new Color(IClientFluidTypeExtensions.of(contained.getFluid())
                    .getTintColor(contained.getFluid().defaultFluidState(), blockEntity.getLevel(), blockEntity.getBlockPos()));
            VertexConsumer buf = renderTypeBuffer.getBuffer(RenderTypesAS.TER_WELL_LIQUID);

            Vector3 offset = new Vector3(0.5D, 0.32D, 0.5D).addY(tank.getPercentageFilled() * 0.6);

            RenderingDrawUtils.renderAngleRotatedTexturedRectVB(buf, renderStack, offset, Vector3.RotAxis.Y_AXIS, (float) Math.toRadians(45F), 0.54F,
                    tas.getU0(), tas.getV0(), tas.getU1() - tas.getU0(), tas.getV1() - tas.getV0(),
                    fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255);
        }

        ItemStack catalyst = blockEntity.getInventory().getStackInSlot(0);
        if (!catalyst.isEmpty()) {
            //AS-TER-03 (resolved): the note claimed TileWell extends TileEntitySynchronized and so has no tick
            //counter. Stale - TileWell extends TileReceiverBase -> TileNetwork -> TileEntityTick, which provides
            //getTicksExisted() and increments it in super.tick() before TileWell's isClientSide guard, so the
            //counter does advance on the client. Passing 0 froze the catalyst instead of letting it bob and spin.
            //(1.16.5 client/render/tile/RenderWell.java:58)
            RenderingUtils.renderItemAsEntity(catalyst, renderStack, renderTypeBuffer, 0.5F, 0.75F, 0.5F, combinedLight, pTicks, blockEntity.getTicksExisted());
        }
    }
}
