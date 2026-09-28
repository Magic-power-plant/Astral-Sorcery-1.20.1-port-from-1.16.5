package hellfirepvp.astralsorcery.common.block.base;

import hellfirepvp.astralsorcery.common.item.block.ItemBlockCustom;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

// Reference: 1.16.5 CustomItemBlock.
// 1.16.5 paired getItemBlockClass() with a reflective createItemBlock() that the registry called to
// build each block's item form. 1.20.1's DeferredRegister needs an explicit item supplier anyway, so
// BlocksAS#registerBlockWithItem takes the factory directly (e.g. `b -> new ItemBlockLens(b, props)`)
// and getItemBlockClass()/reflection are gone. Every 1.16.5 override has a matching BlocksAS factory:
// BlockLens, BlockPrism, BlockCelestialCrystalCluster, BlockGemCrystalCluster,
// BlockRockCollectorCrystal, BlockCelestialCollectorCrystal.
//
// createItemBlock() is therefore unused here, but it is kept to mirror the reference default and now
// returns ItemBlockCustom, exactly as 1.16.5's default getItemBlockClass() did.
public interface CustomItemBlock {

    default BlockItem createItemBlock(Item.Properties properties) {
        return new ItemBlockCustom((Block) this, properties);
    }

}
