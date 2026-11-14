package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Optional;

// Reference: 1.16.5 ContainerAltarDiscovery.
public class ContainerAltarDiscovery extends ContainerAltarBase {

    public ContainerAltarDiscovery(int id, Inventory inv, AltarBlockEntity altar) {
        super(MenuTypesAS.ALTAR_DISCOVERY.get(), id, inv, altar);
    }

    public ContainerAltarDiscovery(int id, Inventory inv, FriendlyByteBuf buffer) {
        this(id, inv, getAltar(inv, buffer.readBlockPos()));
    }

    @Override
    void bindPlayerInventory(Inventory plInventory) {
        bindPlayerInventory(plInventory, 8, 84);
    }

    @Override
    void bindAltarInventory(IItemHandler altarInventory) {
        for (int xx = 0; xx < 3; xx++) {
            addSlot(new SlotItemHandler(altarInventory,  6 + xx, 62 + xx * 18, 11));
        }
        for (int xx = 0; xx < 3; xx++) {
            addSlot(new SlotItemHandler(altarInventory, 11 + xx, 62 + xx * 18, 29));
        }
        for (int xx = 0; xx < 3; xx++) {
            addSlot(new SlotItemHandler(altarInventory, 16 + xx, 62 + xx * 18, 47));
        }
    }

    @Override
    Optional<ItemStack> handleCustomTransfer(Player player, int index) {
        return Optional.empty();
    }

    @Override
    public int translateIndex(int fromIndex) {
        if (fromIndex >= 16) {
            return fromIndex - 10;
        }
        if (fromIndex >= 11) {
            return fromIndex - 8;
        }
        return fromIndex - 6;
    }
}
