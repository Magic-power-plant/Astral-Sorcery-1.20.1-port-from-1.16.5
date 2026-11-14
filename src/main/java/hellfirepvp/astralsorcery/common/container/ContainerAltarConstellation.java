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

// Reference: 1.16.5 ContainerAltarConstellation.
public class ContainerAltarConstellation extends ContainerAltarBase {

    public ContainerAltarConstellation(int id, Inventory inv, AltarBlockEntity altar) {
        super(MenuTypesAS.ALTAR_CONSTELLATION.get(), id, inv, altar);
    }

    public ContainerAltarConstellation(int id, Inventory inv, FriendlyByteBuf buffer) {
        this(id, inv, getAltar(inv, buffer.readBlockPos()));
    }

    @Override
    void bindPlayerInventory(Inventory plInventory) {
        bindPlayerInventory(plInventory, 48, 120);
    }

    @Override
    void bindAltarInventory(IItemHandler altarInventory) {
        addSlot(new SlotItemHandler(altarInventory,  0, 84,  11));
        addSlot(new SlotItemHandler(altarInventory,  1, 102,  11));
        addSlot(new SlotItemHandler(altarInventory,  3, 138,  11));
        addSlot(new SlotItemHandler(altarInventory,  4, 156, 11));
        addSlot(new SlotItemHandler(altarInventory,  5,  84,  29));
        for (int xx = 0; xx < 3; xx++) {
            addSlot(new SlotItemHandler(altarInventory,  6 + xx, 102 + xx * 18, 29));
        }
        addSlot(new SlotItemHandler(altarInventory,  9, 156,  29));
        for (int xx = 0; xx < 3; xx++) {
            addSlot(new SlotItemHandler(altarInventory, 11 + xx, 102 + xx * 18, 47));
        }
        addSlot(new SlotItemHandler(altarInventory, 15, 84,   65));
        for (int xx = 0; xx < 3; xx++) {
            addSlot(new SlotItemHandler(altarInventory, 16 + xx, 102 + xx * 18, 65));
        }
        addSlot(new SlotItemHandler(altarInventory, 19, 156,  65));
        addSlot(new SlotItemHandler(altarInventory, 20, 84,  83));
        addSlot(new SlotItemHandler(altarInventory, 21, 102,  83));
        addSlot(new SlotItemHandler(altarInventory, 23, 138,  83));
        addSlot(new SlotItemHandler(altarInventory, 24, 156, 83));
    }

    @Override
    Optional<ItemStack> handleCustomTransfer(Player player, int index) {
        return Optional.empty();
    }

    @Override
    public int translateIndex(int fromIndex) {
        if (fromIndex >= 23) {
            return fromIndex - 4;
        }
        if (fromIndex >= 15) {
            return fromIndex - 3;
        }
        if (fromIndex >= 11) {
            return fromIndex - 2;
        }
        if (fromIndex >= 3) {
            return fromIndex - 1;
        }
        return fromIndex;
    }
}
