package hellfirepvp.astralsorcery.common.util.blockentity;

import hellfirepvp.astralsorcery.api.sync.SyncTarget;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FilteredBlockEntityInventory extends BlockEntityInventory {

    private InputFilter inputFilter = null;
    private ExtractFilter extractFilter = null;

    public FilteredBlockEntityInventory(@Nonnull SyncTarget blockEntity,
                                 @Nonnull Supplier<Integer> slotCountProvider,
                                 Direction... applicableSides) {
        super(blockEntity, slotCountProvider, applicableSides);
    }

    public FilteredBlockEntityInventory(@Nonnull SyncTarget blockEntity,
                                 @Nonnull Supplier<Integer> slotCountProvider,
                                 @Nullable Consumer<Integer> changeListener,
                                 Direction... applicableSides) {
        super(blockEntity, slotCountProvider, changeListener, applicableSides);
    }

    protected FilteredBlockEntityInventory(@Nonnull SyncTarget blockEntity,
                                    @Nonnull Supplier<Integer> slotCountProvider,
                                    @Nullable Consumer<Integer> changeListener,
                                    @Nonnull Collection<Direction> applicableSides,
                                    @Nonnull BiFunction<Integer, ItemStack, Integer> stackSizeLimiter) {
        super(blockEntity, slotCountProvider, changeListener, applicableSides, stackSizeLimiter);
    }

    public FilteredBlockEntityInventory canInsert(InputFilter filter) {
        this.inputFilter = filter;
        return this;
    }

    public FilteredBlockEntityInventory canExtract(ExtractFilter filter) {
        this.extractFilter = filter;
        return this;
    }

    @Override
    protected FilteredBlockEntityInventory makeNewInstance() {
        FilteredBlockEntityInventory inv = new FilteredBlockEntityInventory(this.blockEntity, this.slotCountProvider,
                this.changeListener, MiscUtils.copySet(this.applicableSides), this.stackSizeLimiter);
        inv.canInsert(this.inputFilter);
        inv.canExtract(this.extractFilter);
        return inv;
    }

    @Nonnull
    @Override
    public FilteredBlockEntityInventory deserialize(CompoundTag tag) {
        return (FilteredBlockEntityInventory) super.deserialize(tag);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!canInsertItem(slot, stack, getStackInSlot(slot))) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!canExtractItem(slot, amount, getStackInSlot(slot))) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }

    public boolean canInsertItem(int slot, ItemStack toAdd) {
        return this.canInsertItem(slot, toAdd, this.getStackInSlot(slot));
    }

    private boolean canInsertItem(int slot, ItemStack toAdd, @Nonnull ItemStack existing) {
        return inputFilter == null || inputFilter.canInsert(slot, toAdd, existing);
    }

    public boolean canExtractItem(int slot, int amount) {
        return this.canExtractItem(slot, amount, this.getStackInSlot(slot));
    }

    private boolean canExtractItem(int slot, int amount, @Nonnull ItemStack existing) {
        return extractFilter == null || extractFilter.canExtract(slot, amount, existing);
    }

    public static interface InputFilter {

        public boolean canInsert(int slot, ItemStack toAdd, @Nonnull ItemStack existing);

    }

    public static interface ExtractFilter {

        public boolean canExtract(int slot, int amount, @Nonnull ItemStack existing);

    }
}
