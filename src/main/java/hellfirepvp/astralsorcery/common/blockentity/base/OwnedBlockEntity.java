/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.base;

import hellfirepvp.astralsorcery.common.util.PlayerReference;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: OwnedBlockEntity
 * Created by HellFirePvP
 * Date: 18.10.2020 / 19:05
 */
public interface OwnedBlockEntity {

    /**
     * Updates the owner of this tile entity.
     *
     * @param player the new owning player
     * @return the previous player reference
     */
    //1.16.5: PlayerEntity -> 1.20.1: Player
    @Nullable
    default public PlayerReference setOwner(@Nullable Player player) {
        return this.setOwner(player == null ? null : PlayerReference.of(player));
    }

    /**
     * Updates the owner of this tile entity.
     *
     * @param player the new owning player as reference
     * @return the previous player reference
     */
    @Nullable
    public PlayerReference setOwner(@Nullable PlayerReference player);

    /**
     * Get the current owner UUID
     *
     * @return the current owner UUID or null if no owner is assigned
     */
    @Nullable
    public PlayerReference getOwner();

}
