/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.structure;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.common.structure.match.block.MatchableState;
import hellfirepvp.astralsorcery.common.structure.match.structure.Structure;
import hellfirepvp.astralsorcery.common.structure.match.blockentity.MatchableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * This class supports to render a structure, centered around structure origin 0, 0, 0.
 *
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureRenderWorld
 * Created by HellFirePvP
 * Date: 30.04.2019 / 22:22
 *
 * Embedded from ObserverLib (hellfirepvp.observerlib.api.client.StructureRenderWorld); ObserverLib is a
 * source-only reference for this port, not a runtime dependency, exactly as with {@code common/tick/TickManager}.
 * Deliberate deviations from the reference, kept as small as possible so it can be re-synced:
 *   - hellfirepvp.observerlib.api.structure.Structure / api.block.MatchableState / api.tile.MatchableTile
 *     become this tree's common/structure/match equivalents. AS's MatchableTile is generic
 *     ({@code MatchableTile<T extends BlockEntity>}) where ObserverLib's is raw, hence the unchecked call below.
 *   - hellfirepvp.observerlib.client.util.ClientTickHelper#getClientTick becomes
 *     {@link ClientScheduler#getClientTick()}, which this tree already ticks.
 *   - hellfirepvp.observerlib.common.util.RegistryUtil#client() is inlined as
 *     {@link #clientRegistryAccess()} rather than embedding that whole class for two call sites.
 */
@OnlyIn(Dist.CLIENT)
public class StructureRenderWorld implements LevelReader {

    private static final int MAX_LIGHT = 15;

    private final Holder<Biome> globalBiome;
    private final SingleBiomeManager biomeManager;
    private final DimensionType thisDimType;
    private final WorldBorder maxBorder;

    private final Structure structure;
    private final Stack<Predicate<BlockPos>> blockFilter = new Stack<>();

    public StructureRenderWorld(Structure structure, Holder<Biome> globalBiome) {
        this.structure = structure;
        this.globalBiome = globalBiome;
        this.biomeManager = new SingleBiomeManager(this.globalBiome);
        RegistryAccess access = clientRegistryAccess();
        DimensionType dimType = access.registryOrThrow(Registries.DIMENSION_TYPE)
                .get(BuiltinDimensionTypes.OVERWORLD);
        if (dimType == null) {
            dimType = Iterables.getFirst(access.registryOrThrow(Registries.DIMENSION_TYPE), null);
        }
        this.thisDimType = dimType;

        if (this.thisDimType.coordinateScale() != 1.0D) {
            this.maxBorder = new WorldBorder() {
                public double getCenterX() {
                    return super.getCenterX() / thisDimType.coordinateScale();
                }

                public double getCenterZ() {
                    return super.getCenterZ() / thisDimType.coordinateScale();
                }
            };
        } else {
            this.maxBorder = new WorldBorder();
        }
    }

    /**
     * Inlined from ObserverLib's RegistryUtil#client(): prefer the connection's synced registry access, and fall
     * back to the built-in registries when there is no connection yet (the journal can be opened from the title
     * screen's mod list in dev).
     */
    static RegistryAccess clientRegistryAccess() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        }
        return mc.getConnection().registryAccess();
    }

    public void pushContentFilter(@Nonnull Predicate<BlockPos> blockFilter) {
        this.blockFilter.push(blockFilter);
    }

    public void popContentFilter() {
        this.blockFilter.pop();
    }

    private boolean allowAccess(BlockPos pos) {
        for (Predicate<BlockPos> filter : this.blockFilter) {
            if (!filter.test(pos)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BlockEntity getBlockEntity(BlockPos pos) {
        if (!this.structure.hasBlockAt(pos) || !allowAccess(pos)) {
            return null;
        }
        MatchableState state = this.structure.getBlockStateAt(pos);
        BlockEntity blockEntity = state.createBlockEntity(this, pos, ClientScheduler.getClientTick());
        if (blockEntity == null) {
            return null;
        }
        blockEntity.setLevel(Minecraft.getInstance().level);

        MatchableBlockEntity blockEntityMatch = this.structure.getBlockEntityAt(pos);
        if (blockEntityMatch == null) {
            return blockEntity;
        }
        CompoundTag tag = blockEntity.saveWithoutMetadata();
        blockEntityMatch.writeDisplayData(blockEntity, ClientScheduler.getClientTick(), tag);
        blockEntity.load(tag);

        blockEntityMatch.postPlacement(blockEntity, this, pos);
        return blockEntity;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!this.structure.hasBlockAt(pos) || !allowAccess(pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        MatchableState state = this.structure.getContents().get(pos);
        return state == null ? Blocks.AIR.defaultBlockState() : state.getDescriptiveState(ClientScheduler.getClientTick());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    //Something with lighting and AO it seems?
    //Some light-multiplier based on direction? seems sketchy tbh
    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShade(Direction direction, boolean b) {
        return 1F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return new StructureRenderLightManager(MAX_LIGHT);
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return MAX_LIGHT;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int amount) {
        return MAX_LIGHT;
    }

    @Override
    public int getMaxLightLevel() {
        return MAX_LIGHT;
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return null;
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return false;
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Types heightmapType, BlockPos pos) {
        return null;
    }

    @Override
    public RegistryAccess registryAccess() {
        return clientRegistryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlags.DEFAULT_FLAGS;
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        return 0;
    }

    @Override
    public int getSkyDarken() {
        return 0;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return biomeManager;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return globalBiome;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return maxBorder;
    }

    @Override
    public boolean isUnobstructed(@Nullable Entity entityIn, VoxelShape shape) {
        return true;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity p_186427_, AABB p_186428_) {
        return Lists.newArrayList();
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public DimensionType dimensionType() {
        return this.thisDimType;
    }
}
