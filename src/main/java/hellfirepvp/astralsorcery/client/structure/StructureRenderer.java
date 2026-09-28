/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.structure;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.common.structure.match.structure.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureRenderer
 * Created by HellFirePvP
 * Date: 08.10.2019 / 19:50
 *
 * Embedded from ObserverLib (hellfirepvp.observerlib.api.client.StructureRenderer); ObserverLib is a source-only
 * reference for this port, not a runtime dependency, exactly as with {@code common/tick/TickManager}. Deliberate
 * deviations from the reference, kept as small as possible so it can be re-synced:
 *   - hellfirepvp.observerlib.api.structure.Structure becomes this tree's
 *     common/structure/match/structure/Structure.
 *   - hellfirepvp.observerlib.common.util.RegistryUtil#client() is inlined through
 *     {@link StructureRenderWorld#clientRegistryAccess()}.
 *   - ObserverLib toggles a static {@code BlockAirRequirement.displayRequiredAir} around the render so that
 *     "required air" positions draw a visible marker block. This tree has no BlockAirRequirement, and its
 *     {@code MatchableState.REQUIRES_AIR} is the very same object as {@code MatchableState.AIR}, whose
 *     descriptive state is plain {@code Blocks.AIR} - which the loop below skips. {@link #setRenderWithRequiredAir}
 *     is therefore kept for call-site parity but has NO visual effect here; the journal's "show air" button still
 *     works, because RenderPageStructure also uses it to decide which slices are non-empty.
 *   - the reference's unused {@code private static final Random rand} field is dropped; it renders with
 *     {@link RandomSource} instead.
 */
@OnlyIn(Dist.CLIENT)
public class StructureRenderer {

    private final StructureRenderWorld world;
    private final Structure structure;

    //In degrees
    private double rotationX = 0, rotationY = 0, rotationZ = 0;
    private boolean isolateIndividualBlockRender = false;

    private boolean displayWithRequiredAir = false;

    public StructureRenderer(Structure structure) {
        this.structure = structure;
        Holder<Biome> plainsBiome = StructureRenderWorld.clientRegistryAccess()
                .registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        this.world = new StructureRenderWorld(this.structure, plainsBiome);
        this.resetRotation();
    }

    private void resetRotation() {
        this.rotationX = -30;
        this.rotationY = 45;
        this.rotationZ = 0;
    }

    public void rotate(double x, double y, double z) {
        this.rotationX += x;
        this.rotationY += y;
        this.rotationZ += z;
    }

    /**
     * Kept for parity with ObserverLib's call sites; see the class javadoc - it has no visual effect in this tree.
     */
    public void setRenderWithRequiredAir(boolean displayWithRequiredAir) {
        this.displayWithRequiredAir = displayWithRequiredAir;
    }

    public StructureRenderer setIsolateIndividualBlock(boolean isolateIndividualBlockRender) {
        this.isolateIndividualBlockRender = isolateIndividualBlockRender;
        return this;
    }

    public void rotateFromMouseDrag(float mouseDX, float mouseDZ) {
        this.rotate(0.5 * -mouseDZ, 0.5 * mouseDX, 0);
    }

    public LevelReader getRenderWorld() {
        return world;
    }

    public Structure getStructure() {
        return structure;
    }

    public int getDefaultSlice() {
        return this.structure.getMinimumOffset().getY();
    }

    public boolean hasSlice(int y) {
        return y >= this.structure.getMinimumOffset().getY() && y <= this.structure.getMaximumOffset().getY();
    }

    public void render3DGUI(PoseStack renderStack, double x, double y, float pTicks) {
        render3DSliceGUI(renderStack, x, y, pTicks, Optional.empty());
    }

    public void render3DSliceGUI(PoseStack renderStack, double x, double y, float pTicks, Optional<Integer> slice) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen == null) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();

        float mul = 10.5F;
        float size = 2;
        float minSize = 0.5F;

        Vec3i max = this.structure.getMaximumOffset();
        Vec3i min = this.structure.getMinimumOffset();

        float maxLength = 0;
        float pointDst = max.getX() - min.getX();
        if (pointDst > maxLength) {
            maxLength = pointDst;
        }
        pointDst = max.getY() - min.getY();
        if (pointDst > maxLength) {
            maxLength = pointDst;
        }
        pointDst = max.getZ() - min.getZ();
        if (pointDst > maxLength) {
            maxLength = pointDst;
        }
        maxLength -= 5;

        if(maxLength > 0) {
            size = (size - minSize) * (1F - (maxLength / 20F));
        }

        float dr = -5.75F * size;

        Minecraft.getInstance().getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
        MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        BlockEntityRenderDispatcher berd = Minecraft.getInstance().getBlockEntityRenderDispatcher();

        slice.ifPresent(ySlice -> this.world.pushContentFilter((pos) -> pos.getY() == ySlice));

        renderStack.pushPose();
        renderStack.translate(x + 16D / scale, y + 16D / scale, 512);
        renderStack.translate(dr, dr, dr);
        renderStack.mulPose(Axis.XP.rotationDegrees((float) rotationX));
        renderStack.mulPose(Axis.YP.rotationDegrees((float) rotationY));
        renderStack.mulPose(Axis.ZP.rotationDegrees((float) rotationZ));
        renderStack.translate(-dr, -dr, -dr);
        renderStack.scale(-size * mul, -size * mul, -size * mul);
        slice.ifPresent(ySlice -> renderStack.translate(0, -ySlice, 0));

        this.structure.getContents().keySet()
                .forEach(pos -> {
                    BlockState view = this.world.getBlockState(pos);
                    if (!view.getBlock().equals(Blocks.AIR)) {
                        renderStack.pushPose();
                        renderStack.translate(pos.getX(), pos.getY(), pos.getZ());

                        if (!view.getFluidState().isEmpty()) {
                            this.renderFluid(pos, view, view.getFluidState(), buffers.getBuffer(RenderType.translucent()));
                        }
                        RenderType type = ItemBlockRenderTypes.getMovingBlockRenderType(view);
                        if (this.isolateIndividualBlockRender) {
                            this.world.pushContentFilter(wPos -> wPos.equals(pos));
                            this.renderBlock(pos, view, buffers.getBuffer(type), renderStack);
                            this.world.popContentFilter();
                        } else {
                            this.renderBlock(pos, view, buffers.getBuffer(type), renderStack);
                        }
                        renderStack.popPose();
                    }
                });
        buffers.endBatch();

        this.structure.getContents().keySet()
                .forEach(pos -> {
                    if (this.isolateIndividualBlockRender) {
                        this.world.pushContentFilter(wPos -> wPos.equals(pos));
                    }
                    BlockEntity tile = this.world.getBlockEntity(pos);
                    if (tile != null) {
                        BlockEntityRenderer tesr = berd.getRenderer(tile);
                        if (tesr != null) {
                            renderStack.pushPose();
                            renderStack.translate(pos.getX(), pos.getY(), pos.getZ());
                            tesr.render(tile, 0, renderStack, buffers, LevelRenderer.getLightColor(this.world, pos), OverlayTexture.NO_OVERLAY);
                            renderStack.popPose();
                        }
                    }
                    if (this.isolateIndividualBlockRender) {
                        this.world.popContentFilter();
                    }
                });
        buffers.endBatch();

        slice.ifPresent(ySlice -> this.world.popContentFilter());
        renderStack.popPose();
    }

    private void renderFluid(BlockPos pos, BlockState state, FluidState fluidState, VertexConsumer buf) {
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        brd.renderLiquid(pos, this.world, buf, state, fluidState);
    }

    private void renderBlock(BlockPos offset, BlockState state, VertexConsumer vb, PoseStack renderStack) {
        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        if (state.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }
        BakedModel model = brd.getBlockModel(state);
        ModelData modelData = model.getModelData(this.world, offset, state, ModelData.EMPTY);
        RandomSource randSrc = RandomSource.create(state.getSeed(offset));
        for (RenderType renderType : model.getRenderTypes(state, randSrc, modelData)) {
            brd.renderBatched(state, offset, this.world, renderStack, vb, false, randSrc, modelData, renderType);
        }
    }
}
