/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.sky.astral.AstralSkyRenderer;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ChainingSkyRenderer
 * Created by HellFirePvP
 * Date: 13.01.2020 / 19:48
 *
 * 1.16.5 -&gt; 1.20.1: Forge deleted {@code ISkyRenderHandler} and the {@code DimensionRenderInfo#get/setSkyRenderHandler}
 * slot this class used to be plugged into. The only remaining custom sky hook is
 * {@code IForgeDimensionSpecialEffects#renderSky(..)}, which lives on {@link DimensionSpecialEffects} itself, so the
 * chaining renderer now <i>is</i> a {@link DimensionSpecialEffects} that wraps the one it replaced: every non-sky
 * method delegates to {@code existingSkyRenderer}, which keeps the wrapped dimension's fog/cloud/lightmap behaviour
 * bit-identical, and {@link #renderSky} carries the reference logic. Returning {@code true} from the hook means
 * "the sky has been drawn", which is the 1.20.1 spelling of "an ISkyRenderHandler was installed".
 */
public class ChainingSkyRenderer extends DimensionSpecialEffects {

    private final DimensionSpecialEffects existingSkyRenderer;

    //1.16.5 temporarily un-installed itself from the DimensionRenderInfo to reach the vanilla sky code. There is no
    //handler slot to null out any more, so re-entrancy is flagged explicitly instead - see renderVanillaSky(..).
    private boolean renderingVanillaSky = false;

    public ChainingSkyRenderer(DimensionSpecialEffects existingSkyRenderer) {
        super(existingSkyRenderer.getCloudHeight(),
                existingSkyRenderer.hasGround(),
                existingSkyRenderer.skyType(),
                existingSkyRenderer.forceBrightLightmap(),
                existingSkyRenderer.constantAmbientLight());

        this.existingSkyRenderer = existingSkyRenderer;
    }

    public DimensionSpecialEffects getExistingSkyRenderer() {
        return this.existingSkyRenderer;
    }

    @Override
    public boolean renderSky(ClientLevel world, int ticks, float partialTicks, PoseStack renderStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        if (this.renderingVanillaSky) {
            //Re-entrant call coming out of renderVanillaSky(..): report "not handled" exactly once so that
            //LevelRenderer runs its own sky body, mirroring 1.16.5's temporarily nulled sky render handler.
            return false;
        }

        //1.16.5 gated the INSTALL on this whitelist (client/event/SkyRenderEventHandler.java:37): the chaining
        //handler was only swapped into a dimension whose id is listed, so an unlisted dimension simply kept its
        //original handler. In 1.20.1 the install is a single up-front registration over every dimension-effects
        //entry, so the whitelist has to be enforced here instead - otherwise it would be ignored entirely and the
        //astral sky would render in every NORMAL-sky dimension. Delegating makes this wrapper fully transparent
        //for an unlisted dimension, which is exactly what "was never installed" meant in 1.16.5.
        if (!RenderingConfig.CONFIG.dimensionsWithSkyRendering.get().contains(world.dimension().location().toString())) {
            return existingSkyRenderer.renderSky(world, ticks, partialTicks, renderStack, camera, projectionMatrix, isFoggy, setupFog);
        }

        EventFlags.SKY_RENDERING.executeWithFlag(() -> {
            //1.16.5: world.getDimensionKey() -> ClientLevel#dimension(), RegistryKey -> ResourceKey
            ResourceKey<Level> dim = world.dimension();
            //1.16.5: world.func_239132_a_().func_241683_c_() == DimensionRenderInfo.FogType.NORMAL
            //     -> world.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL
            if (world.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
                //VERIFY_PORT[AS-SKY-04]: kept verbatim - dimensionsWithOnlyConstellationRendering is a
                //List<? extends String> config value, so testing it against a ResourceLocation (List#contains takes
                //Object) can never match and the constellation-only branch is dead in 1.16.5 as well. Reproduced
                //instead of silently "fixed" because comparing dim.location().toString() would enable a code path
                //that never ran upstream. (1.16.5 client/sky/ChainingSkyRenderer.java:45)
                if (RenderingConfig.CONFIG.dimensionsWithOnlyConstellationRendering.get().contains(dim.location())) {
                    //1.16.5 asked whether the ISkyRenderHandler it had replaced was non-null, letting it draw, and
                    //otherwise rendered the vanilla sky. The 1.20.1 analogue of "there is no other custom handler"
                    //is the wrapped DimensionSpecialEffects returning false from renderSky(..) - Forge's default
                    //implementation, inherited by every vanilla effects instance - so both reference branches fold
                    //into the return value of the delegate call.
                    if (!existingSkyRenderer.renderSky(world, ticks, partialTicks, renderStack, camera, projectionMatrix, isFoggy, setupFog)) {
                        this.renderVanillaSky(renderStack, projectionMatrix, partialTicks, camera, isFoggy, setupFog);
                    }

                    this.renderConstellations(world, renderStack, partialTicks);
                } else {
                    //1.16.5 received the Minecraft instance as a handler parameter; Forge always passed
                    //Minecraft.getInstance(), which the new hook no longer hands out.
                    AstralSkyRenderer.INSTANCE.render(ticks, partialTicks, renderStack, world, Minecraft.getInstance(), setupFog);
                }
            } else {
                //Actually ends up calling renderEndSky
                this.renderVanillaSky(renderStack, projectionMatrix, partialTicks, camera, isFoggy, setupFog);
            }
        });
        return true;
    }

    /**
     * 1.16.5 read the currently installed {@code ISkyRenderHandler}, set it to {@code null}, called
     * {@code WorldRenderer#renderSky(MatrixStack, float)} and put the handler back. Forge 1.20.1 asks
     * {@code level.effects().renderSky(..)} from inside {@code LevelRenderer#renderSky(..)} and runs the vanilla body
     * whenever that returns {@code false}, so the unhook/render/rehook dance becomes flag/re-enter/return-false.
     */
    private void renderVanillaSky(PoseStack renderStack, Matrix4f projectionMatrix, float partialTicks, Camera camera, boolean isFoggy, Runnable setupFog) {
        this.renderingVanillaSky = true;
        try {
            Minecraft.getInstance().levelRenderer.renderSky(renderStack, projectionMatrix, partialTicks, camera, isFoggy, setupFog);
        } finally {
            this.renderingVanillaSky = false;
        }
    }

    //TODO_PORT[AS-SKY-03]: RenderSystem.disableAlphaTest()/enableAlphaTest() (1.16.5
    //client/sky/ChainingSkyRenderer.java:70, 88) and RenderSystem.enableTexture()/disableTexture() (:73, 85) were
    //removed by the 1.17 core-profile rewrite and have no 1.20.1 equivalent - the alpha test is baked into the core
    //shaders and textures are bound per shader sampler slot. Dropped rather than emulated.
    private void renderConstellations(ClientLevel world, PoseStack renderStack, float pTicks) {
        RenderSystem.enableBlend();
        Blending.ADDITIVE_ALPHA.apply();
        RenderSystem.depthMask(false);
        //1.16.5: world.getRainStrength(float) -> Level#getRainLevel(float)
        float alphaSubRain = 1.0F - world.getRainLevel(pTicks);
        //1.16.5: RenderSystem.color4f(..) -> RenderSystem.setShaderColor(..)
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alphaSubRain);

        renderStack.pushPose();
        //1.16.5: MatrixStack#rotate(Vector3f.XP.rotationDegrees(..)) -> PoseStack#mulPose(Axis.XP.rotationDegrees(..))
        renderStack.mulPose(Axis.XP.rotationDegrees(180));
        AstralSkyRenderer.renderConstellationsSky(world, renderStack, pTicks);
        renderStack.popPose();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.depthMask(true);
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    //=================================================================================================
    // Pure delegation to the wrapped DimensionSpecialEffects - not present in 1.16.5, where this class was a
    // standalone ISkyRenderHandler and the DimensionRenderInfo it was attached to stayed untouched.
    //=================================================================================================

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return this.existingSkyRenderer.getBrightnessDependentFogColor(fogColor, brightness);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return this.existingSkyRenderer.isFoggyAt(x, y);
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return this.existingSkyRenderer.getSunriseColor(timeOfDay, partialTicks);
    }

    @Override
    public boolean renderClouds(ClientLevel world, int ticks, float partialTicks, PoseStack renderStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return this.existingSkyRenderer.renderClouds(world, ticks, partialTicks, renderStack, camX, camY, camZ, projectionMatrix);
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel world, int ticks, float partialTicks, LightTexture lightTexture, double camX, double camY, double camZ) {
        return this.existingSkyRenderer.renderSnowAndRain(world, ticks, partialTicks, lightTexture, camX, camY, camZ);
    }

    @Override
    public boolean tickRain(ClientLevel world, int ticks, Camera camera) {
        return this.existingSkyRenderer.tickRain(world, ticks, camera);
    }

    @Override
    public void adjustLightmapColors(ClientLevel world, float partialTicks, float skyDarken, float skyFlashTime, float blockLightRedFlicker, int pixelX, int pixelY, Vector3f colors) {
        this.existingSkyRenderer.adjustLightmapColors(world, partialTicks, skyDarken, skyFlashTime, blockLightRedFlicker, pixelX, pixelY, colors);
    }
}
