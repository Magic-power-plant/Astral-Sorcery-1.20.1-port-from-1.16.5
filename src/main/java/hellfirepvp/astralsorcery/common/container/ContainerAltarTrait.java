package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.container.slot.SlotConstellationFocus;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.item.base.IConstellationFocus;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Optional;

// Reference: 1.16.5 ContainerAltarTrait.
// Slot layout (inherited from ContainerAltarBase): the 36 player slots (0..35), then the 25 grid
// slots (36..60), then the focus slot (61).
public class ContainerAltarTrait extends ContainerAltarBase {

    private SlotConstellationFocus focusSlot;

    public ContainerAltarTrait(int id, Inventory inv, AltarBlockEntity altar) {
        super(MenuTypesAS.ALTAR_RADIANCE.get(), id, inv, altar);
    }

    public ContainerAltarTrait(int id, Inventory inv, FriendlyByteBuf buffer) {
        this(id, inv, getAltar(inv, buffer.readBlockPos()));
    }

    @Override
    void bindPlayerInventory(Inventory plInventory) {
        bindPlayerInventory(plInventory, 48, 120);
    }

    @Override
    void bindAltarInventory(IItemHandler altarInventory) {
        for (int yy = 0; yy < AltarRecipeGrid.GRID_SIZE; yy++) {
            for (int xx = 0; xx < AltarRecipeGrid.GRID_SIZE; xx++) {
                addSlot(new SlotItemHandler(altarInventory, xx + yy * AltarRecipeGrid.GRID_SIZE, 84 + xx * 18, 11 + yy * 18));
            }
        }

        this.focusSlot = new SlotConstellationFocus(altarInventory, this.getBlockEntity(), 35, 11);
        addSlot(this.focusSlot); //Focus item, not accessible from slot index.
    }

    @Override
    Optional<ItemStack> handleCustomTransfer(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            if (index < 36 &&
                    slotStack.getItem() instanceof IConstellationFocus &&
                    ((IConstellationFocus) slotStack.getItem()).getFocusConstellation(slotStack) != null) {
                // 1.16.5 used focusSlot.slotNumber. The 1.20.1 equivalent is Slot#index, but Forge's
                // SlotItemHandler declares a private `index` of its own that shadows it, so the cast is required.
                int focusSlotNumber = ((Slot) this.focusSlot).index;
                if (this.moveItemStackTo(slotStack, focusSlotNumber, focusSlotNumber + 1, false)) {
                    return Optional.of(slotStack);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public int translateIndex(int fromIndex) {
        return fromIndex;
    }
}
