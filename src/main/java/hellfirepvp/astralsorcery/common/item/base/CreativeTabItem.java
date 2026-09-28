/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.base;

import hellfirepvp.astralsorcery.common.lib.CreativeTabsAS;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CreativeTabItem
 * Created by HellFirePvP
 */
//Ported from 1.21.1 common/item/base/CreativeTabItem. It replaces 1.16.5's Item.Properties#group plus
//Item#fillItemGroup, both of which are gone from 1.20.1: creative tab contents are assembled by
//BuildCreativeModeTabContentsEvent, so an item declares its tab and fills it instead of being pushed into one at
//construction time. CommonProxy#onBuildCreativeTabContents is the single caller.
//
//Unlike 1.21.1 this does NOT mark fillCreativeTab @OnlyIn(Dist.CLIENT). An @OnlyIn on an interface member is
//applied to every implementor, so a dedicated server refuses to load them - the same failure that kept
//TileAreaOfInfluence's implementors off the server. The event is client-only anyway (Forge fires it on the logical
//client only), and the default body builds nothing but an ItemStack, which is side-agnostic.
public interface CreativeTabItem extends ItemLike {

    default CreativeModeTab getCreativeTab() {
        return CreativeTabsAS.TAB_AS.get();
    }

    default boolean isInTab(CreativeModeTab tab) {
        return tab == this.getCreativeTab();
    }

    //1.16.5 Item#fillItemGroup(ItemGroup, NonNullList<ItemStack>). The 1.20.1 sink is CreativeModeTab.Output, which
    //BuildCreativeModeTabContentsEvent implements directly.
    default void fillCreativeTab(CreativeModeTab.Output output) {
        output.accept(new ItemStack(this));
    }
}
