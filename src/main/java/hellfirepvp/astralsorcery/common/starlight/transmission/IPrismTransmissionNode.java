package hellfirepvp.astralsorcery.common.starlight.transmission;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Random;

public interface IPrismTransmissionNode {

    Random rand = new Random();

    //1.16.5 IPrismTransmissionNode.java:35 - the default transmission properties of a node that carries no
    //crystal, e.g. a plain lens. An empty attribute set, not null: CrystalCalculations#getThroughputMultiplier
    //multiplies over the attributes it is given, so an empty set yields the neutral 1.0 multiplier.
    CrystalAttributes EMPTY = CrystalAttributes.Builder.newBuilder(false).build();

    BlockPos getLocationPos();

    default BlockPos getPosition() {
        return getLocationPos();
    }

    default float getTransmissionEfficiency() {
        return 1F;
    }

    default float getTransmissionRange() {
        return 32F;
    }

    default <T extends BlockEntity> boolean updateFromTileEntity(T tile) {
        return true;
    }

    default float getTransmissionThroughputMultiplier() {
        return 1F - getTransmissionConsumptionMultiplier();
    }

    default float getTransmissionConsumptionMultiplier() {
        return 0F;
    }

    //Get this node's transmission properties to calculate transmission loss and so on.
    //1.16.5 types this as CrystalAttributes and defaults to EMPTY. It had been widened to Object returning null,
    //which forced an Object-taking bridge in common/auxiliary/CrystalCalculations; both overrides
    //(CrystalTransmissionNode, CrystalPrismTransmissionNode) already return CrystalAttributes.
    default CrystalAttributes getTransmissionProperties() {
        return EMPTY;
    }

    default boolean needsTransmissionUpdate() {
        return false;
    }

    default void onTransmissionTick(Level world, float starlightAmt, hellfirepvp.astralsorcery.common.constellation.IWeakConstellation type) {}

    boolean notifyUnlink(Level world, BlockPos to);

    void notifyLink(Level world, BlockPos to);

    void notifySourceLink(Level world, BlockPos source);

    void notifySourceUnlink(Level world, BlockPos source);

    boolean notifyBlockChange(Level world, BlockPos changed);

    List<NodeConnection<IPrismTransmissionNode>> queryNext(WorldNetworkHandler handler);

    List<BlockPos> getSources();

    default boolean needsUpdate() {
        return false;
    }

    default void update(Level world) {}

    default void postLoad(LevelAccessor world) {}

    default void markDirty(Level world) {
        WorldNetworkHandler.getNetworkHandler(world).markDirty(getLocationPos());
    }

    default boolean providesAutoLink() {
        return true;
    }

    TransmissionProvider getProvider();

    void readFromNBT(CompoundTag compound);

    void writeToNBT(CompoundTag compound);
}
