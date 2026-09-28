/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.block;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemBlockCelestialCrystalCluster
 * Created by HellFirePvP
 * Date: 17.05.2020 / 09:05
 */
public class ItemBlockCelestialCrystalCluster extends ItemBlockCustom implements CrystalAttributeGenItem, CreativeTabItem {

    public ItemBlockCelestialCrystalCluster(Block block, Properties itemProperties) {
        super(block, itemProperties
                .rarity(CommonProxy.RARITY_CELESTIAL));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        if (!world.isClientSide()) {
            CrystalAttributes attributes = getAttributes(stack);

            if (attributes == null && stack.getItem() instanceof CrystalAttributeGenItem) {
                attributes = CrystalGenerator.generateNewAttributes(stack);
                attributes.store(stack);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            attr.addTooltip(tooltip);
        }
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
        for (int stage : BlockCelestialCrystalCluster.STAGE.getPossibleValues()) {
            ItemStack cluster = new ItemStack(this);
            this.setDamage(cluster, stage);
            items.add(cluster);
        }
        return items;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState toPlace = super.getPlacementState(context);
        if (toPlace != null) {
            return toPlace.setValue(BlockCelestialCrystalCluster.STAGE, this.getDamage(context.getItemInHand()));
        }
        return null;
    }

    @Override
    public int getGeneratedPropertyTiers() {
        // 1.16.5 ItemsAS.CELESTIAL_CRYSTAL was a typed ItemCelestialCrystal field; the target holds a
        // RegistryObject<Item>, hence the cast to the attribute-generating interface.
        return ((CrystalAttributeGenItem) ItemsAS.CELESTIAL_CRYSTAL.get()).getGeneratedPropertyTiers();
    }

    @Override
    public int getMaxPropertyTiers() {
        return ((CrystalAttributeGenItem) ItemsAS.CELESTIAL_CRYSTAL.get()).getMaxPropertyTiers();
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes(ItemStack stack) {
        return CrystalAttributes.getCrystalAttributes(stack);
    }

    @Override
    public void setAttributes(ItemStack stack, @Nullable CrystalAttributes attributes) {
        if (attributes != null) {
            attributes.store(stack);
        } else {
            CrystalAttributes.storeNull(stack);
        }
    }
}
