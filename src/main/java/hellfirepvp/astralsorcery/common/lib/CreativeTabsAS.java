/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CreativeTabsAS
 * Created by HellFirePvP
 */
//Ported from 1.21.1 common/lib/CreativeTabsAS.
//
//1.20.1 made CreativeModeTab a registry entry (Registries.CREATIVE_MODE_TAB); a tab built with
//CreativeModeTab.builder() but never registered simply does not exist in the creative menu. CommonProxy used to hold
//the three tabs as plain static fields, so none of them were registered and no listener filled them: the creative
//menu contained no Astral Sorcery item at all. The tabs live here now, registered like every other content registry.
//
//The tab set is 1.16.5's three (main / constellation papers / attuned crystals) rather than 1.21.1's five: 1.21.1
//adds "lumen" and "artifacts" for the lumen crystal and artifact systems, neither of which exists in this tree. The
//three lang keys itemGroup.astralsorcery, .papers and .crystals are already present in en_us.json.
public class CreativeTabsAS {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AstralSorcery.MODID);

    public static final RegistryObject<CreativeModeTab> TAB_AS = CREATIVE_TABS.register("common",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.astralsorcery"))
                    .icon(() -> new ItemStack(ItemsAS.TOME.get()))
                    .build());

    public static final RegistryObject<CreativeModeTab> TAB_AS_PAPERS = CREATIVE_TABS.register("papers",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.astralsorcery.papers"))
                    .icon(() -> new ItemStack(ItemsAS.CONSTELLATION_PAPER.get()))
                    .withTabsBefore(AstralSorcery.key("common"))
                    .build());

    public static final RegistryObject<CreativeModeTab> TAB_AS_CRYSTALS = CREATIVE_TABS.register("crystals",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.astralsorcery.crystals"))
                    .icon(() -> new ItemStack(ItemsAS.ROCK_CRYSTAL.get()))
                    .withTabsBefore(AstralSorcery.key("papers"))
                    .build());

    private CreativeTabsAS() {}
}
