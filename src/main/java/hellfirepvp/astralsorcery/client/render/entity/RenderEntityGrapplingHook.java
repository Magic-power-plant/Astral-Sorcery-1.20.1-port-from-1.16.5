/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.common.entity.technical.EntityGrapplingHook;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderEntityGrapplingHook
 * Created by HellFirePvP
 * Date: 29.02.2020 / 20:04
 *
 * 1.16.5 -&gt; 1.20.1: EntityRendererManager -&gt; EntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, MatrixStack -&gt; PoseStack, MathHelper -&gt; Mth, DefaultVertexFormats -&gt;
 * DefaultVertexFormat, AtlasTexture.LOCATION_BLOCKS_TEXTURE -&gt; TextureAtlas.LOCATION_BLOCKS,
 * Entity#ticksExisted -&gt; Entity#tickCount and #getEntityTexture -&gt; #getTextureLocation. Forge deleted
 * {@code IRenderFactory}, so the reference's inner {@code Factory} class is gone: {@code RegistryEntities}
 * registers this renderer as {@code RenderEntityGrapplingHook::new} against
 * {@code EntityRenderersEvent.RegisterRenderers}, which is why the constructor is public instead of protected.
 */
public class RenderEntityGrapplingHook extends EntityRenderer<EntityGrapplingHook> {

    public RenderEntityGrapplingHook(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    //TODO_PORT[AS-ENT-01]: RenderSystem.disableAlphaTest()/enableAlphaTest() (1.16.5
    //client/render/entity/RenderEntityGrapplingHook.java:62, 95) were removed by the 1.17 core-profile rewrite; the
    //alpha test is part of the core shader program in 1.20.1 and cannot be toggled from outside a RenderType.
    //Dropped rather than emulated - the two draws below are alpha-blended anyway.
    @Override
    public void render(EntityGrapplingHook entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        int alphaMultiplier;
        if (entity.isDespawning()) {
            alphaMultiplier = Mth.clamp(127 - ((int) (entity.despawnPercentage(partialTicks) * 255F)), 0, 255);
        } else {
            alphaMultiplier = 255;
        }
        if (alphaMultiplier <= 1E-4) {
            return;
        }

        Vector3 entityPos = RenderingVectorUtils.interpolatePosition(entity, partialTicks);
        List<Vector3> line = entity.buildLine(partialTicks);

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.disableCull();

        //Main grappling hook sprite
        SpritesAS.SPR_GRAPPLING_HOOK.bindTexture();

        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingDrawUtils.renderFacingSpriteVB(buf, matrixStack,
                    entityPos.getX(), entityPos.getY(), entityPos.getZ(),
                    1.3F, 0F,
                    SpritesAS.SPR_GRAPPLING_HOOK, ClientScheduler.getClientTick() + entity.tickCount,
                    255, 255, 255, alphaMultiplier);
        });

        //Small line of particles
        TexturesAS.TEX_PARTICLE_LARGE.bindTexture();
        Blending.ADDITIVE_ALPHA.apply();

        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            for (Vector3 pos : line) {
                Vector3 at = pos.multiply(2).add(entityPos);
                RenderingDrawUtils.renderFacingFullQuadVB(buf, matrixStack,
                        at.getX(), at.getY(), at.getZ(),
                        0.3F, 0F,
                        50, 40, 180, (int) (alphaMultiplier * 0.8F));
            }
        });

        RenderSystem.enableCull();
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGrapplingHook entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
