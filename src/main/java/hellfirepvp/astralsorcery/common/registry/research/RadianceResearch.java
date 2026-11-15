/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.research;

import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import static hellfirepvp.astralsorcery.common.registry.research.ResearchPages.*;

/** Radiance chapter nodes, pages, tome lookups and graph edges. */
final class RadianceResearch {
    private RadianceResearch() {}

    static void registerEntries() {
        ResearchNode resAttuneCrystalTrait = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "ATT_TRAIT", 0, 0)
                .addPage(text("ATT_TRAIT.1"))
                .addPage(text("ATT_TRAIT.2"))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resObservatory = new ResearchNode(BlocksAS.OBSERVATORY.get(), "OBSERVATORY", 1.5F, 0.25F)
                .addPage(text("OBSERVATORY.1"))
                .addPage(recipe(BlocksAS.OBSERVATORY.get()))
                .addTomeLookup(BlocksAS.OBSERVATORY.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("OBSERVATORY.3"))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resIrradiantStars = new ResearchNode(new ItemLike[] {
                ItemsAS.SHIFTING_STAR_AEVITAS.get(),
                ItemsAS.SHIFTING_STAR_ARMARA.get(),
                ItemsAS.SHIFTING_STAR_DISCIDIA.get(),
                ItemsAS.SHIFTING_STAR_EVORSIO.get(),
                ItemsAS.SHIFTING_STAR_VICIO.get()
        }, "ENH_SHIFTING_STAR", 3.25F, 0.5F)
                .addPage(text("ENH_SHIFTING_STAR.1"))
                .addPage(recipe(ItemsAS.SHIFTING_STAR_AEVITAS.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_AEVITAS.get(), 1, ResearchProgression.RADIANCE)
                .addPage(recipe(ItemsAS.SHIFTING_STAR_ARMARA.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_ARMARA.get(), 2, ResearchProgression.RADIANCE)
                .addPage(recipe(ItemsAS.SHIFTING_STAR_DISCIDIA.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_DISCIDIA.get(), 3, ResearchProgression.RADIANCE)
                .addPage(recipe(ItemsAS.SHIFTING_STAR_EVORSIO.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_EVORSIO.get(), 4, ResearchProgression.RADIANCE)
                .addPage(recipe(ItemsAS.SHIFTING_STAR_VICIO.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_VICIO.get(), 5, ResearchProgression.RADIANCE)
                .register(ResearchProgression.RADIANCE);

        ResearchNode resRelayCrafting = new ResearchNode(BlocksAS.ALTAR_RADIANCE.get(), "CRAFTING_FOCUS_HINT", 2.5F, 2F)
                .addPage(text("CRAFTING_FOCUS_HINT.1"))
                .addPage(text("CRAFTING_FOCUS_HINT.2"))
                .addPage(text("CRAFTING_FOCUS_HINT.3"))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resMantle = new ResearchNode(ItemsAS.MANTLE.get(), "ATT_CAPE", 1.25F, 2.5F)
                .addPage(text("ATT_CAPE.1"))
                .addPage(recipe(stack -> ItemsAS.MANTLE.get().equals(stack.getItem()) &&
                        ((ItemMantle) ItemsAS.MANTLE.get()).getConstellation(stack) == null))
                .addTomeLookup(ItemsAS.MANTLE.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("ATT_CAPE.3"))
                .addPage(text("ATT_CAPE.4"))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resChalice = new ResearchNode(BlocksAS.CHALICE.get(), "C_CHALICE", 3, 3)
                .addPage(text("C_CHALICE.1"))
                .addPage(recipe(BlocksAS.CHALICE.get()))
                .addTomeLookup(BlocksAS.CHALICE.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("C_CHALICE.3"))
                .addPage(text("C_CHALICE.4"))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resFountain = new ResearchNode(BlocksAS.FOUNTAIN.get(), "BORE_CORE", 2.25F, 4F)
                .addPage(text("BORE_CORE.1"))
                .addPage(recipe(BlocksAS.FOUNTAIN.get()))
                .addTomeLookup(BlocksAS.FOUNTAIN.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("BORE_CORE.3"))
                .addPage(structure(StructureTypesAS.PTYPE_FOUNTAIN))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resLiquidBore = new ResearchNode(BlocksAS.FOUNTAIN_PRIME_LIQUID.get(), "BORE_HEAD_LIQUID", 1.5F, 5F)
                .addPage(text("BORE_HEAD_LIQUID.1"))
                .addPage(recipe(BlocksAS.FOUNTAIN_PRIME_LIQUID.get()))
                .addTomeLookup(BlocksAS.FOUNTAIN_PRIME_LIQUID.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("BORE_HEAD_LIQUID.3"))
                .addPage(text("BORE_HEAD_LIQUID.4"))
                .register(ResearchProgression.RADIANCE);

        ItemStack liquidResonator = ItemResonator.setUpgradeUnlocked(new ItemStack(ItemsAS.RESONATOR.get()), ItemResonator.ResonatorUpgrade.STARLIGHT, ItemResonator.ResonatorUpgrade.FLUID_FIELDS);
        ItemResonator.setCurrentUpgradeUnsafe(liquidResonator, ItemResonator.ResonatorUpgrade.FLUID_FIELDS);
        ResearchNode resLiquidResonator = new ResearchNode(liquidResonator, "ICHOSIC", 0, 4.5F)
                .addPage(text("ICHOSIC.1"))
                .addPage(recipe(stack -> ItemsAS.RESONATOR.get().equals(stack.getItem()) && ItemResonator.getCurrentUpgrade(null, stack) == ItemResonator.ResonatorUpgrade.FLUID_FIELDS))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resVortexBore = new ResearchNode(BlocksAS.FOUNTAIN_PRIME_VORTEX.get(), "BORE_HEAD_VORTEX", 3.5F, 4.75F)
                .addPage(text("BORE_HEAD_VORTEX.1"))
                .addPage(recipe(BlocksAS.FOUNTAIN_PRIME_VORTEX.get()))
                .addTomeLookup(BlocksAS.FOUNTAIN_PRIME_VORTEX.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("BORE_HEAD_VORTEX.3"))
                .register(ResearchProgression.RADIANCE);

        resIrradiantStars.addSourceConnectionFrom(resRelayCrafting);
        resMantle.addSourceConnectionFrom(resRelayCrafting);
        resObservatory.addSourceConnectionFrom(resRelayCrafting);
        resAttuneCrystalTrait.addSourceConnectionFrom(resObservatory);
        resChalice.addSourceConnectionFrom(resRelayCrafting);
        resFountain.addSourceConnectionFrom(resChalice);
        resVortexBore.addSourceConnectionFrom(resFountain);
        resLiquidBore.addSourceConnectionFrom(resFountain);
        resLiquidResonator.addSourceConnectionFrom(resLiquidBore);
    }
}
