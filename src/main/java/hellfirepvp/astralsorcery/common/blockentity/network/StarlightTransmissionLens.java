package hellfirepvp.astralsorcery.common.blockentity.network;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.item.lens.LensColorType;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightTransmissionHandler;
import hellfirepvp.astralsorcery.common.starlight.network.TransmissionWorldHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.CrystalTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import hellfirepvp.astralsorcery.common.blockentity.LensBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StarlightTransmissionLens extends CrystalTransmissionNode {

    public StarlightTransmissionLens(BlockPos thisPos, CrystalAttributes attributes) {
        super(thisPos, attributes);
    }

    public StarlightTransmissionLens(BlockPos thisPos) {
        super(thisPos);
    }

    @Override
    public <T extends BlockEntity> boolean updateFromBlockEntity(T blockEntity) {
        if (!(blockEntity instanceof LensBlockEntity)) {
            return super.updateFromBlockEntity(blockEntity);
        }

        LensColorType colorType = ((LensBlockEntity) blockEntity).getColorType();
        if (this.updateAdditionalLoss(colorType == null ? 0 : colorType.getFlowMultiplier())) {
            TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(blockEntity.getLevel());
            if (handle != null) {
                handle.notifyTransmissionNodeChange(this);
            }
        }
        this.updateIgnoreBlockCollisionState(blockEntity.getLevel(), colorType != null && colorType.doesIgnoreBlockCollision());
        return true;
    }

    @Override
    public TransmissionProvider getProvider() {
        return new Provider();
    }

    public static class Provider extends TransmissionProvider {

        @Override
        public IPrismTransmissionNode get() {
            return new StarlightTransmissionLens(null);
        }

    }
}
