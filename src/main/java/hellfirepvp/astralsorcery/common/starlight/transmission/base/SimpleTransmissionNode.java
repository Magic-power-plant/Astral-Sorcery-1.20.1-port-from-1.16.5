package hellfirepvp.astralsorcery.common.starlight.transmission.base;

import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.NodeConnection;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.data.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SimpleTransmissionNode implements ITransmissionNode {

    private boolean ignoreBlockCollision = false;
    private boolean nextReachable = false;
    private BlockPos nextPos = null;
    private double dstToNextSq = 0;
    private RaytraceAssist assistNext = null;
    private BlockPos thisPos;
    private final Set<BlockPos> sourcesToThis = new HashSet<>();

    public SimpleTransmissionNode(BlockPos thisPos) {
        this.thisPos = thisPos;
    }

    @Override
    public BlockPos getLocationPos() {
        return thisPos;
    }

    public void updateIgnoreBlockCollisionState(Level world, boolean ignoreBlockCollision) {
        this.ignoreBlockCollision = ignoreBlockCollision;
        if (assistNext != null) {
            this.nextReachable = ignoreBlockCollision || assistNext.isClear(world);
        }
    }

    public boolean ignoresBlockCollision() {
        return ignoreBlockCollision;
    }

    @Override
    public boolean notifyUnlink(Level world, BlockPos to) {
        if (to.equals(nextPos)) {
            this.nextPos = null;
            this.assistNext = null;
            this.dstToNextSq = 0;
            this.nextReachable = false;
            return true;
        }
        return false;
    }

    @Override
    public void notifyLink(Level world, BlockPos pos) {
        addLink(world, pos, true, false);
    }

    private void addLink(Level world, BlockPos pos, boolean doRayTest, boolean oldRayState) {
        this.nextPos = pos;
        this.assistNext = new RaytraceAssist(thisPos, nextPos);
        if (doRayTest && world != null) {
            this.nextReachable = this.ignoreBlockCollision || assistNext.isClear(world);
        } else {
            this.nextReachable = oldRayState;
        }
        this.dstToNextSq = Vec3.atCenterOf(pos).distanceToSqr(Vec3.atCenterOf(thisPos));
    }

    @Override
    public boolean notifyBlockChange(Level world, BlockPos at) {
        if (nextPos == null || assistNext == null) {
            return false;
        }
        Vec3 changed = Vec3.atCenterOf(at);
        double dstStart = Vec3.atCenterOf(thisPos).distanceToSqr(changed);
        double dstEnd = Vec3.atCenterOf(nextPos).distanceToSqr(changed);
        if (dstStart > dstToNextSq || dstEnd > dstToNextSq) {
            return false;
        }
        boolean oldState = this.nextReachable;
        this.nextReachable = ignoreBlockCollision || assistNext.isClear(world);
        return this.nextReachable != oldState;
    }

    @Override
    public void notifySourceLink(Level world, BlockPos source) {
        sourcesToThis.add(source);
    }

    @Override
    public void notifySourceUnlink(Level world, BlockPos source) {
        sourcesToThis.remove(source);
    }

    @Override
    public NodeConnection<IPrismTransmissionNode> queryNextNode(WorldNetworkHandler handler) {
        if (nextPos == null) {
            return null;
        }
        return new NodeConnection<>(handler.getTransmissionNode(nextPos), nextPos, nextReachable);
    }

    @Override
    public List<BlockPos> getSources() {
        return new ArrayList<>(sourcesToThis);
    }

    @Override
    public TransmissionProvider getProvider() {
        return new Provider();
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        this.thisPos = NBTHelper.readBlockPosFromNBT(compound);
        this.sourcesToThis.clear();
        this.ignoreBlockCollision = compound.getBoolean("ignoreBlockCollision");

        ListTag sources = compound.getList("sources", Tag.TAG_COMPOUND);
        for (int i = 0; i < sources.size(); i++) {
            sourcesToThis.add(NBTHelper.readBlockPosFromNBT(sources.getCompound(i)));
        }

        if (compound.contains("nextPos", Tag.TAG_COMPOUND)) {
            CompoundTag tag = compound.getCompound("nextPos");
            BlockPos next = NBTHelper.readBlockPosFromNBT(tag);
            addLink(null, next, false, tag.getBoolean("rayState"));
        }
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        NBTHelper.writeBlockPosToNBT(thisPos, compound);
        compound.putBoolean("ignoreBlockCollision", this.ignoreBlockCollision);

        ListTag sources = new ListTag();
        for (BlockPos source : sourcesToThis) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(source, tag);
            sources.add(tag);
        }
        compound.put("sources", sources);

        if (nextPos != null) {
            CompoundTag pos = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(nextPos, pos);
            pos.putBoolean("rayState", nextReachable);
            compound.put("nextPos", pos);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTransmissionNode that = (SimpleTransmissionNode) o;
        return Objects.equals(thisPos, that.thisPos);
    }

    @Override
    public int hashCode() {
        return thisPos != null ? thisPos.hashCode() : 0;
    }

    public static class Provider extends TransmissionProvider {
        @Override
        public IPrismTransmissionNode get() {
            return new SimpleTransmissionNode(null);
        }
    }
}
