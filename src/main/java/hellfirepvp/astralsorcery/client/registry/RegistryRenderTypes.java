/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.constellation.ConstellationRenderInfos;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.render.RenderStateBuilder;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.image.SkyImageGenerator;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

import static hellfirepvp.astralsorcery.client.lib.RenderTypesAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRenderTypes
 * Created by HellFirePvP
 * Date: 05.06.2020 / 15:59
 *
 * 1.16.5 -&gt; 1.20.1 notes that apply to this whole file:
 * - {@code DefaultVertexFormats} -&gt; {@link DefaultVertexFormat}, {@code DefaultVertexFormats.ENTITY} -&gt;
 *   {@code DefaultVertexFormat.NEW_ENTITY} (same element layout).
 * - {@code RenderType.makeType(name, format, glDrawMode, size, useDelegate, needsSorting, state)} -&gt;
 *   {@code RenderType.create(name, format, VertexFormat.Mode, size, affectsCrumbling, sortOnUpload, state)};
 *   the raw GL draw mode constants were replaced by {@link VertexFormat.Mode}, translated through
 *   {@link RenderingUtils#toDrawMode(int)} so the reference call shapes stay byte-identical.
 * - Every type now has to name the core shader matching its vertex format explicitly: without a
 *   ShaderStateShard a 1.20.1 RenderType uploads its buffer with no program bound and draws nothing.
 *   The pairings used below are the ones vanilla itself uses for the respective format.
 */
public class RegistryRenderTypes {

    public static void init() {
        initEffectTypes();
        initEffects();
        initConstellationTypes();
        initGuiTypes();
        initTERTypes();
        initModels();
    }

    private static void initEffectTypes() {
        EFFECT_FX_GENERIC_PARTICLE = createType("effect_fx_generic_particle", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .texture(TexturesAS.TEX_PARTICLE_SMALL)
                        .blend(Blending.DEFAULT)
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_GENERIC_PARTICLE_DEPTH = createType("effect_fx_generic_particle_depth", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .texture(TexturesAS.TEX_PARTICLE_SMALL)
                        .blend(Blending.DEFAULT)
                        .disableCull()
                        .disableDepthMask()
                        .disableDepth()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_GENERIC_PARTICLE_ATLAS = createType("effect_fx_generic_particle_atlas", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .altasTexture()
                        .blend(Blending.DEFAULT)
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_LIGHTNING = createType("effect_fx_lightning", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .texture(TexturesAS.TEX_LIGHTNING_PART)
                        .blend(Blending.DEFAULT)
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_LIGHTBEAM = createType("effect_fx_lightbeam", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .texture(TexturesAS.TEX_LIGHTBEAM)
                        .blend(Blending.ADDITIVE_ALPHA)
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        //POSITION_COLOR_TEX_NORMAL appends a normal to POSITION_COLOR_TEX; position/color/uv0 keep the exact
        //element indices the position_color_tex program binds, so the trailing (unread) normal attribute is
        //harmless. See RenderTypesAS.POSITION_COLOR_TEX_NORMAL.
        EFFECT_FX_CRYSTAL = createType("effect_fx_crystal", POSITION_COLOR_TEX_NORMAL, GL11.GL_TRIANGLES, 32768,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .texture(TexturesAS.TEX_MODEL_CRYSTAL_WHITE)
                        .blend(Blending.DEFAULT)
                        .defaultAlpha()
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_BURST = createType("effect_fx_burst", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .altasTexture()
                        .blend(Blending.DEFAULT)
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_DYNAMIC_TEXTURE_SPRITE = createType("effect_fx_dynamic_texture_sprite", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .altasTexture()
                        .blend(Blending.DEFAULT)
                        .alpha(0.0001F)
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_TEXTURE_SPRITE = createType("effect_fx_texture_sprite", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .altasTexture()
                        .blend(Blending.DEFAULT)
                        .alpha(0.0001F)
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_CUBE_OPAQUE_ATLAS = createType("effect_fx_cube_opaque_atlas", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexLightmapShader)
                        .altasTexture()
                        .blend(Blending.DEFAULT)
                        .defaultAlpha()
                        .disableCull()
                        .enableLighting()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_BLOCK_TRANSLUCENT = createType("effect_fx_block_translucent", DefaultVertexFormat.BLOCK,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeTranslucentShader)
                        .altasTexture()
                        .blend(Blending.ADDITIVEDARK)
                        .defaultAlpha()
                        .disableCull()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_BLOCK_TRANSLUCENT_DEPTH = createType("effect_fx_block_translucent_depth", DefaultVertexFormat.BLOCK,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeTranslucentShader)
                        .altasTexture()
                        .blend(Blending.ADDITIVEDARK)
                        .defaultAlpha()
                        .disableCull()
                        .disableDepth()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_CUBE_TRANSLUCENT_ATLAS = createType("effect_fx_cube_translucent_atlas", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexLightmapShader)
                        .altasTexture()
                        .blend(Blending.ADDITIVEDARK)
                        .defaultAlpha()
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_CUBE_TRANSLUCENT_ATLAS_DEPTH = createType("effect_fx_cube_translucent_atlas_depth", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexLightmapShader)
                        .altasTexture()
                        .blend(Blending.ADDITIVEDARK)
                        .defaultAlpha()
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .disableDepth()
                        .build());
        EFFECT_FX_CUBE_AREA_OF_EFFECT = createType("effect_fx_cube_area_of_effect", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexLightmapShader)
                        .texture(TexturesAS.TEX_AREA_OF_EFFECT_CUBE)
                        .blend(Blending.DEFAULT)
                        .defaultAlpha()
                        .disableCull()
                        .disableDepthMask()
                        .particleShaderTarget()
                        .build());
        EFFECT_FX_COLOR_SPHERE = createType("effect_fx_color_sphere", DefaultVertexFormat.POSITION_COLOR, GL11.GL_TRIANGLES, 32768,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorShader)
                        .blend(Blending.DEFAULT)
                        .disableTexture()
                        .alpha(0.00001F)
                        .particleShaderTarget()
                        .build());
    }

    private static void initEffects() {
        EFFECT_LIGHTRAY_FAN = createType("effect_lightray_fan", DefaultVertexFormat.POSITION_COLOR, GL11.GL_TRIANGLE_FAN, 32768,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorShader)
                        .blend(Blending.ADDITIVE_ALPHA)
                        .smoothShade()
                        .disableDepthMask()
                        .enableDiffuseLighting()
                        .build());

        CONSTELLATION_WORLD_STAR = createType("effect_render_cst_star", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .texture(TexturesAS.TEX_STAR_1)
                        .blend(Blending.DEFAULT)
                        .disableDepthMask()
                        .build());

        CONSTELLATION_WORLD_CONNECTION = createType("effect_render_cst_connection", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .texture(TexturesAS.TEX_STAR_CONNECTION)
                        .blend(Blending.DEFAULT)
                        .disableDepthMask()
                        .build());
    }

    private static void initConstellationTypes() {
        CONSTELLATION_DISCIDIA_BACKGROUND   = createConstellationBackgroundType(ConstellationsAS.discidia, TexturesAS.TEX_DISCIDIA_BACKGROUND);
        CONSTELLATION_ARMARA_BACKGROUND     = createConstellationBackgroundType(ConstellationsAS.armara, TexturesAS.TEX_ARMARA_BACKGROUND);
        CONSTELLATION_VICIO_BACKGROUND      = createConstellationBackgroundType(ConstellationsAS.vicio, TexturesAS.TEX_VICIO_BACKGROUND);
        CONSTELLATION_AEVITAS_BACKGROUND    = createConstellationBackgroundType(ConstellationsAS.aevitas, TexturesAS.TEX_AEVITAS_BACKGROUND);
        CONSTELLATION_EVORSIO_BACKGROUND    = createConstellationBackgroundType(ConstellationsAS.evorsio, TexturesAS.TEX_EVORSIO_BACKGROUND);
        CONSTELLATION_LUCERNA_BACKGROUND    = createConstellationBackgroundType(ConstellationsAS.lucerna, TexturesAS.TEX_LUCERNA_BACKGROUND);
        CONSTELLATION_MINERALIS_BACKGROUND  = createConstellationBackgroundType(ConstellationsAS.mineralis, TexturesAS.TEX_MINERALIS_BACKGROUND);
        CONSTELLATION_HOROLOGIUM_BACKGROUND = createConstellationBackgroundType(ConstellationsAS.horologium, TexturesAS.TEX_HOROLOGIUM_BACKGROUND);
        CONSTELLATION_OCTANS_BACKGROUND     = createConstellationBackgroundType(ConstellationsAS.octans, TexturesAS.TEX_OCTANS_BACKGROUND);
        CONSTELLATION_BOOTES_BACKGROUND     = createConstellationBackgroundType(ConstellationsAS.bootes, TexturesAS.TEX_BOOTES_BACKGROUND);
        CONSTELLATION_FORNAX_BACKGROUND     = createConstellationBackgroundType(ConstellationsAS.fornax, TexturesAS.TEX_FORNAX_BACKGROUND);
        CONSTELLATION_PELOTRIO_BACKGROUND   = createConstellationBackgroundType(ConstellationsAS.pelotrio, TexturesAS.TEX_PELOTRIO_BACKGROUND);
        CONSTELLATION_GELU_BACKGROUND       = createConstellationBackgroundType(ConstellationsAS.gelu, TexturesAS.TEX_GELU_BACKGROUND);
        CONSTELLATION_ULTERIA_BACKGROUND    = createConstellationBackgroundType(ConstellationsAS.ulteria, TexturesAS.TEX_ULTERIA_BACKGROUND);
        CONSTELLATION_ALCARA_BACKGROUND     = createConstellationBackgroundType(ConstellationsAS.alcara, TexturesAS.TEX_ALCARA_BACKGROUND);
        CONSTELLATION_VORUX_BACKGROUND      = createConstellationBackgroundType(ConstellationsAS.vorux, TexturesAS.TEX_VORUX_BACKGROUND);
    }

    private static void initGuiTypes() {
        GUI_MISC_INFO_STAR = createType("gui_misc_info_star", DefaultVertexFormat.POSITION_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionTexShader)
                        .texture(TexturesAS.TEX_STAR_1)
                        .blend(Blending.DEFAULT)
                        .defaultAlpha()
                        .build());
    }

    private static void initTERTypes() {
        TER_WELL_LIQUID = createType("ter_well_liquid", DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .altasTexture()
                        .blend(Blending.DEFAULT)
                        .alpha(0.00001F)
                        .disableDepthMask()
                        .build());
        TER_CHALICE_LIQUID = createType("ter_chalice_liquid", POSITION_COLOR_TEX_NORMAL,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .altasTexture()
                        .blend(Blending.DEFAULT)
                        .alpha(0.00001F)
                        .disableDepthMask()
                        .build());
    }

    private static void initModels() {
        MODEL_ATTUNEMENT_ALTAR = createType("model_attunement_altar", DefaultVertexFormat.NEW_ENTITY,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeEntitySolidShader)
                        .texture(AssetLibrary.loadTexture(AssetLoader.TextureLocation.BLOCKS, "entity", "attunement_altar"))
                        .enableLighting()
                        .enableDiffuseLighting()
                        .enableOverlay()
                        .build());

        MODEL_LENS_SOLID = createType("model_lens", DefaultVertexFormat.NEW_ENTITY,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeEntitySolidShader)
                        .texture(AssetLibrary.loadTexture(AssetLoader.TextureLocation.BLOCKS, "entity", "lens_frame"))
                        .enableLighting()
                        .enableDiffuseLighting()
                        .enableOverlay()
                        .build());

        MODEL_LENS_GLASS = createType("model_lens_glass", DefaultVertexFormat.NEW_ENTITY,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeEntityTranslucentShader)
                        .texture(AssetLibrary.loadTexture(AssetLoader.TextureLocation.BLOCKS, "entity", "lens_frame"))
                        .blend(Blending.DEFAULT)
                        .disableDepthMask()
                        .enableLighting()
                        .enableDiffuseLighting()
                        .enableOverlay()
                        .build());

        MODEL_LENS_COLORED_SOLID = createType("model_lens_colored", DefaultVertexFormat.NEW_ENTITY,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeEntitySolidShader)
                        .texture(AssetLibrary.loadTexture(AssetLoader.TextureLocation.BLOCKS, "entity", "lens_color"))
                        .enableLighting()
                        .enableDiffuseLighting()
                        .enableOverlay()
                        .build());

        MODEL_LENS_COLORED_GLASS = createType("model_lens_colored_glass", DefaultVertexFormat.NEW_ENTITY,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeEntityTranslucentShader)
                        .texture(AssetLibrary.loadTexture(AssetLoader.TextureLocation.BLOCKS, "entity", "lens_color"))
                        .blend(Blending.DEFAULT)
                        .disableDepthMask()
                        .enableLighting()
                        .enableDiffuseLighting()
                        .enableOverlay()
                        .build());

        MODEL_OBSERVATORY = createType("model_observatory", DefaultVertexFormat.NEW_ENTITY,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeEntityTranslucentShader)
                        .texture(AssetLibrary.loadTexture(AssetLoader.TextureLocation.BLOCKS, "entity", "observatory"))
                        .blend(Blending.DEFAULT)
                        .disableCull()
                        .enableLighting()
                        .enableDiffuseLighting()
                        .enableOverlay()
                        .build());

        MODEL_REFRACTION_TABLE = createType("model_refraction_table", DefaultVertexFormat.NEW_ENTITY,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeEntitySolidShader)
                        .texture(AssetLibrary.loadTexture(AssetLoader.TextureLocation.BLOCKS, "entity", "refraction_table"))
                        .enableLighting()
                        .enableDiffuseLighting()
                        .enableOverlay()
                        .build());

        MODEL_REFRACTION_TABLE_GLASS = createType("model_refraction_table_glass", DefaultVertexFormat.NEW_ENTITY,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeEntityTranslucentShader)
                        .texture(AssetLibrary.loadTexture(AssetLoader.TextureLocation.BLOCKS, "entity", "refraction_table"))
                        .blend(Blending.DEFAULT)
                        .disableDepthMask()
                        .enableLighting()
                        .enableDiffuseLighting()
                        .enableOverlay()
                        .build());

        MODEL_TELESCOPE = createType("model_telescope", DefaultVertexFormat.NEW_ENTITY,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getRendertypeEntityTranslucentShader)
                        .texture(AssetLibrary.loadTexture(AssetLoader.TextureLocation.BLOCKS, "entity", "telescope"))
                        .blend(Blending.DEFAULT)
                        .disableCull()
                        .enableLighting()
                        .enableDiffuseLighting()
                        .enableOverlay()
                        .build());

        //The three wing models keep the reference's missing texture state (MODEL_DEMON_WINGS and
        //MODEL_WRAITH_WINGS never set one): NO_TEXTURE leaves whatever the caller bound in place, which is
        //exactly what 1.16.5 did.
        MODEL_DEMON_WINGS = createType("model_demon_wings", POSITION_COLOR_TEX_NORMAL,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .enableLighting()
                        .enableDiffuseLighting()
                        .smoothShade()
                        .build());

        MODEL_CELESTIAL_WINGS = createType("model_celestial_wings", POSITION_COLOR_TEX_NORMAL,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .texture(TexturesAS.TEX_MODEL_CELESTIAL_WINGS)
                        .enableDiffuseLighting()
                        .smoothShade()
                        .build());

        MODEL_WRAITH_WINGS = createType("model_wraith_wings", POSITION_COLOR_TEX_NORMAL,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .enableLighting()
                        .enableDiffuseLighting()
                        .smoothShade()
                        .build());
    }

    public static RenderType createDepthProjectionType(int zoom) {
        return createType("player_starry_sky_layer", DefaultVertexFormat.POSITION_COLOR, GL11.GL_QUADS, 256, false, true,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorShader)
                        .blend(Blending.ADDITIVE)
                        .texture(AssetLibrary.loadGeneratedResource(AstralSorcery.key("player_starry_sky_layer"), SkyImageGenerator::generateStarBackground, true))
                        .alpha(0.001F)
                        .vanillaBuilder()
                        .setTexturingState(new IdentityProjectionModelTexturingState(zoom))
                        .createCompositeState(false));
    }

    private static RenderType createType(String name, VertexFormat vertexFormat, RenderType.CompositeState state) {
        return createType(name, vertexFormat, GL11.GL_QUADS, 32768, state);
    }

    private static RenderType createType(String name, VertexFormat vertexFormat, int glDrawMode, int bufferSize, RenderType.CompositeState state) {
        return createType(name, vertexFormat, glDrawMode, bufferSize, false, false, state);
    }

    private static RenderType createType(String name, VertexFormat vertexFormat, int glDrawMode, int bufferSize, boolean usesDelegateDrawing, boolean sortVertices, RenderType.CompositeState state) {
        return RenderType.create(AstralSorcery.key(name).toString(), vertexFormat, RenderingUtils.toDrawMode(glDrawMode), bufferSize, usesDelegateDrawing, sortVertices, state);
    }

    private static RenderType createConstellationBackgroundType(IConstellation cst, AbstractRenderableTexture tex) {
        RenderType rType = createType("constellation_background_" + cst.getSimpleName(), DefaultVertexFormat.POSITION_COLOR_TEX,
                RenderStateBuilder.builder()
                        .shader(GameRenderer::getPositionColorTexShader)
                        .texture(tex)
                        .blend(Blending.DEFAULT)
                        .disableDepthMask()
                        .build());

        ConstellationRenderInfos.registerBackground(cst, rType, tex);
        return rType;
    }

    private static class IdentityProjectionModelTexturingState extends RenderStateShard.TexturingStateShard {

        private final int zoom;

        public IdentityProjectionModelTexturingState(int zoom) {
            //BLOCKED_PORT[AS-RTY-01]: the animated texture-matrix/tex-gen setup of the 1.16.5 state is gone
            //(1.16.5 client/registry/RegistryRenderTypes.java:404-423). Every call it made -
            //RenderSystem.matrixMode / pushMatrix / popMatrix / loadIdentity / translatef / scalef / rotatef /
            //mulTextureByProjModelView / setupEndPortalTexGen / clearTexGen - was deleted in the 1.17 core
            //profile rewrite; fixed-function texture matrices and GL_TEXTURE_GEN do not exist any more.
            //1.20.1 does the equivalent inside a dedicated core shader (see vanilla rendertype_end_portal,
            //which is why RenderType.endPortal() no longer carries a TexturingState at all), so restoring this
            //effect needs an astralsorcery core shader + a ShaderStateShard, not a render state.
            //The zoom-keyed identity/equality of the state is kept so the RENDER_TYPES cache in
            //client/render/entity/layer/StarryLayerRenderer still produces one distinct type per zoom level.
            super(AstralSorcery.key("depth_projection_texturing_" + zoom).toString(), () -> {}, () -> {});
            this.zoom = zoom;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            IdentityProjectionModelTexturingState that = (IdentityProjectionModelTexturingState) o;
            return zoom == that.zoom;
        }

        @Override
        public int hashCode() {
            return zoom;
        }
    }
}
