/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.TileFakedState;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileTranslucentBlock
 * Created by HellFirePvP
 * Date: 28.11.2019 / 19:25
 */
public class TileTranslucentBlock extends TileFakedState {

    private UUID playerUUID = null;

    public TileTranslucentBlock(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.TRANSLUCENT_BLOCK.get(), pos, state);
    }

    @Nullable
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.markForUpdate();
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.playerUUID = NBTHelper.getUUID(compound, "playerUUID", null);
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        if (this.playerUUID != null) {
            //1.16.5: CompoundNBT#putUniqueId -> 1.20.1: CompoundTag#putUUID
            compound.putUUID("playerUUID", this.playerUUID);
        }
    }
}
