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
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.world.level.ItemLike;

import static hellfirepvp.astralsorcery.common.registry.research.ResearchPages.*;

/** Discovery chapter nodes, pages, tome lookups and graph edges. */
final class DiscoveryResearch {
    private DiscoveryResearch() {}

    static void registerEntries() {
        ResearchNode resWelcome = new ResearchNode(ItemsAS.TOME.get(), "WELCOME", 1, 1)
                .addPage(text("WELCOME.1"))
                .addPage(text("WELCOME.2"))
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resShrines = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "SHRINES", 2, 0)
                .addPage(text("SHRINES.1"))
                .addPage(text("SHRINES.2"))
                .addPage(text("SHRINES.3"))
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resConstellationPaper = new ResearchNode(ItemsAS.CONSTELLATION_PAPER.get(), "CPAPER", 3, 1)
                .addPage(text("CPAPER.1"))
                .addPage(recipe(ItemsAS.TOME.get()))
                .addPage(text("CPAPER.3"))
                .addPage(recipe(ItemsAS.PARCHMENT.get()))
                .addTomeLookup(ItemsAS.CONSTELLATION_PAPER.get(), 0, ResearchProgression.DISCOVERY)
                .addTomeLookup(ItemsAS.TOME.get(), 1, ResearchProgression.DISCOVERY)
                .addTomeLookup(ItemsAS.PARCHMENT.get(), 3, ResearchProgression.DISCOVERY)
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resOres = new ResearchNode(new ItemLike[] {
                BlocksAS.ROCK_CRYSTAL_ORE.get(),
                BlocksAS.AQUAMARINE_SAND_ORE.get()
        }, "ORES", 2, 2)
                .addPage(text("ORES.1"))
                .addPage(text("ORES.2"))
                .addTomeLookup(ItemsAS.AQUAMARINE.get(), 0, ResearchProgression.DISCOVERY)
                .addTomeLookup(ItemsAS.ROCK_CRYSTAL.get(), 0, ResearchProgression.DISCOVERY)
                .addTomeLookup(BlocksAS.AQUAMARINE_SAND_ORE.get(), 0, ResearchProgression.DISCOVERY)
                .addTomeLookup(BlocksAS.ROCK_CRYSTAL_ORE.get(), 0, ResearchProgression.DISCOVERY)
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resResonatingWand = new ResearchNode(ItemsAS.WAND.get(), "WAND", 3, 3)
                .addPage(text("WAND.1"))
                .addPage(recipeVanilla(ItemsAS.WAND.get()))
                .addTomeLookup(ItemsAS.WAND.get(), 1, ResearchProgression.DISCOVERY)
                .addPage(text("WAND.3"))
                .addPage(text("WAND.4"))
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resAltar1 = new ResearchNode(BlocksAS.ALTAR_DISCOVERY.get(), "ALTAR1", 4, 2)
                .addPage(text("ALTAR1.1"))
                .addPage(text("ALTAR1.2"))
                .addPage(recipeTransmutation(BlocksAS.ALTAR_DISCOVERY.get()))
                .addTomeLookup(BlocksAS.ALTAR_DISCOVERY.get(), 1, ResearchProgression.DISCOVERY)
                .addPage(text("ALTAR1.4"))
                .addPage(text("ALTAR1.5"))
                .addPage(text("ALTAR1.6"))
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resMarbles = new ResearchNode(new ItemLike[] {
                BlocksAS.MARBLE_RAW.get(),
                BlocksAS.MARBLE_PILLAR.get(),
                BlocksAS.MARBLE_ARCH.get(),
                BlocksAS.MARBLE_BRICKS.get(),
                BlocksAS.MARBLE_CHISELED.get(),
                BlocksAS.MARBLE_ENGRAVED.get(),
                BlocksAS.MARBLE_RUNED.get(),
                BlocksAS.MARBLE_SLAB.get(),
                BlocksAS.MARBLE_STAIRS.get()
        }, "MARBLETYPES", 0, 2.5F)
                .addPage(text("MARBLETYPES.1"))
                .addPage(text("MARBLETYPES.2"))
                .addTomeLookup(BlocksAS.MARBLE_RAW.get(), 1, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_PILLAR.get()))
                .addTomeLookup(BlocksAS.MARBLE_PILLAR.get(), 2, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_ARCH.get()))
                .addTomeLookup(BlocksAS.MARBLE_ARCH.get(), 3, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_BRICKS.get()))
                .addTomeLookup(BlocksAS.MARBLE_BRICKS.get(), 4, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_CHISELED.get()))
                .addTomeLookup(BlocksAS.MARBLE_CHISELED.get(), 5, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_ENGRAVED.get()))
                .addTomeLookup(BlocksAS.MARBLE_ENGRAVED.get(), 6, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_RUNED.get()))
                .addTomeLookup(BlocksAS.MARBLE_RUNED.get(), 7, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_SLAB.get()))
                .addTomeLookup(BlocksAS.MARBLE_SLAB.get(), 8, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_STAIRS.get()))
                .addTomeLookup(BlocksAS.MARBLE_STAIRS.get(), 9, ResearchProgression.DISCOVERY)
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resSootyMarble = new ResearchNode(new ItemLike[] {
                BlocksAS.BLACK_MARBLE_RAW.get(),
                BlocksAS.BLACK_MARBLE_PILLAR.get(),
                BlocksAS.BLACK_MARBLE_ARCH.get(),
                BlocksAS.BLACK_MARBLE_BRICKS.get(),
                BlocksAS.BLACK_MARBLE_CHISELED.get(),
                BlocksAS.BLACK_MARBLE_ENGRAVED.get(),
                BlocksAS.BLACK_MARBLE_RUNED.get(),
                BlocksAS.BLACK_MARBLE_SLAB.get(),
                BlocksAS.BLACK_MARBLE_STAIRS.get()
        }, "SOOTYMARBLE", 1, 3)
                .addPage(text("SOOTYMARBLE.1"))
                .addPage(text("SOOTYMARBLE.2"))
                .addPage(recipe(BlocksAS.BLACK_MARBLE_RAW.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_RAW.get(), 2, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_PILLAR.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_PILLAR.get(), 3, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_ARCH.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_ARCH.get(), 4, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_BRICKS.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_BRICKS.get(), 5, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_CHISELED.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_CHISELED.get(), 6, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_ENGRAVED.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_ENGRAVED.get(), 7, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_RUNED.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_RUNED.get(), 8, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_SLAB.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_SLAB.get(), 9, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_STAIRS.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_STAIRS.get(), 10, ResearchProgression.DISCOVERY)
                .register(ResearchProgression.DISCOVERY);

        resShrines.addSourceConnectionFrom(resWelcome);
        resConstellationPaper.addSourceConnectionFrom(resShrines);
        resMarbles.addSourceConnectionFrom(resWelcome);
        resSootyMarble.addSourceConnectionFrom(resMarbles);
        resOres.addSourceConnectionFrom(resWelcome);
        resResonatingWand.addSourceConnectionFrom(resOres);
        resAltar1.addSourceConnectionFrom(resResonatingWand);
    }
}
