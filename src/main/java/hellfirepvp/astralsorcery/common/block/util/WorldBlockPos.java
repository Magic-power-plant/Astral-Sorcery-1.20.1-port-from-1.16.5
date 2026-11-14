package hellfirepvp.astralsorcery.common.block.util;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.object.TransformReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

public class WorldBlockPos extends BlockPos {

    private final TransformReference<ResourceKey<Level>, Level> worldReference;

    private WorldBlockPos(TransformReference<ResourceKey<Level>, Level> worldReference, BlockPos pos) {
        super(pos);
        this.worldReference = worldReference;
    }

    private WorldBlockPos(ResourceKey<Level> type, BlockPos pos, Function<ResourceKey<Level>, Level> worldProvider) {
        super(pos);
        this.worldReference = new TransformReference<>(type, worldProvider);
    }

    public static WorldBlockPos wrapServer(Level world, BlockPos pos) {
        return new WorldBlockPos(world.dimension(), pos, type -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            return server.getLevel(type);
        });
    }

    public static WorldBlockPos wrapBlockEntity(BlockEntity blockEntity) {
        return new WorldBlockPos(blockEntity.getLevel().dimension(), blockEntity.getBlockPos(), type -> blockEntity.getLevel());
    }

    public ResourceKey<Level> getWorldKey() {
        return this.worldReference.getReference();
    }

    private WorldBlockPos wrapInternal(BlockPos pos) {
        return new WorldBlockPos(this.worldReference, pos);
    }

    @Override
    public WorldBlockPos above() {
        return wrapInternal(super.above());
    }

    @Override
    public WorldBlockPos below() {
        return wrapInternal(super.below());
    }

    @Override
    public WorldBlockPos north() {
        return wrapInternal(super.north());
    }

    @Override
    public WorldBlockPos south() {
        return wrapInternal(super.south());
    }

    @Override
    public WorldBlockPos east() {
        return wrapInternal(super.east());
    }

    @Override
    public WorldBlockPos west() {
        return wrapInternal(super.west());
    }

    @Override
    public WorldBlockPos offset(int x, int y, int z) {
        return wrapInternal(super.offset(x, y, z));
    }

    @Override
    public WorldBlockPos offset(Vec3i vec) {
        return wrapInternal(super.offset(vec));
    }

    @Override
    public WorldBlockPos subtract(Vec3i vec) {
        return wrapInternal(super.subtract(vec));
    }

    @Override
    public WorldBlockPos relative(net.minecraft.core.Direction direction) {
        return wrapInternal(super.relative(direction));
    }

    @Override
    public WorldBlockPos relative(net.minecraft.core.Direction direction, int distance) {
        return wrapInternal(super.relative(direction, distance));
    }

    @Override
    public WorldBlockPos multiply(int scalar) {
        return wrapInternal(super.multiply(scalar));
    }

    @Nullable
    public <T extends BlockEntity> T getBlockEntityAt(Class<T> blockEntityClass, boolean forceChunkLoad) {
        Level world = this.worldReference.getValue();
        if (world != null) {
            return MiscUtils.getBlockEntityAt(world, this, blockEntityClass, forceChunkLoad);
        }
        return null;
    }

    @Nullable
    public Level getWorld() {
        return this.worldReference.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorldBlockPos that = (WorldBlockPos) o;
        return Objects.equals(getWorldKey(), that.getWorldKey());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getWorldKey().hashCode();
        return result;
    }

}
