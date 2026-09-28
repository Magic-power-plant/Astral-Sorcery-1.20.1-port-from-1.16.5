package hellfirepvp.astralsorcery.common.container.slot;

import hellfirepvp.astralsorcery.common.item.base.IConstellationFocus;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

// Reference: 1.16.5 SlotConstellationFocus. The slot is backed by TileAltar#getFocusItem, not by handler slot 100.
public class SlotConstellationFocus extends SlotItemHandler {

    private final TileAltar altar;

    public SlotConstellationFocus(IItemHandler itemHandler, TileAltar altar, int xPosition, int yPosition) {
        super(itemHandler, 100, xPosition, yPosition);
        this.altar = altar;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IConstellationFocus && ((IConstellationFocus) stack.getItem()).getFocusConstellation(stack) != null;
    }

    @Override
    @Nonnull
    public ItemStack getItem() {
        return this.altar.getFocusItem();
    }

    @Override
    public void set(@Nonnull ItemStack stack) {
        this.altar.setFocusItem(stack);
    }

    @Override
    public void initialize(@Nonnull ItemStack stack) {
        // SlotItemHandler#initialize writes to handler slot 100 (out of range); the focus item lives on the altar.
        this.altar.setFocusItem(stack);
        this.setChanged();
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return true;
    }

    @Override
    public void onTake(Player thePlayer, @Nonnull ItemStack stack) {
        this.altar.markForUpdate();
        super.onTake(thePlayer, stack);
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        ItemStack focus = this.altar.getFocusItem();
        this.altar.setFocusItem(ItemStack.EMPTY);
        return focus;
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return false;
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return 1;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}
