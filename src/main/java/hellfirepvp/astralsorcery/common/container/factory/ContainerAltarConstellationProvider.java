/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container.factory;

import hellfirepvp.astralsorcery.common.container.ContainerAltarConstellation;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.IContainerFactory;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerAltarDiscoveryProvider
 * Created by HellFirePvP
 * Date: 15.08.2019 / 16:22
 */
public class ContainerAltarConstellationProvider extends CustomContainerProvider<ContainerAltarConstellation> {

    private final AltarBlockEntity ta;

    public ContainerAltarConstellationProvider(AltarBlockEntity ta) {
        super(MenuTypesAS.ALTAR_CONSTELLATION.get());
        this.ta = ta;
    }

    @Override
    protected void writeExtraData(FriendlyByteBuf buf) {
        ByteBufUtils.writePos(buf, this.ta.getBlockPos());
    }

    @Nonnull
    @Override
    public ContainerAltarConstellation createMenu(int id, Inventory plInventory, Player player) {
        return new ContainerAltarConstellation(id, plInventory, ta);
    }

    private static ContainerAltarConstellation createFromPacket(int id, Inventory plInventory, FriendlyByteBuf data) {
        BlockPos at = ByteBufUtils.readPos(data);
        Player player = plInventory.player;
        AltarBlockEntity ta = MiscUtils.getBlockEntityAt(player.level(), at, AltarBlockEntity.class, true);
        return new ContainerAltarConstellation(id, plInventory, ta);
    }

    public static class Factory implements IContainerFactory<ContainerAltarConstellation> {

        @Override
        public ContainerAltarConstellation create(int windowId, Inventory inv, FriendlyByteBuf data) {
            return ContainerAltarConstellationProvider.createFromPacket(windowId, inv, data);
        }
    }
}
