package hellfirepvp.astralsorcery.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ItemStackHandlerAS extends ItemStackHandler {
    public ItemStackHandlerAS(int size) {
        super(size);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);
        ItemStack existing = this.stacks.get(slot);
        if (existing.isEmpty()) {
            if (!simulate) {
                this.stacks.set(slot, stack.copy());
                onContentsChanged(slot);
            }
            return ItemStack.EMPTY;
        }
        int space = Math.min(existing.getMaxStackSize() - existing.getCount(), stack.getCount());
        if (space > 0 && !simulate) {
            existing.grow(space);
            onContentsChanged(slot);
        }
        return new ItemStack(stack.getItem(), stack.getCount() - space);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);
        ItemStack existing = this.stacks.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getCount());
        if (!simulate) {
            this.stacks.set(slot, new ItemStack(existing.getItem(), existing.getCount() - toExtract));
            onContentsChanged(slot);
        }
        return new ItemStack(existing.getItem(), toExtract);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < this.stacks.size(); i++) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("Slot", i);
            this.stacks.get(i).save(itemTag);
            list.add(itemTag);
        }
        tag.put("Items", list);
        tag.putInt("Size", this.stacks.size());
        return tag;
    }

    //ItemStackHandler#stacks is a NonNullList created by NonNullList.withSize(..), which is backed by a
    //fixed-size list: clear() only refills it with the default value and add() throws
    //UnsupportedOperationException out of AbstractList. Growing it by hand therefore blew up on every read,
    //which took down the altar, the attunement altar and the spectral relay - TileAltar#updateType calls
    //readCustomNBT, so even getBlockEntity() on a placed altar threw:
    //    UnsupportedOperationException
    //      at java.util.AbstractList.add(AbstractList.java:153)
    //      at net.minecraft.core.NonNullList.add(NonNullList.java:61)
    //      at ItemStackHandlerAS.deserializeNBT(ItemStackHandlerAS.java:79)
    //      at TileAltar.readCustomNBT(TileAltar.java:553)
    //      at TileAltar.updateType(TileAltar.java:525)
    //observed in game. setSize(int) is the accessor Forge provides for exactly this: it REPLACES the list with
    //a fresh NonNullList of the requested size, which is what ItemStackHandler#deserializeNBT itself does.
    @Override
    public void deserializeNBT(@Nonnull CompoundTag nbt) {
        ListTag list = nbt.getList("Items", Tag.TAG_COMPOUND);
        this.setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : this.stacks.size());
        for (int j = 0; j < list.size(); j++) {
            CompoundTag tag = list.getCompound(j);
            int slot = tag.getInt("Slot");
            if (slot >= 0 && slot < this.stacks.size()) {
                this.stacks.set(slot, ItemStack.of(tag));
            }
        }
        onLoad();
    }
}