package hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import hellfirepvp.astralsorcery.common.blockentity.starlight.LensBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class CrystalTransmissionNode extends SimpleTransmissionNode {

    private CrystalAttributes attributes;
    private float additionalLoss = 1F;

    public CrystalTransmissionNode(BlockPos thisPos, CrystalAttributes attributes) {
        super(thisPos);
        this.attributes = attributes;
    }

    public CrystalTransmissionNode(BlockPos thisPos) {
        super(thisPos);
    }

    public boolean updateAdditionalLoss(float loss) {
        boolean didChange = this.additionalLoss != loss;
        this.additionalLoss = loss;
        return didChange;
    }

    @Override
    public void onTransmissionTick(Level world, float starlightAmt, IWeakConstellation type) {
        LensBlockEntity lens = MiscUtils.getBlockEntityAt(world, getLocationPos(), LensBlockEntity.class, false);
        if (lens != null) {
            lens.transmissionTick(starlightAmt, type);
        }
    }

    @Override
    public float getTransmissionConsumptionMultiplier() {
        return additionalLoss;
    }

    @Override
    public boolean needsTransmissionUpdate() {
        return true;
    }

    public CrystalAttributes getTransmissionProperties() {
        return attributes;
    }

    @Override
    public TransmissionProvider getProvider() {
        return new Provider();
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);

        this.attributes = CrystalAttributes.getCrystalAttributes(compound);
        this.additionalLoss = compound.getFloat("lossMultiplier");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        super.writeToNBT(compound);

        if (this.attributes != null) {
            this.attributes.store(compound);
        }
        compound.putFloat("lossMultiplier", this.additionalLoss);
    }

    public static class Provider extends TransmissionProvider {

        @Override
        public IPrismTransmissionNode get() {
            return new CrystalTransmissionNode(null);
        }

    }
}
