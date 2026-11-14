/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedCelestialCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedRockCrystal;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.blockentity.ritual.RitualPedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderRitualPedestal
 * Created by HellFirePvP
 * Date: 28.05.2020 / 21:33
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, MatrixStack#push/pop -&gt; PoseStack#pushPose/popPose, TileEntity#getPos -&gt;
 * BlockEntity#getBlockPos. BlocksAS entries are DeferredRegister RegistryObjects in 1.20.1 instead of plain
 * Block fields, hence the added {@code .get()}.
 */
public class RenderRitualPedestal extends CustomBlockEntityRenderer<RitualPedestalBlockEntity> {

    public RenderRitualPedestal(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(RitualPedestalBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        ItemStack stack = blockEntity.getCurrentCrystal();
        if (stack.isEmpty()) {
            return;
        }

        ItemStack display = stack;
        if (display.getItem() instanceof ItemAttunedRockCrystal) {
            display = new ItemStack(BlocksAS.ROCK_COLLECTOR_CRYSTAL.get());
        }
        if (display.getItem() instanceof ItemAttunedCelestialCrystal) {
            display = new ItemStack(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get());
        }

        renderStack.pushPose();
        renderStack.translate(0.5F, 0.9F, 0.5F);
        renderStack.scale(2F, 2F, 2F);
        RenderingUtils.renderTranslucentItemStackModelGround(display, renderStack, Color.WHITE, Blending.DEFAULT, 255);
        renderStack.popPose();

        IWeakConstellation ritualConstellation = blockEntity.getRitualConstellation();
        if (ritualConstellation != null) {
            long seed = RenderingUtils.getPositionSeed(blockEntity.getBlockPos());
            int scales = blockEntity.isWorking() ? 24 : 12;
            int count = blockEntity.isWorking() ? 16 : 12;

            renderStack.pushPose();
            renderStack.translate(0.5F, 1.2F, 0.5F);

            RenderingDrawUtils.renderLightRayFan(renderStack, renderTypeBuffer, ritualConstellation.getConstellationColor(), seed, scales, scales, count);

            renderStack.popPose();
        }
    }
}
