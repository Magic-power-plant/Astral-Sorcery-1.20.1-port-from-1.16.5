/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.util.data.NameUtil;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.base.BlockDynamicColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryBlocks
 * Created by HellFirePvP
 * Date: 31.05.2019 / 21:44
 */
// Port notes for 1.16.5 common/registry/RegistryBlocks.java (170 lines):
//
// As with RegistryItems, the object-creation half moved into a DeferredRegister holder and must not be repeated
// here or every block would be registered twice:
//   registerBlocks()       (1.16.5 ref:62-130)          -> common/lib/BlocksAS (BlocksAS.BLOCKS). Every block id
//                                                          of ref:63-129 exists there; only one was renamed
//                                                          (GATEWAY -> CELESTIAL_GATEWAY). The six invented ids an
//                                                          earlier stage added here (marble, black_marble, altar,
//                                                          collector_crystal, translucent, starmetal_block) are
//                                                          gone again, so the id sets now match 1:1.
//   registerFluidBlocks()  (ref:132-134)                -> common/lib/FluidsAS#LIQUID_STARLIGHT_BLOCK, registered
//                                                          straight into BlocksAS.BLOCKS.
//   makeSlab/makeStairs    (ref:141-153)                -> BlocksAS builds the six slab/stair blocks inline with
//                                                          SlabBlock/StairBlock + the shared *Properties() helpers;
//                                                          Block.Properties.from(Block) became
//                                                          BlockBehaviour.Properties.copy(BlockBehaviour) there.
//   registerBlock(T[, name])(ref:155-169)               -> DeferredRegister#register. NameUtil.fromClass is no
//                                                          longer used: names are explicit strings.
//   ITEM_BLOCKS            (ref:58, 162-164)            -> BlocksAS#registerBlockWithItem registers the BlockItem
//                                                          directly, so the CustomItemBlock hand-off list is gone.
//                                                          See TODO_PORT[AS-REG-09].
//   COLOR_BLOCKS           (ref:57, 165-167)            -> replaced by the registry walk in registerColors below.
//
//TODO_PORT[AS-REG-09]: 1.16.5 RegistryItems#buildItemBlockProperties (common/registry/RegistryItems.java:184-205)
// read hellfirepvp.astralsorcery.common.block.base.CustomItemBlockProperties off every CustomItemBlock and applied
// getItemGroup / canItemBeRepaired / getItemRarity / getItemMaxStackSize / getItemMaxDamage / getContainerItem /
// getItemTEISR / getItemToolLevels to the BlockItem's Item.Properties, and called block.createItemBlock(props).
// BlocksAS#registerBlockWithItem instead hands every BlockItem a bare `new Item.Properties()`, so those per-block
// overrides are currently dropped. Fixing that means changing common/lib/BlocksAS (reported as an external change),
// not this class.
public class RegistryBlocks {

    private RegistryBlocks() {}

    @OnlyIn(Dist.CLIENT)
    public static void registerColors(RegisterColorHandlersEvent.Block event) {
        // 1.16.5: COLOR_BLOCKS.forEach(block -> blockColorEvent.getBlockColors().register(block::getColor, (Block) block));
        // COLOR_BLOCKS was filled while the blocks were being constructed; BlocksAS constructs them now, so the
        // equivalent set is "every block in the astralsorcery namespace that implements BlockDynamicColor".
        List<Block> colorBlocks = ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(AstralSorcery.MODID))
                .map(Map.Entry::getValue)
                .filter(block -> block instanceof BlockDynamicColor)
                .collect(Collectors.toList());
        for (Block block : colorBlocks) {
            event.register(((BlockDynamicColor) block)::getColor, block);
        }
    }
}
