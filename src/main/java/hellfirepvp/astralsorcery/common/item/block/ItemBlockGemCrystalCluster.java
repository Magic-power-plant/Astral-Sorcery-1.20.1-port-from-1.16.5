/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.block;

import hellfirepvp.astralsorcery.common.block.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemBlockGemCrystalCluster
 * Created by HellFirePvP
 * Date: 17.05.2020 / 09:26
 */
public class ItemBlockGemCrystalCluster extends ItemBlockCustom implements CreativeTabItem {

    public ItemBlockGemCrystalCluster(Block block, Properties itemProperties) {
        super(block, itemProperties);
    }

    /**
     * 1.16.5 {@code fillItemGroup} equivalent: one creative entry per growth stage.
     */
    @Override
    public void fillCreativeTab(CreativeModeTab.Output output) {
        this.createCreativeStacks().forEach(output::accept);
    }

    public List<ItemStack> createCreativeStacks() {
        List<ItemStack> items = new ArrayList<>();
        for (BlockGemCrystalCluster.GrowthStageType stage : BlockGemCrystalCluster.STAGE.getPossibleValues()) {
            ItemStack cluster = new ItemStack(this);
            this.setDamage(cluster, stage.ordinal());
            items.add(cluster);
        }
        return items;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState toPlace = super.getPlacementState(context);
        if (toPlace != null) {
            return toPlace.setValue(BlockGemCrystalCluster.STAGE, this.getGrowthStage(context.getItemInHand()));
        }
        return null;
    }

    @Nonnull
    private BlockGemCrystalCluster.GrowthStageType getGrowthStage(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlockGemCrystalCluster)) {
            return BlockGemCrystalCluster.GrowthStageType.STAGE_0;
        }
        return MiscUtils.getEnumEntry(BlockGemCrystalCluster.GrowthStageType.class, this.getDamage(stack));
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        BlockGemCrystalCluster.GrowthStageType stage = this.getGrowthStage(stack);
        switch (stage) {
            case STAGE_2_SKY:
                return super.getDescriptionId(stack) + ".sky";
            case STAGE_2_DAY:
                return super.getDescriptionId(stack) + ".day";
            case STAGE_2_NIGHT:
                return super.getDescriptionId(stack) + ".night";
            default:
                return super.getDescriptionId(stack);
        }
    }
}
