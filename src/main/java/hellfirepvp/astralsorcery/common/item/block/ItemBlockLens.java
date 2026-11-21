/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.block;

import hellfirepvp.astralsorcery.common.crystal.CalculationContext;
import hellfirepvp.astralsorcery.api.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import net.minecraft.network.chat.Component;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemBlockLens
 * Created by HellFirePvP
 * Date: 24.08.2019 / 22:27
 */
public class ItemBlockLens extends ItemBlockCustom implements CrystalAttributeItem, CreativeTabItem {

    public ItemBlockLens(Block block, Properties itemProperties) {
        super(block, itemProperties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            attr.addTooltip(tooltip, CalculationContext.Builder.newBuilder()
                    .addUsage(CrystalPropertiesAS.Usages.USE_LENS_EFFECT)
                    .addUsage(CrystalPropertiesAS.Usages.USE_LENS_TRANSFER)
                    .build());
        }
    }

    /**
     * 1.16.5 {@code fillItemGroup} equivalent: the creative entry for this lens with the creative lens attributes.
     */
    @Override
    public void fillCreativeTab(CreativeModeTab.Output output) {
        output.accept(this.createCreativeStack());
    }

    public ItemStack createCreativeStack() {
        ItemStack lens = new ItemStack(this);
        this.setAttributes(lens, CrystalPropertiesAS.LENS_PRISM_CREATIVE_ATTRIBUTES);
        return lens;
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
