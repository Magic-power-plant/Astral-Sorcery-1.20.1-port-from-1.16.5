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
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import static hellfirepvp.astralsorcery.common.registry.research.ResearchPages.*;

/** Crafting chapter nodes, pages, tome lookups and graph edges. */
final class CraftingResearch {
    private CraftingResearch() {}

    static void registerEntries() {
        ResearchNode resHandTelescope = new ResearchNode(ItemsAS.HAND_TELESCOPE.get(), "HAND_TELESCOPE", 0, 1)
                .addPage(text("HAND_TELESCOPE.1"))
                .addPage(recipe(ItemsAS.GLASS_LENS.get()))
                .addTomeLookup(ItemsAS.GLASS_LENS.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(ItemsAS.HAND_TELESCOPE.get()))
                .addTomeLookup(ItemsAS.HAND_TELESCOPE.get(), 2, ResearchProgression.BASIC_CRAFT)
                .addPage(text("HAND_TELESCOPE.4"))
                .addPage(text("HAND_TELESCOPE.5"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resIlluminationPowder = new ResearchNode(ItemsAS.ILLUMINATION_POWDER.get(), "ILLUM_POWDER", 1, 2)
                .addPage(text("ILLUM_POWDER.1"))
                .addPage(recipe(ItemsAS.ILLUMINATION_POWDER.get()))
                .addTomeLookup(ItemsAS.ILLUMINATION_POWDER.get(), 1, ResearchProgression.BASIC_CRAFT)
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resNocturnalPowder = new ResearchNode(ItemsAS.NOCTURNAL_POWDER.get(), "NOC_POWDER", 0, 2.5F)
                .addPage(text("NOC_POWDER.1"))
                .addPage(recipe(ItemsAS.NOCTURNAL_POWDER.get()))
                .addTomeLookup(ItemsAS.NOCTURNAL_POWDER.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(text("NOC_POWDER.3"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resIlluminator = new ResearchNode(BlocksAS.ILLUMINATOR.get(), "ILLUMINATOR", 0.75F, 3)
                .addPage(text("ILLUMINATOR.1"))
                .addPage(recipe(BlocksAS.ILLUMINATOR.get()))
                .addTomeLookup(BlocksAS.ILLUMINATOR.get(), 1, ResearchProgression.BASIC_CRAFT)
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resRockCrystals = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "ROCK_CRYSTALS", 2, 1.5F)
                .addPage(text("ROCK_CRYSTALS.1"))
                .addPage(text("ROCK_CRYSTALS.2"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resCrystalGrowth = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "CRYSTAL_GROWTH", 3, 2.5F)
                .addPage(text("CRYSTAL_GROWTH.1"))
                .addPage(text("CRYSTAL_GROWTH.2"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resTools = new ResearchNode(new ItemLike[] {
                ItemsAS.CRYSTAL_SWORD.get(),
                ItemsAS.CRYSTAL_PICKAXE.get(),
                ItemsAS.CRYSTAL_AXE.get(),
                ItemsAS.CRYSTAL_SHOVEL.get()
        }, "TOOLS", 4.5F, 3)
                .addPage(text("TOOLS.1"))
                .addPage(text("TOOLS.3"))
                .addPage(recipe(ItemsAS.CRYSTAL_SWORD.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_SWORD.get(), 2, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(ItemsAS.CRYSTAL_PICKAXE.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_PICKAXE.get(), 3, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(ItemsAS.CRYSTAL_AXE.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_AXE.get(), 4, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(ItemsAS.CRYSTAL_SHOVEL.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_SHOVEL.get(), 5, ResearchProgression.BASIC_CRAFT)
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resLightwell = new ResearchNode(BlocksAS.WELL.get(), "WELL", 3, 1)
                .addPage(text("WELL.1"))
                .addPage(recipe(BlocksAS.WELL.get()))
                .addTomeLookup(BlocksAS.WELL.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(text("WELL.3"))
                .addPage(text("WELL.4"))
                .addPage(text("WELL.5"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resInfusedWood = new ResearchNode(new ItemLike[] {
                BlocksAS.INFUSED_WOOD.get(),
                BlocksAS.INFUSED_WOOD_ARCH.get(),
                BlocksAS.INFUSED_WOOD_COLUMN.get(),
                BlocksAS.INFUSED_WOOD_ENGRAVED.get(),
                BlocksAS.INFUSED_WOOD_ENRICHED.get(),
                BlocksAS.INFUSED_WOOD_PLANKS.get(),
                BlocksAS.INFUSED_WOOD_STAIRS.get(),
                BlocksAS.INFUSED_WOOD_SLAB.get()
        }, "INFUSED_WOOD", 4, 0)
                .addPage(text("INFUSED_WOOD.1"))
                .addTomeLookup(BlocksAS.INFUSED_WOOD.get(), 0, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_PLANKS.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_PLANKS.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_ARCH.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_ARCH.get(), 2, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_COLUMN.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_COLUMN.get(), 3, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_ENGRAVED.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_ENGRAVED.get(), 4, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_ENRICHED.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_ENRICHED.get(), 5, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_STAIRS.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_STAIRS.get(), 6, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_SLAB.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_SLAB.get(), 7, ResearchProgression.BASIC_CRAFT)
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resAltar2 = new ResearchNode(BlocksAS.ALTAR_ATTUNEMENT.get(), "ALTAR2", 4, 1.5F)
                .addPage(text("ALTAR2.1"))
                .addPage(recipe(BlocksAS.ALTAR_ATTUNEMENT.get()))
                .addTomeLookup(BlocksAS.ALTAR_ATTUNEMENT.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(structure(StructureTypesAS.PTYPE_ALTAR_ATTUNEMENT))
                .addPage(text("ALTAR2.4"))
                .register(ResearchProgression.BASIC_CRAFT);

        ItemStack starlightResonator = ItemResonator.setUpgradeUnlocked(new ItemStack(ItemsAS.RESONATOR.get()), ItemResonator.ResonatorUpgrade.STARLIGHT);
        ItemResonator.setCurrentUpgradeUnsafe(starlightResonator, ItemResonator.ResonatorUpgrade.STARLIGHT);
        ResearchNode resResonator = new ResearchNode(starlightResonator, "SKY_RESO", 5, 0.5F)
                .addPage(text("SKY_RESO.1"))
                .addPage(recipe(stack -> ItemsAS.RESONATOR.get().equals(stack.getItem()) && ItemResonator.getCurrentUpgrade(null, stack) == ItemResonator.ResonatorUpgrade.STARLIGHT))
                .addPage(text("SKY_RESO.2"))
                .addPage(text("SKY_RESO.3"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resSpectralRelay = new ResearchNode(BlocksAS.SPECTRAL_RELAY.get(), "SPEC_RELAY", 6, 1)
                .addPage(text("SPEC_RELAY.1"))
                .addPage(recipe(BlocksAS.SPECTRAL_RELAY.get()))
                .addTomeLookup(BlocksAS.SPECTRAL_RELAY.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(structure(StructureTypesAS.PTYPE_SPECTRAL_RELAY))
                .addPage(text("SPEC_RELAY.4"))
                .register(ResearchProgression.BASIC_CRAFT);

        resNocturnalPowder.addSourceConnectionFrom(resIlluminationPowder);
        resIlluminator.addSourceConnectionFrom(resIlluminationPowder);
        resCrystalGrowth.addSourceConnectionFrom(resRockCrystals);
        resTools.addSourceConnectionFrom(resCrystalGrowth);
        resLightwell.addSourceConnectionFrom(resRockCrystals);
        resInfusedWood.addSourceConnectionFrom(resLightwell);
        resAltar2.addSourceConnectionFrom(resLightwell);
        resResonator.addSourceConnectionFrom(resLightwell);
        resSpectralRelay.addSourceConnectionFrom(resResonator);
    }
}
