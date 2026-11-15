/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiType
 * Created by HellFirePvP
 * Date: 03.08.2019 / 11:26
 */
public enum GuiType {

    CONSTELLATION_PAPER,
    TOME,
    REFRACTION_TABLE,
    TELESCOPE,
    HAND_TELESCOPE;

    //1.16.5: CompoundNBT -> 1.20.1: CompoundTag
    public CompoundTag serializeArguments(Object[] data) {
        try {
            CompoundTag nbt = new CompoundTag();
            switch (this) {
                case CONSTELLATION_PAPER:
                    nbt.putString("cst", ((IConstellation) data[0]).getRegistryName().toString());
                    break;
                case REFRACTION_TABLE:
                case TELESCOPE:
                    NBTHelper.writeBlockPosToNBT((BlockPos) data[0], nbt);
                    break;
                default:
                    break;
            }
            return nbt;
        } catch (Exception exc) {
            throw new IllegalArgumentException("Invalid Arguments for GuiType: " + this.name(), exc);
        }
    }

}
