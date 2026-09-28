/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderEntityEmpty
 * Created by HellFirePvP
 * Date: 17.08.2019 / 13:08
 *
 * 1.16.5 -&gt; 1.20.1: EntityRendererManager -&gt; EntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, MatrixStack -&gt; PoseStack, AtlasTexture.LOCATION_BLOCKS_TEXTURE -&gt;
 * TextureAtlas.LOCATION_BLOCKS and #getEntityTexture -&gt; #getTextureLocation. Forge deleted
 * {@code IRenderFactory}, so the reference's inner {@code Factory} class is gone: {@code RegistryEntities}
 * registers this renderer as {@code RenderEntityEmpty::new} against
 * {@code EntityRenderersEvent.RegisterRenderers}.
 */
public class RenderEntityEmpty extends EntityRenderer<Entity> {

    public RenderEntityEmpty(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(Entity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {}

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

}
