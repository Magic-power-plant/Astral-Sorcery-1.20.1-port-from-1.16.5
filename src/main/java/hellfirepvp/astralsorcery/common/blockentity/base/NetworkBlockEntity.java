/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.base;

import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionNetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: NetworkBlockEntity
 * Created by HellFirePvP
 * Date: 03.08.2016 / 18:12
 */
public abstract class NetworkBlockEntity<T extends IPrismTransmissionNode> extends TickableBlockEntity {

    protected static final Random rand = new Random();
    private boolean isNetworkInformed = false;

    private T cachedNetworkNode = null;
    private boolean needsNetworkSync = false;

    protected NetworkBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Nullable
    public T getNetworkNode() {
        if (cachedNetworkNode != null) {
            if (!cachedNetworkNode.getLocationPos().equals(getBlockPos())) {
                cachedNetworkNode = null;
            }
        }
        if (cachedNetworkNode == null) {
            cachedNetworkNode = resolveNode();
        }
        return cachedNetworkNode;
    }

    @Nullable
    private T resolveNode() {
        IPrismTransmissionNode node = WorldNetworkHandler.getNetworkHandler(getLevel()).getTransmissionNode(getBlockPos());
        if (node == null) {
            return null;
        }
        return (T) node;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getLevel().isClientSide()) {
            if (!this.isNetworkInformed) {
                if (!TransmissionNetworkHelper.isBlockEntityInNetwork(this)) {
                    TransmissionNetworkHelper.informNetworkBlockEntityPlacement(this);
                }
                this.isNetworkInformed = true;
            }

            if (this.needsNetworkSync) {
                this.doNetworkSync();
            }
        }
    }

    protected void doNetworkSync() {
        T networkNode = this.getNetworkNode();
        if (networkNode != null && networkNode.updateFromBlockEntity(this)) {
            this.needsNetworkSync = false;
            this.markForUpdate();
            this.preventNetworkSync();
        }
    }

    @Override
    public void markForUpdate() {
        super.markForUpdate();
        this.needsNetworkSync = true;
    }

    protected void preventNetworkSync() {
        this.needsNetworkSync = false;
    }

    public boolean needsNetworkSync() {
        return needsNetworkSync;
    }

    public void onBreak() {}

    @Override
    // 1.16.5: TileEntity#remove()
    public void setRemoved() {
        super.setRemoved();

        if (this.getLevel() == null || this.getLevel().isClientSide()) {
            return;
        }
        TransmissionNetworkHelper.informNetworkBlockEntityRemoval(this);
        this.isNetworkInformed = false;
    }

    @Override
    protected void writeSaveNBT(CompoundTag compound) {
        super.writeSaveNBT(compound);

        compound.putBoolean("needsNetworkSync", this.needsNetworkSync);
    }

    @Override
    protected void readSaveNBT(CompoundTag compound) {
        super.readSaveNBT(compound);

        this.needsNetworkSync = compound.getBoolean("needsNetworkSync");
    }
}
