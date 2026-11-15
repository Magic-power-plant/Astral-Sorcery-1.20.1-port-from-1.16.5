/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.research;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.journal.JournalPageRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import static hellfirepvp.astralsorcery.common.registry.research.ResearchPages.*;

/** Constellation chapter nodes, pages, tome lookups and graph edges. */
final class ConstellationResearch {
    private ConstellationResearch() {}

    static void registerEntries() {
        ResearchNode resLenses = new ResearchNode(ItemsAS.GLASS_LENS.get(), "LENSES_EFFECTS", 6.25F, 1.75F)
                .addPage(text("LENSES_EFFECTS.1"))
                .addPage(text("LENSES_EFFECTS.2"))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensFire = new ResearchNode(ItemsAS.COLORED_LENS_FIRE.get(), "IGNITION_LENS", 5.5F, 0.5F)
                .addPage(text("IGNITION_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_FIRE.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_FIRE.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensBreak = new ResearchNode(ItemsAS.COLORED_LENS_BREAK.get(), "BREAK_LENS", 6.75F, 0.25F)
                .addPage(text("BREAK_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_BREAK.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_BREAK.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensDamage = new ResearchNode(ItemsAS.COLORED_LENS_DAMAGE.get(), "DAMAGE_LENS", 7.5F, 1.25F)
                .addPage(text("DAMAGE_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_DAMAGE.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_DAMAGE.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensPush = new ResearchNode(ItemsAS.COLORED_LENS_PUSH.get(), "PUSH_LENS", 7.25F, 2.25F)
                .addPage(text("PUSH_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_PUSH.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_PUSH.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensRegeneration = new ResearchNode(ItemsAS.COLORED_LENS_REGENERATION.get(), "REGENERATION_LENS", 6.75F, 3F)
                .addPage(text("REGENERATION_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_REGENERATION.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_REGENERATION.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensGrowth = new ResearchNode(ItemsAS.COLORED_LENS_GROWTH.get(), "GROWTH_LENS", 5.75F, 2.75F)
                .addPage(text("GROWTH_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_GROWTH.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_GROWTH.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensSpectral = new ResearchNode(ItemsAS.COLORED_LENS_SPECTRAL.get(), "SPECTRAL_LENS", 4.75F, 2)
                .addPage(text("SPECTRAL_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_SPECTRAL.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_SPECTRAL.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resInfuser = new ResearchNode(BlocksAS.INFUSER.get(), "INFUSER", 2, 1.75F)
                .addPage(text("INFUSER.1"))
                .addPage(recipe(BlocksAS.INFUSER.get()))
                .addTomeLookup(BlocksAS.INFUSER.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("INFUSER.3"))
                .addPage(structure(StructureTypesAS.PTYPE_INFUSER))
                .addPage(recipeInfusion(ItemsAS.RESONATING_GEM.get()))
                .addTomeLookup(ItemsAS.RESONATING_GEM.get(), 4, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resEnchantmentAmulet = new ResearchNode(ItemsAS.ENCHANTMENT_AMULET.get(), "ENCHANTMENT_AMULET", 1.75F, 3.25F)
                .addPage(text("ENCHANTMENT_AMULET.1"))
                .addPage(JournalPageRecipe.fromName(AstralSorcery.key("altar/enchantment_amulet_init")))
                .addTomeLookup(ItemsAS.ENCHANTMENT_AMULET.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("ENCHANTMENT_AMULET.3"))
                .addPage(JournalPageRecipe.fromName(AstralSorcery.key("altar/enchantment_amulet_reroll")))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resRitualLink = new ResearchNode(BlocksAS.RITUAL_LINK.get(), "RITUAL_LINK", 0.5F, 3.5F)
                .addPage(text("RITUAL_LINK.1"))
                .addPage(recipe(BlocksAS.RITUAL_LINK.get()))
                .addTomeLookup(BlocksAS.RITUAL_LINK.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resIlluminationWand = new ResearchNode(ItemsAS.ILLUMINATION_WAND.get(), "ILLUMINATION_WAND", 0.25F, 2.5F)
                .addPage(text("ILLUMINATION_WAND.1"))
                .addPage(recipe(ItemsAS.ILLUMINATION_WAND.get()))
                .addTomeLookup(ItemsAS.ILLUMINATION_WAND.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("ILLUMINATION_WAND.3"))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resInfusedTools = new ResearchNode(new ItemLike[] {
                ItemsAS.INFUSED_CRYSTAL_SWORD.get(),
                ItemsAS.INFUSED_CRYSTAL_PICKAXE.get(),
                ItemsAS.INFUSED_CRYSTAL_AXE.get(),
                ItemsAS.INFUSED_CRYSTAL_SHOVEL.get()
        }, "CHARGED_TOOLS", 0.25F, 1.25F)
                .addPage(text("CHARGED_TOOLS.1"))
                .addPage(text("CHARGED_TOOLS.2"))
                .addPage(recipeInfusion(ItemsAS.INFUSED_CRYSTAL_SWORD.get()))
                .addTomeLookup(ItemsAS.INFUSED_CRYSTAL_SWORD.get(), 2, ResearchProgression.CONSTELLATION)
                .addPage(recipeInfusion(ItemsAS.INFUSED_CRYSTAL_PICKAXE.get()))
                .addTomeLookup(ItemsAS.INFUSED_CRYSTAL_PICKAXE.get(), 3, ResearchProgression.CONSTELLATION)
                .addPage(recipeInfusion(ItemsAS.INFUSED_CRYSTAL_AXE.get()))
                .addTomeLookup(ItemsAS.INFUSED_CRYSTAL_AXE.get(), 4, ResearchProgression.CONSTELLATION)
                .addPage(recipeInfusion(ItemsAS.INFUSED_CRYSTAL_SHOVEL.get()))
                .addTomeLookup(ItemsAS.INFUSED_CRYSTAL_SHOVEL.get(), 5, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resTreeBeacon = new ResearchNode(BlocksAS.TREE_BEACON.get(), "TREEBEACON", 1.25F, 0.5F)
                .addPage(text("TREEBEACON.1"))
                .addPage(recipe(BlocksAS.TREE_BEACON.get()))
                .addTomeLookup(BlocksAS.TREE_BEACON.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("TREEBEACON.3"))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resPrism = new ResearchNode(BlocksAS.PRISM.get(), "PRISM", 2.75F, 0)
                .addPage(text("PRISM.1"))
                .addPage(recipe(BlocksAS.PRISM.get()))
                .addTomeLookup(BlocksAS.PRISM.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resEngravingTable = new ResearchNode(BlocksAS.REFRACTION_TABLE.get(), "DRAWING_TABLE", 3.5F, 1)
                .addPage(text("DRAWING_TABLE.1"))
                .addPage(recipe(BlocksAS.REFRACTION_TABLE.get()))
                .addTomeLookup(BlocksAS.REFRACTION_TABLE.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("DRAWING_TABLE.3"))
                .addPage(text("DRAWING_TABLE.4"))
                .addPage(recipe(ItemsAS.INFUSED_GLASS.get()))
                .addTomeLookup(ItemsAS.INFUSED_GLASS.get(), 4, ResearchProgression.CONSTELLATION)
                .addPage(text("DRAWING_TABLE.6"))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resAltar4 = new ResearchNode(BlocksAS.ALTAR_RADIANCE.get(), "ALTAR4", 3.5F, 3)
                .addPage(text("ALTAR4.1"))
                .addPage(recipe(BlocksAS.ALTAR_RADIANCE.get()))
                .addTomeLookup(BlocksAS.ALTAR_RADIANCE.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(structure(StructureTypesAS.PTYPE_ALTAR_TRAIT))
                .addPage(text("ALTAR4.4"))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resCollectorCrystal = new ResearchNode(BlocksAS.ROCK_COLLECTOR_CRYSTAL.get(), "COLL_CRYSTAL", 2.75F, 3.75F)
                .addPage(text("COLL_CRYSTAL.1"))
                .addPage(recipe(BlocksAS.ROCK_COLLECTOR_CRYSTAL.get()))
                .addTomeLookup(BlocksAS.ROCK_COLLECTOR_CRYSTAL.get(), 1, ResearchProgression.CONSTELLATION)
                .addTomeLookup(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("COLL_CRYSTAL.3"))
                .addPage(text("COLL_CRYSTAL.4"))
                .register(ResearchProgression.CONSTELLATION);

        ItemStack celestialCrystalCluster = new ItemStack(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get());
        celestialCrystalCluster.setDamageValue(4); //Growth stage 4
        ResearchNode resCelestialCrystalCluster = new ResearchNode(celestialCrystalCluster, "CEL_CRYSTAL_GROW", 6.25F, 4)
                .addPage(text("CEL_CRYSTAL_GROW.1"))
                .addPage(text("CEL_CRYSTAL_GROW.2"))
                .addPage(text("CEL_CRYSTAL_GROW.3"))
                .addTomeLookup(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get(), 0, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resCelestialCrystals = new ResearchNode(ItemsAS.CELESTIAL_CRYSTAL.get(), "CEL_CRYSTALS", 5, 3.75F)
                .addPage(text("CEL_CRYSTALS.1"))
                .addTomeLookup(ItemsAS.CELESTIAL_CRYSTAL.get(), 0, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resEnhancedCollectorCrystal = new ResearchNode(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get(), "ENHANCED_COLLECTOR", 4, 4.5F)
                .addPage(text("ENHANCED_COLLECTOR.1"))
                .addPage(structure(StructureTypesAS.PTYPE_ENHANCED_COLLECTOR_CRYSTAL))
                .register(ResearchProgression.CONSTELLATION);

        resColoredLensFire.addSourceConnectionFrom(resLenses);
        resColoredLensBreak.addSourceConnectionFrom(resLenses);
        resColoredLensDamage.addSourceConnectionFrom(resLenses);
        resColoredLensPush.addSourceConnectionFrom(resLenses);
        resColoredLensRegeneration.addSourceConnectionFrom(resLenses);
        resColoredLensGrowth.addSourceConnectionFrom(resLenses);
        resColoredLensSpectral.addSourceConnectionFrom(resLenses);
        resColoredLensSpectral.addSourceConnectionFrom(resInfuser);
        resEngravingTable.addSourceConnectionFrom(resColoredLensSpectral);

        resEnchantmentAmulet.addSourceConnectionFrom(resInfuser);
        resRitualLink.addSourceConnectionFrom(resInfuser);
        resIlluminationWand.addSourceConnectionFrom(resInfuser);
        resInfusedTools.addSourceConnectionFrom(resInfuser);
        resTreeBeacon.addSourceConnectionFrom(resInfuser);
        resPrism.addSourceConnectionFrom(resInfuser);
        resAltar4.addSourceConnectionFrom(resInfuser);
        resEngravingTable.addSourceConnectionFrom(resInfuser);
        resCollectorCrystal.addSourceConnectionFrom(resInfuser);

        resCelestialCrystals.addSourceConnectionFrom(resCelestialCrystalCluster);
        resAltar4.addSourceConnectionFrom(resCelestialCrystals);
        resEnhancedCollectorCrystal.addSourceConnectionFrom(resCollectorCrystal);
        resEnhancedCollectorCrystal.addSourceConnectionFrom(resCelestialCrystals);
    }
}
