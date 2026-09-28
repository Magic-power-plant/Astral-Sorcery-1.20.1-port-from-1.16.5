/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EmptyRenderWorld
 * Created by HellFirePvP
 * Date: 18.07.2019 / 16:35
 */
public class EmptyRenderWorld implements BlockAndTintGetter {

    private final Biome biome;

    public EmptyRenderWorld(Supplier<Biome> biomeSupplier) {
        this.biome = biomeSupplier.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShade(Direction direction, boolean b) {
        return 1.0F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return new StructureRenderLightManager(this.getMaxLightLevel());
    }

    @Override
    public int getBlockTint(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return colorResolverIn.getColor(biome, blockPosIn.getX(), blockPosIn.getZ());
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return this.getMaxLightLevel();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return Fluids.EMPTY.defaultFluidState();
    }

    /*
     * 1.16.5 only had to implement IBlockDisplayReader; in 1.20.1 BlockGetter also extends LevelHeightAccessor,
     * which forces the two height accessors below. A structure preview never resolves any actual block, so the
     * degenerate 0-height world of ObserverLib's fake light chunk getter is reused here.
     */
    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getMinBuildHeight() {
        return 0;
    }

    /**
     * Embedded from ObserverLib (hellfirepvp.observerlib.api.client.StructureRenderLightManager).
     * ObserverLib is a source-only reference for this port and must never become a runtime dependency, so the
     * light manager lives here, next to its only consumer.
     */
    public static class StructureRenderLightManager extends LevelLightEngine {

        private final int lightLevel;

        public StructureRenderLightManager(int lightLevel) {
            super(FakeLightChunkGetter.INSTANCE, false, false);
            this.lightLevel = lightLevel;
        }

        @Override
        public void checkBlock(BlockPos pPos) {}

        @Override
        public boolean hasLightWork() {
            return false;
        }

        @Override
        public int runLightUpdates() {
            return 0;
        }

        @Override
        public void updateSectionStatus(BlockPos pPos, boolean pIsQueueEmpty) {}

        @Override
        public void updateSectionStatus(SectionPos pPos, boolean pIsEmpty) {}

        @Override
        public void setLightEnabled(ChunkPos pChunkPos, boolean pLightEnabled) {}

        @Override
        public void propagateLightSources(ChunkPos pChunkPos) {}

        @Override
        public LayerLightEventListener getLayerListener(LightLayer pType) {
            return new ConstantLightEngine(this.lightLevel);
        }

        @Override
        public String getDebugData(LightLayer pLightLayer, SectionPos pSectionPos) {
            return "n/a";
        }

        @Override
        public LayerLightSectionStorage.SectionType getDebugSectionType(LightLayer pLightLayer, SectionPos pSectionPos) {
            return LayerLightSectionStorage.SectionType.EMPTY;
        }

        @Override
        public void queueSectionData(LightLayer pLightLayer, SectionPos pSectionPos, @Nullable DataLayer pDataLayer) {}

        @Override
        public void retainData(ChunkPos pPos, boolean pRetain) {}

        @Override
        public int getRawBrightness(BlockPos pBlockPos, int pAmount) {
            return super.getRawBrightness(pBlockPos, pAmount);
        }

        @Override
        public boolean lightOnInSection(SectionPos pSectionPos) {
            return true;
        }

        //Mainly concerned with network sync for lights? unnecessary for rendering it seems
        @Override
        public int getLightSectionCount() {
            return 0;
        }

        @Override
        public int getMinLightSection() {
            return 0;
        }

        @Override
        public int getMaxLightSection() {
            return 0;
        }

        private static class FakeLightChunkGetter implements LightChunkGetter {

            private static final FakeLightChunkGetter INSTANCE = new FakeLightChunkGetter();

            @Nullable
            @Override
            public LightChunk getChunkForLighting(int pChunkX, int pChunkZ) {
                return null;
            }

            @Override
            public BlockGetter getLevel() {
                return FakeBlockGetter.INSTANCE;
            }
        }

        private static class FakeBlockGetter implements BlockGetter {

            private static final FakeBlockGetter INSTANCE = new FakeBlockGetter();

            @Nullable
            @Override
            public BlockEntity getBlockEntity(BlockPos pPos) {
                return null;
            }

            @Override
            public BlockState getBlockState(BlockPos p_45571_) {
                return Blocks.AIR.defaultBlockState();
            }

            @Override
            public FluidState getFluidState(BlockPos pPos) {
                return Fluids.EMPTY.defaultFluidState();
            }

            @Override
            public int getHeight() {
                return 0;
            }

            @Override
            public int getMinBuildHeight() {
                return 0;
            }
        }

        private record ConstantLightEngine(int lightLevel) implements LayerLightEventListener {

            @Nullable
            @Override
            public DataLayer getDataLayerData(SectionPos pos) {
                return null;
            }

            @Override
            public int getLightValue(BlockPos pos) {
                return this.lightLevel;
            }

            @Override
            public void checkBlock(BlockPos pos) {}

            @Override
            public boolean hasLightWork() {
                return false;
            }

            @Override
            public int runLightUpdates() {
                return 0;
            }

            @Override
            public void updateSectionStatus(SectionPos sectionPos, boolean isQueueEmpty) {}

            @Override
            public void setLightEnabled(ChunkPos chPos, boolean isLightEnabled) {}

            @Override
            public void propagateLightSources(ChunkPos chPos) {}
        }
    }
}
