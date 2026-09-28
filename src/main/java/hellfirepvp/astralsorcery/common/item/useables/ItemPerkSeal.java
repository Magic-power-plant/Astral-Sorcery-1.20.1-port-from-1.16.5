package hellfirepvp.astralsorcery.common.item.useables;

import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Collection;

public class ItemPerkSeal extends Item {

    public ItemPerkSeal(Properties properties) {
        super(properties);
    }

    public static int getPlayerSealCount(Player player) {
        return getPlayerSealCount(player.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null));
    }

    public static int getPlayerSealCount(IItemHandler inventory) {
        if (inventory == null) {
            return 0;
        }
        Collection<ItemStack> seals = ItemUtils.findItemsInInventory(inventory,
                new ItemStack(ItemsAS.PERK_SEAL.get()), false);
        return seals.stream().mapToInt(ItemStack::getCount).sum();
    }

    public static boolean useSeal(Player player, boolean simulate) {
        return player.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .filter(IItemHandlerModifiable.class::isInstance)
                .map(IItemHandlerModifiable.class::cast)
                .map(inventory -> useSeal(inventory, simulate))
                .orElse(false);
    }

    public static boolean useSeal(IItemHandlerModifiable inventory, boolean simulate) {
        if (inventory == null) {
            return false;
        }
        return ItemUtils.consumeFromInventory(inventory, new ItemStack(ItemsAS.PERK_SEAL.get()), simulate);
    }
}
