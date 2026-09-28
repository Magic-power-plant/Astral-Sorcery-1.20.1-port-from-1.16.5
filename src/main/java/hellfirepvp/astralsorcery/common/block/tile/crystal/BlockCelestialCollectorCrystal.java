/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.tile.crystal;

import hellfirepvp.astralsorcery.common.block.BlockCollectorCrystal;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockCelestialCollectorCrystal
 * Created by HellFirePvP
 * Date: 10.08.2019 / 23:04
 */
//1.16.5 additionally overrides
//    public Class<? extends ItemBlockCollectorCrystal> getItemBlockClass() { return ItemBlockCelestialCollectorCrystal.class; }
//(1.16.5 BlockCelestialCollectorCrystal.java:28-31). That hook only existed so the 1.16.5 registry could
//reflectively instantiate the item form; 1.20.1's DeferredRegister takes the item factory explicitly, and this
//tree's BlocksAS#CELESTIAL_COLLECTOR_CRYSTAL already passes
//`b -> new ItemBlockCelestialCollectorCrystal(b, new Item.Properties())`. getItemBlockClass() is therefore absent
//tree-wide - see the note on common/block/base/CustomItemBlock.java.
public class BlockCelestialCollectorCrystal extends BlockCollectorCrystal {

    public BlockCelestialCollectorCrystal() {
        super(CollectorCrystalType.CELESTIAL_CRYSTAL);
    }
}
