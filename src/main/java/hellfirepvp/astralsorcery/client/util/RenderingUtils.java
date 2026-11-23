/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mojang.math.MatrixUtil;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.effect.EntityComplexFX;
import hellfirepvp.astralsorcery.client.util.draw.BufferDecoratorBuilder;
import hellfirepvp.astralsorcery.client.util.draw.RenderTypeDecorator;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderingUtils
 * Created by HellFirePvP
 * Date: 27.05.2019 / 22:26
 */
public class RenderingUtils {

    private static final RandomSource rand = RandomSource.create();
    private static BlockAndTintGetter plainRenderWorld = null;

    // 1.20.1: ItemEntity#age is private and ItemEntity#bobOffs is final; both were directly assignable in 1.16.5.
    @Nullable
    private static Field itemEntityAge = null;
    @Nullable
    private static Field itemEntityBobOffs = null;
    private static boolean itemEntityFieldsResolved = false;

    public static long getPositionSeed(BlockPos pos) {
        long seed = 1553015L;
        seed ^= pos.getX();
        seed ^= pos.getY();
        seed ^= pos.getZ();
        return seed;
    }

    @Nullable
    public static TextureAtlasSprite getParticleTexture(FluidStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ResourceLocation res = IClientFluidTypeExtensions.of(stack.getFluid()).getStillTexture(stack);
        if (res == null || MissingTextureAtlasSprite.getLocation().equals(res)) {
            return null;
        }
        return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(res);
    }

    @Nullable
    public static TextureAtlasSprite getParticleTexture(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ItemModelShaper imm = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
        BakedModel mdl = imm.getItemModel(stack);
        if (mdl.equals(imm.getModelManager().getMissingModel())) {
            return null;
        }
        return mdl.getParticleIcon(ModelData.EMPTY);
    }

