/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.treebeacon;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.base.FakedStateBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TreeBeaconComponentBlockEntity
 * Created by HellFirePvP
 * Date: 04.09.2020 / 19:37
 */
public class TreeBeaconComponentBlockEntity extends FakedStateBlockEntity {

    private BlockPos treeBeaconPos = BlockPos.ZERO;

    public TreeBeaconComponentBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.TREE_BEACON_COMPONENT.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        //1.16.5: getWorld().isRemote() -> 1.20.1: getLevel().isClientSide()
        if (!this.getLevel().isClientSide() && this.getTicksExisted() % 200 == 0) {
            if (this.getTreeBeaconPos().equals(BlockPos.ZERO)) {
                this.removeSelf();
            } else {
                TreeBeaconBlockEntity ttb = MiscUtils.getBlockEntityAt(this.getLevel(), this.getTreeBeaconPos(), TreeBeaconBlockEntity.class, false);
                if (ttb == null) {
                    this.removeSelf();
                }
            }
        }
    }

    @Nonnull
    public BlockPos getTreeBeaconPos() {
        return treeBeaconPos;
    }

    public void setTreeBeaconPos(BlockPos treeBeaconPos) {
        this.treeBeaconPos = treeBeaconPos;
        this.markForUpdate();
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.treeBeaconPos = NBTHelper.readFromSubTag(compound, "treeBeaconPos", NBTHelper::readBlockPosFromNBT);
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        NBTHelper.setAsSubTag(compound, "treeBeaconPos", tag -> NBTHelper.writeBlockPosToNBT(this.treeBeaconPos, tag));
    }
}
