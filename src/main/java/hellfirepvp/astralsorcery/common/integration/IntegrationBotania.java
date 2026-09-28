/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration;

import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IntegrationBotania
 * Created by Penrif
 * Date: 12.26.2020 / 16:45
 */
//BLOCKED_PORT[AS-INT-04]: 1.16.5 imported vazkii.botania.api.item.IBlockProvider
// (1.16.5 IntegrationBotania.java:20) and both methods below pivot on
// "if (sItem instanceof IBlockProvider)". No Botania artifact is on the compile classpath of this
// port (build.gradle declares no vazkii.botania dependency - the 1.16.5 build.gradle did not declare
// one either, it relied on the API being on the dev classpath), so the interface cannot be named and
// the two provider branches cannot be written. Everything around them - the BlockItem filter, the
// item-handler capability lookup and the slot walk - is ported for real.
// 1.20.1 notes for whoever unblocks this: Botania renamed the interface to
// vazkii.botania.api.item.BlockProvider and exposes it as a capability
// (BotaniaForgeCapabilities.BLOCK_PROVIDER) rather than by "instanceof" on the Item, and the method
// signatures became getBlockCount(Player, ItemStack, ItemStack, Block) /
// provideBlock(Player, ItemStack, ItemStack, Block, boolean) on that capability instance.
public class IntegrationBotania {

    public static Collection<ItemStack> findProvidersProvidingItems(Player player, ItemStack match) {
        List<ItemStack> stacksOut = new LinkedList<>();

        // Botania can only supply blocks, so let's filter that out first.
        if (!(match.getItem() instanceof BlockItem)) {
            return stacksOut;
        }
        Block matchBlock = ((BlockItem) match.getItem()).getBlock();

        // 1.16.5: CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> 1.20.1 ForgeCapabilities.ITEM_HANDLER
        IItemHandler handler = player.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(ItemUtils.EMPTY_INVENTORY);
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            Item sItem = s.getItem();
            //BLOCKED_PORT[AS-INT-04]: 1.16.5 body (1.16.5 IntegrationBotania.java:46-56):
            //   if (sItem instanceof IBlockProvider) {
            //       IBlockProvider provider = (IBlockProvider) sItem;
            //       int blockCount = provider.getBlockCount(player, ItemStack.EMPTY, s, matchBlock);
            //       if (blockCount == -1) {
            //           // Used by rods to indicate infinite.  That doesn't suit our needs, so let's just report a lot.
            //           blockCount = 9001;
            //       }
            //       if (blockCount > 0) {
            //           stacksOut.add(ItemUtils.copyStackWithSize(s, blockCount));
            //       }
            //   }
            // ItemUtils.copyStackWithSize is ported; only IBlockProvider is missing.
        }
        return stacksOut;
    }

    public static boolean consumeFromPlayerInventory(Player player, ItemStack requestingItemStack, ItemStack toConsume, boolean simulate) {
        // Botania can only supply blocks, so let's filter that out first.
        if (!(toConsume.getItem() instanceof BlockItem)) {
            return false;
        }

        Block consumeBlock = ((BlockItem) toConsume.getItem()).getBlock();
        // 1.16.5: CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> 1.20.1 ForgeCapabilities.ITEM_HANDLER
        IItemHandler handler = player.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(ItemUtils.EMPTY_INVENTORY);
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            Item sItem = s.getItem();
            //BLOCKED_PORT[AS-INT-04]: 1.16.5 body (1.16.5 IntegrationBotania.java:72-83):
            //   if (sItem instanceof IBlockProvider) {
            //       IBlockProvider provider = (IBlockProvider) sItem;
            //       int blockCount = provider.getBlockCount(player, requestingItemStack, s, consumeBlock);
            //       if (blockCount == -1 || blockCount > toConsume.getCount()) {
            //           for (int i = 0; i < toConsume.getCount(); i++) {
            //               if (!provider.provideBlock(player, requestingItemStack, s, consumeBlock, !simulate)) {
            //                   return false;
            //               }
            //           }
            //           return true;
            //       }
            //   }
        }
        return false;
    }

}
