/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinParticleManager
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5: ParticleManager#renderParticles(MatrixStack, IRenderTypeBuffer$Impl, LightTexture,
 *         ActiveRenderInfo, float, ClippingHelper) - the Forge-added overload that takes the
 *         frustum, hence remap = false.
 * 1.20.1: ParticleEngine#render(PoseStack, MultiBufferSource$BufferSource, LightTexture, Camera,
 *         float, Frustum). ParticleEngine has two "render" overloads (the 5-arg vanilla one and
 *         this 6-arg Forge one), so the full descriptor is mandatory to disambiguate. Verified
 *         against forge-1.20.1-47.4.20-srg.jar: the 5-arg vanilla overload is SRG-named
 *         m_107336_ while the 6-arg Forge overload keeps the name "render" in production too -
 *         so remap = false is both required and correct.
 */
@Mixin(ParticleEngine.class)
public class MixinParticleManager {

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V",
            at = @At("RETURN"),
            remap = false
    )
    public void renderParticles(PoseStack poseStack, MultiBufferSource.BufferSource buffer, LightTexture lightTexture, Camera camera, float pTicks, Frustum frustum, CallbackInfo ci) {
        EffectHandler.getInstance().render(poseStack, pTicks);

        //Setup GL states again
        //Seriously, keep a clean GL state for once mojang.
        //VERIFY_PORT[AS-MIX-01]: 1.16.5 additionally called RenderSystem.enableAlphaTest() and
        //GlStateManager.enableTexture() here. Neither exists in 1.20.1 - the fixed-function alpha
        //test was replaced by shader-side discard and the texture enable/disable state was dropped
        //entirely from GlStateManager/RenderSystem (verified with javap: no enableAlphaTest and no
        //enableTexture/_enableTexture on either class). The remaining two calls are the 1:1
        //equivalents (GlStateManager.enableDepthTest -> RenderSystem.enableDepthTest, which is the
        //render-thread-checked wrapper around GlStateManager._enableDepthTest).
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
    }
}
