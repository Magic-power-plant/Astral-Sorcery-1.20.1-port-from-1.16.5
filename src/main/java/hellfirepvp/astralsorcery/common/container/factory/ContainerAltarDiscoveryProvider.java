/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container.factory;

import hellfirepvp.astralsorcery.common.container.ContainerAltarDiscovery;
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
public class ContainerAltarDiscoveryProvider extends CustomContainerProvider<ContainerAltarDiscovery> {

    private final AltarBlockEntity ta;

    public ContainerAltarDiscoveryProvider(AltarBlockEntity ta) {
        super(MenuTypesAS.ALTAR_DISCOVERY.get());
        this.ta = ta;
    }

    @Override
    protected void writeExtraData(FriendlyByteBuf buf) {
        // 1.16.5 TileEntity#getPos -> 1.20.1 BlockEntity#getBlockPos
        ByteBufUtils.writePos(buf, this.ta.getBlockPos());
    }

    @Nonnull
    @Override
    public ContainerAltarDiscovery createMenu(int id, Inventory plInventory, Player player) {
        //1.16.5 constructor order is (TileAltar, PlayerInventory, int); the ported container takes
        //(int, Inventory, TileAltar) to line up with vanilla's AbstractContainerMenu conventions.
        return new ContainerAltarDiscovery(id, plInventory, ta);
    }

    private static ContainerAltarDiscovery createFromPacket(int id, Inventory plInventory, FriendlyByteBuf data) {
        BlockPos at = ByteBufUtils.readPos(data);
        Player player = plInventory.player;
        // 1.16.5 Entity#getEntityWorld -> 1.20.1 Entity#level
        AltarBlockEntity ta = MiscUtils.getBlockEntityAt(player.level(), at, AltarBlockEntity.class, true);
        return new ContainerAltarDiscovery(id, plInventory, ta);
    }

    public static class Factory implements IContainerFactory<ContainerAltarDiscovery> {

        @Override
        public ContainerAltarDiscovery create(int windowId, Inventory inv, FriendlyByteBuf data) {
            return ContainerAltarDiscoveryProvider.createFromPacket(windowId, inv, data);
        }
    }
}
