/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.crystal;

import hellfirepvp.astralsorcery.common.block.BlockCollectorCrystal;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockRockCollectorCrystal
 * Created by HellFirePvP
 * Date: 10.08.2019 / 23:03
 */
//1.16.5 additionally overrides
//    public Class<? extends ItemBlockCollectorCrystal> getItemBlockClass() { return ItemBlockRockCollectorCrystal.class; }
//(1.16.5 BlockRockCollectorCrystal.java:28-31). That hook only existed so the 1.16.5 registry could reflectively
//instantiate the item form; 1.20.1's DeferredRegister takes the item factory explicitly, and this tree's
//BlocksAS#ROCK_COLLECTOR_CRYSTAL already passes `b -> new ItemBlockRockCollectorCrystal(b, new Item.Properties())`.
//getItemBlockClass() is therefore absent tree-wide - see the note on common/block/base/CustomItemBlock.java.
public class BlockRockCollectorCrystal extends BlockCollectorCrystal {

    public BlockRockCollectorCrystal() {
        super(CollectorCrystalType.ROCK_CRYSTAL);
    }
}
