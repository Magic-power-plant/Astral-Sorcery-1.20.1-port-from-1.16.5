package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileObservatory;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerObservatory extends ContainerTileEntity<TileObservatory> {

    public ContainerObservatory(TileObservatory observatory, int windowId) {
        super(MenuTypesAS.OBSERVATORY.get(), windowId, observatory);
    }

    public ContainerObservatory(int id, Inventory inv, FriendlyByteBuf buffer) {
        this(getObservatory(inv, ByteBufUtils.readPos(buffer)), id);
    }

    private static TileObservatory getObservatory(Inventory inv, BlockPos pos) {
        return MiscUtils.getTileAt(inv.player.level(), pos, TileObservatory.class, true);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
