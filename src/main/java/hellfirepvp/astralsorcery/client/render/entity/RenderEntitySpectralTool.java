/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderEntitySpectralTool
 * Created by HellFirePvP
 * Date: 22.02.2020 / 14:28
 *
 * 1.16.5 -&gt; 1.20.1: EntityRendererManager -&gt; EntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, MatrixStack#push/pop/rotate -&gt; PoseStack#pushPose/popPose/mulPose, Vector3f.XP/YP/ZP -&gt;
 * com.mojang.math.Axis.XP/YP/ZP, Entity#getHeight -&gt; Entity#getBbHeight,
 * AtlasTexture.LOCATION_BLOCKS_TEXTURE -&gt; TextureAtlas.LOCATION_BLOCKS and #getEntityTexture -&gt;
 * #getTextureLocation. Forge deleted {@code IRenderFactory}, so the reference's inner {@code Factory} class is
 * gone: {@code RegistryEntities} registers this renderer as {@code RenderEntitySpectralTool::new} against
 * {@code EntityRenderersEvent.RegisterRenderers}, which is why the constructor is public instead of protected.
 */
public class RenderEntitySpectralTool extends EntityRenderer<EntitySpectralTool> {

    public RenderEntitySpectralTool(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EntitySpectralTool entity, float entityYaw, float partialTicks, PoseStack renderStack, MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty() || !entity.isAlive()) {
            return;
        }

        renderStack.pushPose();
        renderStack.translate(0, entity.getBbHeight() / 2, 0);
        renderStack.mulPose(Axis.YP.rotationDegrees(-entityYaw - 90));
        if (stack.getItem() instanceof AxeItem) {
            renderStack.mulPose(Axis.XP.rotationDegrees(180));
            renderStack.mulPose(Axis.ZP.rotationDegrees(270));
        }

        RenderingUtils.renderTranslucentItemStackModelGround(stack, renderStack, ColorsAS.SPECTRAL_TOOL, Blending.CONSTANT_ALPHA, 63);

        renderStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySpectralTool entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
