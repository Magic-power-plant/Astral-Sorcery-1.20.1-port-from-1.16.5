package hellfirepvp.astralsorcery.common.container.slot;

import hellfirepvp.astralsorcery.common.container.ContainerTome;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotConstellationPaper extends SlotItemHandler {

    private final ContainerTome listener;

    public SlotConstellationPaper(ContainerTome tome, IItemHandler inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        this.listener = tome;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() &&
                stack.getItem() instanceof ItemConstellationPaper &&
                ((ItemConstellationPaper) stack.getItem()).getConstellation(stack) != null;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        listener.slotChanged();
    }
}
