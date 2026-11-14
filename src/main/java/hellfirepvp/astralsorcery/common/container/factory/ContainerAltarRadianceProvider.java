/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container.factory;

import hellfirepvp.astralsorcery.common.container.ContainerAltarTrait;
import hellfirepvp.astralsorcery.common.lib.ContainerTypesAS;
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
public class ContainerAltarRadianceProvider extends CustomContainerProvider<ContainerAltarTrait> {

    private final AltarBlockEntity ta;

    public ContainerAltarRadianceProvider(AltarBlockEntity ta) {
        super(ContainerTypesAS.ALTAR_RADIANCE.get());
        this.ta = ta;
    }

    @Override
    protected void writeExtraData(FriendlyByteBuf buf) {
        ByteBufUtils.writePos(buf, this.ta.getBlockPos());
    }

    @Nonnull
    @Override
    public ContainerAltarTrait createMenu(int id, Inventory plInventory, Player player) {
        return new ContainerAltarTrait(id, plInventory, ta);
    }

    private static ContainerAltarTrait createFromPacket(int id, Inventory plInventory, FriendlyByteBuf data) {
        BlockPos at = ByteBufUtils.readPos(data);
        Player player = plInventory.player;
        AltarBlockEntity ta = MiscUtils.getBlockEntityAt(player.level(), at, AltarBlockEntity.class, true);
        return new ContainerAltarTrait(id, plInventory, ta);
    }

    public static class Factory implements IContainerFactory<ContainerAltarTrait> {

        @Override
        public ContainerAltarTrait create(int windowId, Inventory inv, FriendlyByteBuf data) {
            return ContainerAltarRadianceProvider.createFromPacket(windowId, inv, data);
        }
    }
}
