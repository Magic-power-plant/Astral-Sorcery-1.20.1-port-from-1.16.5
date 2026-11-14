package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.ObservatoryBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerObservatory extends ContainerBlockEntity<ObservatoryBlockEntity> {

    public ContainerObservatory(ObservatoryBlockEntity observatory, int windowId) {
        super(MenuTypesAS.OBSERVATORY.get(), windowId, observatory);
    }

    public ContainerObservatory(int id, Inventory inv, FriendlyByteBuf buffer) {
        this(getObservatory(inv, ByteBufUtils.readPos(buffer)), id);
    }

    private static ObservatoryBlockEntity getObservatory(Inventory inv, BlockPos pos) {
        return MiscUtils.getBlockEntityAt(inv.player.level(), pos, ObservatoryBlockEntity.class, true);
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