    @Nullable
    public static TextureAtlasSprite getParticleTexture(BlockState state, @Nullable BlockPos positionHint) {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return null;
        }
        BlockPos pos = positionHint != null ? positionHint : BlockPos.ZERO;
        try {
            if (state.isAir()) {
                return null;
            }
        } catch (Exception exc) {
            return null;
        }
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getTexture(state, world, pos);
    }

    //Straight up ripped off of MC code.
    public static void playBlockBreakParticles(BlockPos pos, @Nullable BlockState actualState, BlockState particleState) {
        ClientLevel world = Minecraft.getInstance().level;
        ParticleEngine mgr = Minecraft.getInstance().particleEngine;

        VoxelShape voxelshape;
        try {
            voxelshape = actualState == null ? Shapes.block() : actualState.getShape(world, pos);
        } catch (Exception exc) {
            voxelshape = Shapes.block();
        }
        voxelshape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double xDist = Math.min(1, maxX - minX);
            double yDist = Math.min(1, maxY - minY);
            double zDist = Math.min(1, maxZ - minZ);
            double i = Math.max(2, Mth.ceil(xDist / 0.25D));
            double j = Math.max(2, Mth.ceil(yDist / 0.25D));
            double k = Math.max(2, Mth.ceil(zDist / 0.25D));

            for (int xx = 0; xx < i; ++xx) {
                for (int yy = 0; yy < j; ++yy) {
                    for (int zz = 0; zz < k; ++zz) {

                        double d4 = (xx + 0.5D) / i;
                        double d5 = (yy + 0.5D) / j;
                        double d6 = (zz + 0.5D) / k;
                        double d7 = d4 * xDist + minX;
                        double d8 = d5 * yDist + minY;
                        double d9 = d6 * zDist + minZ;

                        // 1.20.1: TerrainParticle's (state, pos) constructor already performs what 1.16.5 did
                        // through DiggingParticle#init() + DiggingParticle#setBlockPos(pos)
                        TerrainParticle p = new TerrainParticle(world,
                                pos.getX() + d7, pos.getY() + d8, pos.getZ() + d9,
                                d4 - 0.5D, d5 - 0.5D, d6 - 0.5D,
                                particleState, pos);
                        mgr.add(p);
                    }
                }
            }

        });
    }

    public static Color clampToColor(int rgb) {
        return clampToColorWithMultiplier(rgb, 1F);
    }

    public static Color clampToColorWithMultiplier(int rgb, float mul) {
        int r = ((rgb >> 16) & 0xFF);
        int g = ((rgb >> 8)  & 0xFF);
        int b = ((rgb >> 0)  & 0xFF);
        return new Color(
                Mth.clamp((int) (((float) r) * mul), 0, 255),
                Mth.clamp((int) (((float) g) * mul), 0, 255),
                Mth.clamp((int) (((float) b) * mul), 0, 255));
    }

    public static Color clampToColor(int r, int g, int b) {
        return new Color(
                Mth.clamp((int) (((float) r)), 0, 255),
                Mth.clamp((int) (((float) g)), 0, 255),
                Mth.clamp((int) (((float) b)), 0, 255));
    }

    public static boolean canEffectExist(EntityComplexFX fx) {
        Entity view = Minecraft.getInstance().getCameraEntity();
        if (view == null) {
            view = Minecraft.getInstance().player;
        }
        if (view == null) {
            return false;
        }
        return fx.getPosition().distanceSquared(view) <= RenderingConfig.CONFIG.getMaxEffectRenderDistanceSq();
    }

    public static void translate(PoseStack renderStack, float x, float y, float z, Consumer<PoseStack> fn) {
        renderStack.pushPose();
        renderStack.translate(x, y, z);
        fn.accept(renderStack);
        renderStack.popPose();
    }

    public static void drawSprite(SpriteSheetResource sprite, Blending blending, Consumer<BufferBuilder> geometry) {
        draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buffer -> {
            sprite.bindForRendering(blending);
            geometry.accept(buffer);
        });
    }

    public static void draw(int drawMode, VertexFormat format, Consumer<BufferBuilder> fn) {
        draw(drawMode, format, bufferBuilder -> {
            fn.accept(bufferBuilder);
            return null;
        });
    }

    public static <R> R draw(int drawMode, VertexFormat format, Function<BufferBuilder, R> fn) {
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        setupShaderFor(format);
        buf.begin(toDrawMode(drawMode), format);
        R result = fn.apply(buf);
        finishDrawing(buf);
        return result;
    }

    /**
     * 1.16.5 passed raw GL draw mode constants around; 1.20.1 replaced them with {@link VertexFormat.Mode}.
     * The enum's {@code asGLMode} values are <i>not</i> a bijection of the legacy constants (GL_QUADS isn't even
     * present anymore), so the legacy mapping has to be spelled out explicitly.
     */
    public static VertexFormat.Mode toDrawMode(int glDrawMode) {
        switch (glDrawMode) {
            case GL11.GL_QUADS:
                return VertexFormat.Mode.QUADS;
            case GL11.GL_TRIANGLES:
                return VertexFormat.Mode.TRIANGLES;
            case GL11.GL_TRIANGLE_STRIP:
                return VertexFormat.Mode.TRIANGLE_STRIP;
            case GL11.GL_TRIANGLE_FAN:
                return VertexFormat.Mode.TRIANGLE_FAN;
            case GL11.GL_LINES:
                return VertexFormat.Mode.DEBUG_LINES;
            case GL11.GL_LINE_STRIP:
                return VertexFormat.Mode.DEBUG_LINE_STRIP;
            default:
                return VertexFormat.Mode.QUADS;
        }
    }

    /**
     * 1.16.5 relied on fixed-function GL state for immediate-mode drawing. 1.20.1 requires a core shader
     * matching the used {@link VertexFormat} to be selected before the buffer is uploaded.
     */
    public static void setupShaderFor(VertexFormat format) {
        if (format == DefaultVertexFormat.POSITION) {
            RenderSystem.setShader(GameRenderer::getPositionShader);
        } else if (format == DefaultVertexFormat.POSITION_COLOR) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        } else if (format == DefaultVertexFormat.POSITION_TEX) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
        } else if (format == DefaultVertexFormat.POSITION_COLOR_TEX) {
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        } else if (format == DefaultVertexFormat.POSITION_TEX_COLOR) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        } else if (format == DefaultVertexFormat.POSITION_COLOR_LIGHTMAP) {
            RenderSystem.setShader(GameRenderer::getPositionColorLightmapShader);
        } else if (format == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP) {
            RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        } else if (format == DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        } else if (format == DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR) {
            RenderSystem.setShader(GameRenderer::getPositionTexLightmapColorShader);
        } else if (format == DefaultVertexFormat.PARTICLE) {
            RenderSystem.setShader(GameRenderer::getParticleShader);
        } else if (format.hasUV(0)) {
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }
    }

    public static void finishDrawing(BufferBuilder buf) {
        finishDrawing(buf, null);
    }

    public static void finishDrawing(BufferBuilder buf, @Nullable RenderType type) {
        if (buf.building()) {
            if (type != null) {
                type.end(buf, VertexSorting.DISTANCE_TO_ORIGIN);
            } else {
                BufferUploader.drawWithShader(buf.end());
            }
        }
    }

    public static void refreshDrawing(VertexConsumer vb, RenderType type) {
        if (vb instanceof BufferBuilder) {
            type.end((BufferBuilder) vb, VertexSorting.DISTANCE_TO_ORIGIN);
            ((BufferBuilder) vb).begin(type.mode(), type.format());
        }
    }

    public static int renderInWorldText(FormattedText text, Color color, Vector3 at, PoseStack renderStack, float pTicks, boolean facePlayer) {
        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        return renderInWorldText(text, color, 0.02F * (Minecraft.getInstance().options.guiScale().get() / scale), at, renderStack, pTicks, facePlayer);
    }

    public static int renderInWorldText(FormattedText text, Color color, float scale, Vector3 at, PoseStack renderStack, float pTicks, boolean facePlayer) {
        Font fr = Minecraft.getInstance().font;

        renderStack.pushPose();
        renderStack.translate(at.getX(), at.getY(), at.getZ());
        renderStack.scale(scale, -scale, scale);

        if (facePlayer) {
            Entity le = Minecraft.getInstance().cameraEntity;
            if (le == null) {
                le = Minecraft.getInstance().player;
            }
            float iYaw = RenderingVectorUtils.interpolate(Mth.wrapDegrees(le.yRotO), Mth.wrapDegrees(le.getYRot()), pTicks);
            renderStack.mulPose(Axis.YP.rotationDegrees(-iYaw + 180F));
        }

        Matrix4f matr = renderStack.last().pose();
        int length = fr.width(text);
        MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        FormattedCharSequence processedText = Language.getInstance().getVisualOrder(text);
        int drawnLength = fr.drawInBatch(processedText, -(length / 2F), 0, color.getRGB(), false, matr, buffers,
                Font.DisplayMode.SEE_THROUGH, 0, LightmapUtil.getPackedFullbrightCoords());
        buffers.endBatch();

        renderStack.popPose();
        return drawnLength;
    }

    public static void renderItemAsEntity(ItemStack stack, PoseStack renderStack, MultiBufferSource buffers, double x, double y, double z, int combinedLight, float pTicks, int age) {
        ItemEntity ei = new ItemEntity(Minecraft.getInstance().level, x, y, z, stack);
        setItemEntityRenderState(ei, age);
        ReflectionHelper.setSkipItemPhysicsRender(ei);
        Minecraft.getInstance().getEntityRenderDispatcher().render(ei, x, y, z, 0F, pTicks, renderStack, buffers, combinedLight);
    }

    /**
     * 1.16.5 assigned {@code ItemEntity#age} and {@code ItemEntity#hoverStart} directly. In 1.20.1 {@code age} is
     * private and {@code bobOffs} (the renamed {@code hoverStart}) is final, so both have to go through reflection.
     */
    private static void setItemEntityRenderState(ItemEntity entity, int age) {
        if (!itemEntityFieldsResolved) {
            itemEntityFieldsResolved = true;
            try {
                itemEntityAge = ObfuscationReflectionHelper.findField(ItemEntity.class, "f_31985_"); //age
            } catch (Exception ignored) {
                itemEntityAge = null;
            }
            try {
                itemEntityBobOffs = ObfuscationReflectionHelper.findField(ItemEntity.class, "f_31983_"); //bobOffs
            } catch (Exception ignored) {
                itemEntityBobOffs = null;
            }
        }
        if (itemEntityAge != null) {
            try {
                itemEntityAge.setInt(entity, age);
            } catch (Exception ignored) {}
        }
        if (itemEntityBobOffs != null) {
            try {
                itemEntityBobOffs.setFloat(entity, 0F);
            } catch (Exception ignored) {}
        }
    }

    public static void renderItemStackGUI(PoseStack renderStack, ItemStack stack, @Nullable String alternativeText) {
        renderStack.pushPose();
        renderStack.translate(0, 0, 100F);
        Font font = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT);
        if (font == null) {
            font = Minecraft.getInstance().font;
        }
        renderTranslucentItemStackModelGUI(stack, renderStack, Color.WHITE, Blending.DEFAULT, 255);
        mcdefault_renderItemOverlayIntoGUI(font, renderStack, stack, Minecraft.getInstance().getFrameTime(), alternativeText);

        renderStack.popPose();
    }

    public static void renderTranslucentItemStack(ItemStack stack, PoseStack renderStack, float pTicks) {
        renderTranslucentItemStack(stack, renderStack, pTicks, Color.WHITE, 25);
    }

    public static void renderTranslucentItemStack(ItemStack stack, PoseStack renderStack, float pTicks, Color overlayColor, int alpha) {
        renderStack.pushPose();

        // EntityItemRenderer entity bobbing
        float sinBobY = Mth.sin((ClientScheduler.getClientTick() + pTicks) / 10.0F) * 0.1F + 0.1F;
        renderStack.translate(0, sinBobY, 0);
        float ageRotate = ((ClientScheduler.getClientTick() + pTicks) / 20.0F);
        renderStack.mulPose(Axis.YP.rotation(ageRotate));

        renderTranslucentItemStackModelGround(stack, renderStack, overlayColor, Blending.PREALPHA, alpha);

        renderStack.popPose();
    }

    public static void renderTranslucentItemStackModelGround(ItemStack stack, PoseStack renderStack, Color overlayColor, Blending blendMode, int alpha) {
        BakedModel bakedModel = getItemModel(stack);
        ForgeHooksClient.handleCameraTransforms(renderStack, bakedModel, ItemDisplayContext.GROUND, false);
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, false);

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderItemModelWithColor(stack, ItemDisplayContext.GROUND, bakedModel, renderStack, (renderType) -> {
            RenderTypeDecorator decorated = RenderTypeDecorator.wrapSetup(renderType, () -> {
                RenderSystem.enableBlend();
                blendMode.apply();
            }, () -> {
                Blending.DEFAULT.apply();
                RenderSystem.disableBlend();
            });
            return buffer.getBuffer(decorated);
        }, LightmapUtil.getPackedFullbrightCoords(), OverlayTexture.NO_OVERLAY, overlayColor, alpha);
        buffer.endBatch();
    }

    public static void renderTranslucentItemStackModelGUI(ItemStack stack, PoseStack renderStack, Color overlayColor, Blending blendMode, int alpha) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, false);

        // 1.16.5: RenderSystem.enableRescaleNormal(); enableAlphaTest(); defaultAlphaFunc();
        // None of the three exist in 1.20.1 - normal rescaling and the alpha test are shader driven now.
        RenderSystem.enableBlend();
        blendMode.apply();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        renderStack.pushPose();
        renderStack.translate(8.0F, 8.0F, 0.0F);
        renderStack.scale(16.0F, -16.0F, 16.0F);

        BakedModel bakedModel = ForgeHooksClient.handleCameraTransforms(renderStack, getItemModel(stack), ItemDisplayContext.GUI, false);
        boolean isSideLit = bakedModel.usesBlockLight();
        if (!isSideLit) {
            com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderItemModelWithColor(stack, ItemDisplayContext.GUI, bakedModel, renderStack, buffer,
                LightmapUtil.getPackedFullbrightCoords(), OverlayTexture.NO_OVERLAY, overlayColor, Mth.clamp(alpha, 0, 255));
        buffer.endBatch();

        if (!isSideLit) {
            com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
        }

        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
        // 1.16.5: RenderSystem.disableAlphaTest(); RenderSystem.disableRescaleNormal(); - both removed in 1.20.1
        RenderSystem.enableDepthTest();
        renderStack.popPose();
    }

    //TODO wait for mojang to do their work and actually port this method so i don't have to do this myself
    @Deprecated
    public static void mcdefault_renderItemOverlayIntoGUI(Font fr, PoseStack renderStack, ItemStack stack, float pTicks, @Nullable String text) {
        if (stack.isEmpty()) {
            return;
        }
        // 1.16.5: RenderSystem.disableLighting(); - fixed-function lighting no longer exists in 1.20.1

        renderStack.pushPose();
        renderStack.translate(0, 0, 100F);
        if (stack.getCount() > 1 || text != null) {
            FormattedText display = Component.literal(ObjectUtils.firstNonNull(text, String.valueOf(stack.getCount())));
            int length = fr.width(display);

            renderStack.pushPose();
            renderStack.translate(17 - length, 9, 0);
            RenderingDrawUtils.renderStringAt(display, renderStack, fr, 0xFFFFFFFF, true);
            renderStack.popPose();
        }

        if (stack.getItem().isBarVisible(stack)) {
            RenderSystem.disableDepthTest();
            // 1.16.5: RenderSystem.disableTexture(); RenderSystem.disableAlphaTest(); - gone in 1.20.1
            RenderSystem.disableBlend();

            // 1.16.5 computed 13F - getDurabilityForDisplay(stack) * 13F; 1.20.1 exposes exactly that as getBarWidth
            float durabilityPercent = stack.getItem().getBarWidth(stack);
            int color = stack.getItem().getBarColor(stack);

            RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                RenderingGuiUtils.rect(buf, renderStack, 2, 13, 0, 13, 2)
                        .color(0, 0, 0, 255)
                        .draw();
                RenderingGuiUtils.rect(buf, renderStack, 2, 13, 0, durabilityPercent, 1)
                        .color(color >> 16 & 255, color >> 8 & 255, color & 255, 255)
                        .draw();
            });

            RenderSystem.enableBlend();
            // 1.16.5: RenderSystem.enableAlphaTest(); RenderSystem.enableTexture(); - gone in 1.20.1
            RenderSystem.enableDepthTest();
        }

        LocalPlayer player = Minecraft.getInstance().player;
        float cooldownPercent = player == null ? 0F : player.getCooldowns().getCooldownPercent(stack.getItem(), pTicks);
        if (cooldownPercent > 0F) {
            RenderSystem.disableDepthTest();
            // 1.16.5: RenderSystem.disableTexture(); - gone in 1.20.1
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                RenderingGuiUtils.rect(buf, renderStack, 0, 16F * (1F - cooldownPercent), 0, 16, 16F * cooldownPercent)
                        .color(255, 255, 255, 127)
                        .draw();
            });

            // 1.16.5: RenderSystem.enableTexture(); - gone in 1.20.1
            RenderSystem.enableDepthTest();
        }
        renderStack.popPose();
    }

    private static BakedModel getItemModel(ItemStack stack) {
        return Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, Minecraft.getInstance().player, 0);
    }

    private static void renderItemModelWithColor(ItemStack stack, ItemDisplayContext transformType, BakedModel model, PoseStack renderStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, Color c, int alpha) {
        if (!stack.isEmpty()) {
            renderStack.pushPose();
            renderStack.translate(-0.5, -0.5, -0.5);

            boolean renderThirdPersonView = transformType == ItemDisplayContext.GUI ||
                    transformType == ItemDisplayContext.GROUND ||
                    transformType == ItemDisplayContext.FIXED;

            if (model.isCustomRenderer() || (stack.getItem() == Items.TRIDENT && !renderThirdPersonView)) {
                int[] colors = new int[] { c.getRed(), c.getGreen(), c.getBlue(), alpha };
                MultiBufferSource decoratedBuffer = type -> BufferDecoratorBuilder.withColor((r, g, b, a) -> colors).decorate(buffer.getBuffer(type));
                IClientItemExtensions.of(stack).getCustomRenderer()
                        .renderByItem(stack, transformType, renderStack, decoratedBuffer, combinedLight, combinedOverlay);
            } else {
                // VERIFY_PORT[AS-RUTIL-01]: 1.16.5 branched on IBakedModel#isLayered()/getLayerModels() and otherwise
                // used the single "always translucent" RenderTypeLookup type. 1.20.1/Forge replaced both paths with
                // BakedModel#getRenderPasses(stack, fabulous) x BakedModel#getRenderTypes(stack, fabulous), whose
                // default implementation returns exactly {this} x {ItemBlockRenderTypes.getRenderType(stack, true)}.
                // Multi-pass (formerly "layered") models therefore fall out of the same loop now.
                List<BakedModel> renderPasses = model.getRenderPasses(stack, true);
                for (BakedModel pass : renderPasses) {
                    List<RenderType> renderTypes = pass.getRenderTypes(stack, true);
                    if (renderTypes.isEmpty()) {
                        //Always get translucent renderType
                        renderTypes = List.of(ItemBlockRenderTypes.getRenderType(stack, true));
                    }
                    for (RenderType rType : renderTypes) {
                        VertexConsumer vertexBuilder;

                        //Wth are you doing here mojang. Taken from ItemRenderer#renderItem
                        if (stack.getItem() instanceof CompassItem && stack.hasFoil()) {
                            renderStack.pushPose();
                            PoseStack.Pose topEntry = renderStack.last();

                            if (transformType == ItemDisplayContext.GUI) {
                                MatrixUtil.mulComponentWise(topEntry.pose(), 0.5F);
                            } else if (transformType.firstPerson()) {
                                MatrixUtil.mulComponentWise(topEntry.pose(), 0.75F);
                            }
                            vertexBuilder = ItemRenderer.getCompassFoilBufferDirect(buffer, rType, topEntry);
                            renderStack.popPose();
                        } else {
                            vertexBuilder = ItemRenderer.getFoilBuffer(buffer, rType, true, stack.hasFoil());
                        }

                        renderColoredItemModel(stack, pass, renderStack, vertexBuilder, combinedLight, combinedOverlay, c, alpha);
                    }
                }
            }

            renderStack.popPose();
        }
    }

    private static void renderColoredItemModel(ItemStack stack, BakedModel model, PoseStack renderStack, VertexConsumer buffer, int combinedLight, int combinedOverlay, Color color, int alpha) {
        Color alphaColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        RandomSource renderRand = RandomSource.create();
        ModelData data = ModelData.EMPTY;
        for (Direction dir : Direction.values()) {
            renderRand.setSeed(42);
            renderColoredQuads(buffer, renderStack, model.getQuads(null, dir, renderRand, data, null), alphaColor, combinedLight, combinedOverlay, stack);
        }

        renderRand.setSeed(42);
        renderColoredQuads(buffer, renderStack, model.getQuads(null, null, renderRand, data, null), alphaColor, combinedLight, combinedOverlay, stack);
    }

    private static void renderColoredQuads(VertexConsumer vb, PoseStack renderStack, List<BakedQuad> quads, Color color, int combinedLight, int combinedOverlay, ItemStack stack) {
        boolean useOverlayColors = (color.getRGB() & 0xFFFFFF) == 0xFFFFFF && !stack.isEmpty();
        int i = 0;

        net.minecraft.client.color.item.ItemColors itemColors = Minecraft.getInstance().getItemColors();
        for (int j = quads.size(); i < j; ++i) {
            BakedQuad bakedquad = quads.get(i);
            int col = color.getRGB();
            if (useOverlayColors && bakedquad.isTinted()) {
                col = itemColors.getColor(stack, bakedquad.getTintIndex());
            }

            float r = (col >> 16 & 255) / 255F;
            float g = (col >> 8 & 255) / 255F;
            float b = (col & 255) / 255F;
            float a = color.getAlpha() / 255F;

            vb.putBulkData(renderStack.last(), bakedquad, r, g, b, a, combinedLight, combinedOverlay, true);
        }
    }

    public static void renderSimpleBlockModel(BlockState state, PoseStack renderStack, VertexConsumer vb) {
        renderSimpleBlockModel(state, renderStack, vb, BlockPos.ZERO, null, false);
    }

    public static void renderSimpleBlockModel(BlockState state, PoseStack renderStack, VertexConsumer vb, BlockPos pos, @Nullable BlockEntity te, boolean checkRenderSide) {
        if (plainRenderWorld == null) {
            plainRenderWorld = new EmptyRenderWorld(() -> getClientRegistryValue(Registries.BIOME, Biomes.PLAINS));
        }

        RenderShape brt = state.getRenderShape();
        if (brt == RenderShape.INVISIBLE) {
            return;
        }
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        ModelData data = ModelData.EMPTY;
        if (te != null) {
            data = te.getModelData();
        }
        brd.renderBatched(state, pos, plainRenderWorld, renderStack, vb, checkRenderSide, rand, data, null);
    }

    public static void renderSimpleBlockModelCurrentWorld(BlockState state, PoseStack renderStack, VertexConsumer buf, int combinedOverlayIn) {
        renderSimpleBlockModelCurrentWorld(state, renderStack, buf, BlockPos.ZERO, null, combinedOverlayIn, false);
    }

    public static void renderSimpleBlockModelCurrentWorld(BlockState state, PoseStack renderStack, VertexConsumer buf, BlockPos pos, @Nullable BlockEntity te, int combinedOverlayIn, boolean checkRenderSide) {
        RenderShape brt = state.getRenderShape();
        if (brt == RenderShape.INVISIBLE) {
            return;
        }
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        ModelData data = ModelData.EMPTY;
        if (te != null) {
            data = te.getModelData();
        }
        if (brt == RenderShape.MODEL) {
            BakedModel model = brd.getBlockModel(state);
            brd.getModelRenderer().tesselateBlock(Minecraft.getInstance().level, model, state, pos, renderStack, buf, checkRenderSide, rand, state.getSeed(pos), combinedOverlayIn, data, null);
        }
    }

    /**
     * 1.16.5 used ObserverLib's {@code RegistryUtil.client()}; ObserverLib is only a source reference here, so the
     * client-side registry lookup is inlined.
     */
    @Nullable
    private static <V> V getClientRegistryValue(ResourceKey<net.minecraft.core.Registry<V>> registry, ResourceKey<V> key) {
        Minecraft mc = Minecraft.getInstance();
        RegistryAccess access = mc.getConnection() != null
                ? mc.getConnection().registryAccess()
                : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        return access.registryOrThrow(registry).get(key);
    }
}
