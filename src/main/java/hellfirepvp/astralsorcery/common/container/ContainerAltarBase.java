package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Optional;

// Reference: 1.16.5 ContainerAltarBase.
// Slot order matches 1.16.5 exactly: the 36 player slots first (27 main at 0..26, 9 hotbar at 27..35),
// then the altar slots from 36 upwards. ContainerAltarTrait#handleCustomTransfer relies on this
// (its `index < 36` test identifies player slots).
public abstract class ContainerAltarBase extends ContainerBlockEntity<AltarBlockEntity> {

    private final Inventory playerInv;
    private final IItemHandler invHandler;

    protected ContainerAltarBase(@Nullable MenuType<?> type, int id, Inventory inv, AltarBlockEntity altar) {
        super(type, id, altar);
        this.playerInv = inv;
        this.invHandler = altar.getInventory();

        bindPlayerInventory(this.playerInv);
        bindAltarInventory(this.invHandler);
    }

    abstract void bindPlayerInventory(Inventory plInventory);

    abstract void bindAltarInventory(IItemHandler altarInventory);

    abstract Optional<ItemStack> handleCustomTransfer(Player player, int index);

    //Yes this is not a pretty solution. tell me a better one.
    public abstract int translateIndex(int fromIndex);

    /**
     * Binds the 27 main inventory slots followed by the 9 hotbar slots at the given offsets.
     * 1.16.5 inlined this in every subclass; the coordinates produced are identical
     * (main rows at yOffset, +18, +36 and the hotbar at yOffset + 58).
     */
    protected void bindPlayerInventory(Inventory plInventory, int xOffset, int yOffset) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(plInventory, j + i * 9 + 9, xOffset + j * 18, yOffset + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(plInventory, i, xOffset + i * 18, yOffset + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            Optional<ItemStack> stackOpt = this.handleCustomTransfer(playerIn, index);
            if (stackOpt.isPresent()) {
                return stackOpt.get();
            }

            if (index >= 0 && index < 27) {
                if (!this.moveItemStackTo(slotStack, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 27 && index < 36) {
                if (!this.moveItemStackTo(slotStack, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, slotStack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        AltarBlockEntity altar = this.getBlockEntity();
        if (altar == null) {
            // 1.20.1 only: a client menu built from the network buffer may outlive its block entity.
            return false;
        }
        BlockPos pos = altar.getBlockPos();
        if (MiscUtils.getBlockEntityAt(altar.getLevel(), pos, BlockEntity.class, false) != altar) {
            return false;
        } else {
            return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    protected static AltarBlockEntity getAltar(Inventory inv, BlockPos pos) {
        AltarBlockEntity altar = MiscUtils.getBlockEntityAt(inv.player.level(), pos, AltarBlockEntity.class, true);
        if (altar == null) {
            throw new IllegalStateException("Missing altar at " + pos);
        }
        return altar;
    }
}
