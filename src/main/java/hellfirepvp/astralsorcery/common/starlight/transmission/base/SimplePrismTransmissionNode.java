package hellfirepvp.astralsorcery.common.starlight.transmission.base;

import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
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

public class SimplePrismTransmissionNode implements IPrismTransmissionNode {

    private boolean ignoreBlockCollision = false;
    private BlockPos thisPos;
    private final Set<BlockPos> sourcesToThis = new HashSet<>();
    private final Map<BlockPos, PrismNext> nextNodes = new HashMap<>();

    public SimplePrismTransmissionNode(BlockPos thisPos) {
        this.thisPos = thisPos;
    }

    @Override
    public BlockPos getLocationPos() {
        return thisPos;
    }

    public void updateIgnoreBlockCollisionState(Level world, boolean ignoreBlockCollision) {
        this.ignoreBlockCollision = ignoreBlockCollision;
        for (PrismNext next : nextNodes.values()) {
            next.reachable = ignoreBlockCollision || next.rayAssist.isClear(world);
        }
    }

    public boolean ignoresBlockCollision() {
        return ignoreBlockCollision;
    }

    @Override
    public boolean notifyUnlink(Level world, BlockPos to) {
        return nextNodes.remove(to) != null;
    }

    @Override
    public void notifyLink(Level world, BlockPos pos) {
        addLink(world, pos, true, false);
    }

    private void addLink(Level world, BlockPos pos, boolean doRayCheck, boolean previousRayState) {
        PrismNext nextNode = new PrismNext(this, world, thisPos, pos, doRayCheck, previousRayState);
        this.nextNodes.put(pos, nextNode);
    }

    @Override
    public boolean notifyBlockChange(Level world, BlockPos at) {
        boolean anyChange = false;
        for (PrismNext next : nextNodes.values()) {
            if (next.notifyBlockPlace(world, thisPos, at)) {
                anyChange = true;
            }
        }
        return anyChange;
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
    public List<NodeConnection<IPrismTransmissionNode>> queryNext(WorldNetworkHandler handler) {
        List<NodeConnection<IPrismTransmissionNode>> nodes = new LinkedList<>();
        for (PrismNext next : nextNodes.values()) {
            nodes.add(new NodeConnection<>(handler.getTransmissionNode(next.pos), next.pos, next.reachable));
        }
        return nodes;
    }

    @Override
    public List<BlockPos> getSources() {
        return new LinkedList<>(sourcesToThis);
    }

    @Override
    public TransmissionProvider getProvider() {
        return new Provider();
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        this.thisPos = NBTHelper.readBlockPosFromNBT(compound);
        this.sourcesToThis.clear();
        this.nextNodes.clear();
        this.ignoreBlockCollision = compound.getBoolean("ignoreBlockCollision");

        ListTag sources = compound.getList("sources", Tag.TAG_COMPOUND);
        for (int i = 0; i < sources.size(); i++) {
            sourcesToThis.add(NBTHelper.readBlockPosFromNBT(sources.getCompound(i)));
        }

        ListTag nextList = compound.getList("nextList", Tag.TAG_COMPOUND);
        for (int i = 0; i < nextList.size(); i++) {
            CompoundTag tag = nextList.getCompound(i);
            addLink(null, NBTHelper.readBlockPosFromNBT(tag), false, tag.getBoolean("rayState"));
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

        ListTag nextList = new ListTag();
        for (PrismNext next : nextNodes.values()) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(next.pos, tag);
            tag.putBoolean("rayState", next.reachable);
            nextList.add(tag);
        }
        compound.put("nextList", nextList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimplePrismTransmissionNode that = (SimplePrismTransmissionNode) o;
        return Objects.equals(thisPos, that.thisPos);
    }

    @Override
    public int hashCode() {
        return thisPos != null ? thisPos.hashCode() : 0;
    }

    private static class PrismNext {
        private final SimplePrismTransmissionNode parent;
        private final BlockPos pos;
        private final RaytraceAssist rayAssist;
        private final double distanceSq;
        private boolean reachable;

        private PrismNext(SimplePrismTransmissionNode parent, Level world, BlockPos start, BlockPos end, boolean doRayTest, boolean oldRayState) {
            this.parent = parent;
            this.pos = end;
            this.rayAssist = new RaytraceAssist(start, end);
            if (doRayTest && world != null) {
                this.reachable = parent.ignoreBlockCollision || rayAssist.isClear(world);
            } else {
                this.reachable = oldRayState;
            }
            this.distanceSq = Vec3.atCenterOf(end).distanceToSqr(Vec3.atCenterOf(start));
        }

        private boolean notifyBlockPlace(Level world, BlockPos connect, BlockPos at) {
            Vec3 changed = Vec3.atCenterOf(at);
            double dstStart = Vec3.atCenterOf(connect).distanceToSqr(changed);
            double dstEnd = Vec3.atCenterOf(pos).distanceToSqr(changed);
            if (dstStart > distanceSq || dstEnd > distanceSq) {
                return false;
            }
            boolean oldState = this.reachable;
            this.reachable = parent.ignoreBlockCollision || rayAssist.isClear(world);
            return this.reachable != oldState;
        }
    }

    public static class Provider extends TransmissionProvider {
        @Override
        public IPrismTransmissionNode get() {
            return new SimplePrismTransmissionNode(null);
        }
    }
}
