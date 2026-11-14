/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.observation;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.base.SynchronizedBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.capability.NamedInventoryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TelescopeBlockEntity
 * Created by HellFirePvP
 * Date: 15.01.2020 / 15:37
 */
public class TelescopeBlockEntity extends SynchronizedBlockEntity implements NamedInventoryBlockEntity {

    private TelescopeRotation rotation = TelescopeRotation.N;

    public TelescopeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.TELESCOPE.get(), pos, state);
    }

    public TelescopeRotation getRotation() {
        return rotation;
    }

    public void setRotation(TelescopeRotation rotation) {
        this.rotation = rotation;
        markForUpdate();
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.rotation = TelescopeRotation.values()[compound.getInt("rotation")];
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        compound.putInt("rotation", rotation.ordinal());
    }

    @Override
    //1.16.5: ITextComponent + new TranslationTextComponent(...) -> 1.20.1: Component.translatable(...)
    public Component getDisplayName() {
        return Component.translatable("screen.astralsorcery.telescope");
    }

    public static enum TelescopeRotation {

        N,
        N_E,
        E,
        S_E,
        S,
        S_W,
        W,
        N_W;

        public TelescopeRotation nextClockWise() {
            return values()[(ordinal() + 1) % values().length];
        }

        public TelescopeRotation nextCounterClockWise() {
            return values()[(ordinal() + 7) % values().length];
        }

    }
}
