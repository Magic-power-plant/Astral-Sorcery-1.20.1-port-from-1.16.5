/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderStateUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderStateBuilder
 * Created by HellFirePvP
 * Date: 05.06.2020 / 16:27
 *
 * 1.16.5 -&gt; 1.20.1: {@code RenderState} was renamed to {@link RenderStateShard} and all of its inner
 * {@code *State} classes gained a {@code *StateShard} suffix; {@code RenderType.State(.Builder)} became
 * {@link RenderType.CompositeState}({@code .CompositeStateBuilder}) and its setters were renamed from
 * {@code texture(..)}-style to {@code setTextureState(..)}-style. {@code State.getBuilder()} is now
 * {@code CompositeState.builder()} and {@code build(boolean)} is now {@code createCompositeState(boolean)}.
 */
public class RenderStateBuilder {

    private final RenderType.CompositeState.CompositeStateBuilder builder;

    private RenderStateBuilder(RenderType.CompositeState.CompositeStateBuilder builder) {
        this.builder = builder;
    }

    public static RenderStateBuilder builder() {
        return new RenderStateBuilder(RenderType.CompositeState.builder());
    }

    public RenderStateBuilder texture(AbstractRenderableTexture texture) {
        this.builder.setTextureState(texture.asState());
        return this;
    }

    public RenderStateBuilder altasTexture() {
        this.builder.setTextureState(BlockAtlasTexture.getInstance().asState());
        return this;
    }

    public RenderStateBuilder disableTexture() {
        //1.16.5 used the no-arg 'new RenderState.TextureState()' which was public. The equivalent
        //1.20.1 class is EmptyTextureStateShard, whose no-arg constructor is package-private; the public
        //(Runnable, Runnable) constructor with two no-op runnables is byte-for-byte what vanilla's
        //RenderStateShard.NO_TEXTURE does.
        this.builder.setTextureState(new RenderStateShard.EmptyTextureStateShard(() -> {}, () -> {}));
        return this;
    }

    public RenderStateBuilder blend(Blending blendMode) {
        this.builder.setTransparencyState(blendMode.asState());
        return this;
    }

    public RenderStateBuilder smoothShade() {
        //TODO_PORT[AS-RSB-01]: no equivalent for RenderState.ShadeModelState(true) (1.16.5 client/render/RenderStateBuilder.java:60)
        //1.20.1 removed the fixed-function shade model entirely (GL core profile); smooth shading is now
        //implicit because every core shader interpolates the per-vertex colour attribute. Calls are kept so
        //the RegistryRenderTypes call graph stays identical to the reference.
        return this;
    }

    public RenderStateBuilder enableItemRendering() {
        //TODO_PORT[AS-RSB-02]: no equivalent for RenderState.DiffuseLightingState(true) (1.16.5 client/render/RenderStateBuilder.java:66)
        //1.20.1 removed DiffuseLightingState; diffuse lighting is applied inside the rendertype_entity_*
        //core shaders instead of via a toggleable render state.
        return this;
    }

    public RenderStateBuilder disableDepth() {
        this.builder.setDepthTestState(new RenderStateShard.DepthTestStateShard("always", GL11.GL_ALWAYS) {
            @Override
            public void setupRenderState() {
                //For some ungodly reason this might not be reset to disable depth testing by default...
                //I guess we're gonna do it manually then.
                RenderSystem.disableDepthTest();
                super.setupRenderState();
            }
        });
        return this;
    }

    public RenderStateBuilder disableDepthMask() {
        this.builder.setWriteMaskState(new RenderStateUtil.WriteMaskState(true, false));
        return this;
    }

    public RenderStateBuilder enableLighting() {
        this.builder.setLightmapState(new RenderStateShard.LightmapStateShard(true));
        return this;
    }

    public RenderStateBuilder enableDiffuseLighting() {
        //TODO_PORT[AS-RSB-02]: no equivalent for RenderState.DiffuseLightingState(true) (1.16.5 client/render/RenderStateBuilder.java:94)
        //See enableItemRendering() above - same removed vanilla state.
        return this;
    }

    public RenderStateBuilder enableOverlay() {
        this.builder.setOverlayState(new RenderStateShard.OverlayStateShard(true));
        return this;
    }

    public RenderStateBuilder disableCull() {
        this.builder.setCullState(new RenderStateUtil.CullState(false));
        return this;
    }

    public RenderStateBuilder alpha(float alphaThreshold) {
        //TODO_PORT[AS-RSB-03]: no equivalent for RenderState.AlphaState(alphaThreshold) (1.16.5 client/render/RenderStateBuilder.java:109)
        //1.20.1 dropped the fixed-function alpha test along with AlphaState; discarding low-alpha fragments
        //is now hardcoded per core shader (the 'cutout' shaders discard below 0.1). The 1F/255F threshold
        //used by every AS type here only rejected fully-transparent fragments, which the blend func already
        //makes invisible, so omitting it does not change the rendered result.
        return this;
    }

    public RenderStateBuilder defaultAlpha() {
        return alpha(1F / 255F);
    }

    /**
     * Not present in 1.16.5: in 1.20.1 a RenderType without a ShaderStateShard draws nothing at all, as
     * RenderSystem has no fixed-function fallback left. Every type created through this builder therefore has
     * to name the core shader matching its vertex format explicitly.
     */
    public RenderStateBuilder shader(Supplier<ShaderInstance> shader) {
        this.builder.setShaderState(new RenderStateShard.ShaderStateShard(shader));
        return this;
    }

    public RenderStateBuilder particleShaderTarget() {
        this.builder.setOutputState(ParticleTarget.INSTANCE);
        return this;
    }

    public RenderType.CompositeState.CompositeStateBuilder vanillaBuilder() {
        return this.builder;
    }

    public RenderType.CompositeState buildAsOverlay() {
        return this.builder.createCompositeState(true);
    }

    public RenderType.CompositeState build() {
        return this.builder.createCompositeState(false);
    }

    private static class ParticleTarget extends RenderStateShard.OutputStateShard {

        private static final ParticleTarget INSTANCE = new ParticleTarget();

        private ParticleTarget() {
            //1.16.5 -> 1.20.1: Minecraft.isFabulousGraphicsEnabled() -> Minecraft.useShaderTransparency(),
            //WorldRenderer.func_239230_s_() -> LevelRenderer.getTranslucentTarget(),
            //Minecraft.getFramebuffer() -> Minecraft.getMainRenderTarget(),
            //Framebuffer.bindFramebuffer(boolean) -> RenderTarget.bindWrite(boolean).
            super("as_particle_target", () -> {
                if (Minecraft.useShaderTransparency()) {
                    Minecraft.getInstance().levelRenderer.getTranslucentTarget().bindWrite(false);
                }
            }, () -> {
                if (Minecraft.useShaderTransparency()) {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                }
            });
        }
    }
}
