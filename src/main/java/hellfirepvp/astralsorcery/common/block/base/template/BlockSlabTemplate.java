/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base.template;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockSlabTemplate
 * Created by HellFirePvP
 * Date: 21.07.2019 / 10:36
 */
public class BlockSlabTemplate extends SlabBlock implements CustomItemBlock {

    //TODO_PORT[AS-BLK-13]: 1.16.5 keeps this field to forward the base block's harvest data to the slab block:
    //  @Nullable @Override public ToolType getHarvestTool(BlockState tool) { return this.baseState.getHarvestTool(); }
    //  @Override public int getHarvestLevel(BlockState state) { return this.baseState.getHarvestLevel(); }
    //(1.16.5 BlockSlabTemplate.java:27-42). Forge deleted ToolType together with IForgeBlock#getHarvestTool /
    //#getHarvestLevel and BlockState#getHarvestTool / #getHarvestLevel in 1.17; harvestability is expressed through
    //the minecraft:mineable/* and forge:needs_*_tool block tags plus Properties#requiresCorrectToolForDrops instead,
    //so the equivalent fix is a tag/datagen change on whichever blocks use this template, not Java here. The field
    //is kept so the reference's constructor contract and state stay intact.
    private final BlockState baseState;

    public BlockSlabTemplate(BlockState baseState, Properties properties) {
        super(properties);
        this.baseState = baseState;
    }
}
