package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Optional;

// Reference: 1.16.5 ContainerAltarAttunement.
public class ContainerAltarAttunement extends ContainerAltarBase {

    public ContainerAltarAttunement(int id, Inventory inv, TileAltar altar) {
        super(MenuTypesAS.ALTAR_ATTUNEMENT.get(), id, inv, altar);
    }

    public ContainerAltarAttunement(int id, Inventory inv, FriendlyByteBuf buffer) {
        this(id, inv, getAltar(inv, buffer.readBlockPos()));
    }

    @Override
    void bindPlayerInventory(Inventory plInventory) {
        bindPlayerInventory(plInventory, 48, 120);
    }

    @Override
    void bindAltarInventory(IItemHandler altarInventory) {
        addSlot(new SlotItemHandler(altarInventory,  0, 84,  11));
        addSlot(new SlotItemHandler(altarInventory,  4, 156, 11));
        for (int xx = 0; xx < 3; xx++) {
            addSlot(new SlotItemHandler(altarInventory,  6 + xx, 102 + xx * 18, 29));
        }
        for (int xx = 0; xx < 3; xx++) {
            addSlot(new SlotItemHandler(altarInventory, 11 + xx, 102 + xx * 18, 47));
        }
        for (int xx = 0; xx < 3; xx++) {
            addSlot(new SlotItemHandler(altarInventory, 16 + xx, 102 + xx * 18, 65));
        }
        addSlot(new SlotItemHandler(altarInventory, 20, 84,  83));
        addSlot(new SlotItemHandler(altarInventory, 24, 156, 83));
    }

    @Override
    Optional<ItemStack> handleCustomTransfer(Player player, int index) {
        return Optional.empty();
    }

    @Override
    public int translateIndex(int fromIndex) {
        if (fromIndex == 24) {
            return 12;
        }
        if (fromIndex >= 20) {
            return 11;
        }
        if (fromIndex >= 16) {
            return fromIndex - 8;
        }
        if (fromIndex >= 11) {
            return fromIndex - 6;
        }
        if (fromIndex >= 6) {
            return fromIndex - 4;
        }
        if (fromIndex >= 4) {
            return 1;
        }
        return 0;
    }
}
