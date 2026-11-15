/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.research;

import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import static hellfirepvp.astralsorcery.common.registry.research.ResearchPages.*;

/** Attunement chapter nodes, pages, tome lookups and graph edges. */
final class AttunementResearch {
    private AttunementResearch() {}

    static void registerEntries() {
        ResearchNode resTelescope = new ResearchNode(BlocksAS.TELESCOPE.get(), "TELESCOPE", 0.5F, 0)
                .addPage(text("TELESCOPE.1"))
                .addPage(recipe(BlocksAS.TELESCOPE.get()))
                .addTomeLookup(BlocksAS.TELESCOPE.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("TELESCOPE.3"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resKnowledgeShare = new ResearchNode(ItemsAS.KNOWLEDGE_SHARE.get(), "KNOWLEDGE_SHARE", 2.5F, 0.25F)
                .addPage(text("KNOWLEDGE_SHARE.1"))
                .addPage(recipe(ItemsAS.KNOWLEDGE_SHARE.get()))
                .addTomeLookup(ItemsAS.KNOWLEDGE_SHARE.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resLens = new ResearchNode(BlocksAS.LENS.get(), "LENS", 0, 1.25F)
                .addPage(text("LENS.1"))
                .addPage(recipe(BlocksAS.LENS.get()))
                .addTomeLookup(BlocksAS.LENS.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("LENS.3"))
                .addPage(text("LENS.4"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resLinkingTool = new ResearchNode(ItemsAS.LINK_TOOL.get(), "LINKTOOL", 0.25F, 2.25F)
                .addPage(text("LINKTOOL.1"))
                .addPage(recipe(ItemsAS.LINK_TOOL.get()))
                .addTomeLookup(ItemsAS.LINK_TOOL.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("LINKTOOL.3"))
                .addPage(text("LINKTOOL.4"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resStarlightNetwork = new ResearchNode(BlocksAS.LENS.get(), "STARLIGHT_NETWORK", 1.5F, 1)
                .addPage(text("STARLIGHT_NETWORK.1"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resTransmutationOres = new ResearchNode(new ItemLike[] {
                Blocks.MAGMA_BLOCK,
                Blocks.SAND,
                Blocks.DIAMOND_ORE,
                Blocks.NETHER_WART_BLOCK,
                Blocks.PUMPKIN,
                Blocks.SANDSTONE
        }, "TRANSMUTATION_ORES", 2.75F, 1.5F)
                .addPage(text("TRANSMUTATION_ORES.1"))
                .addPage(recipeTransmutation(Blocks.CLAY))
                .addPage(recipeTransmutation(Blocks.EMERALD_ORE))
                .addPage(recipeTransmutation(Blocks.CAKE))
                .addPage(recipeTransmutation(Blocks.LAPIS_BLOCK))
                .addPage(recipeTransmutation(Blocks.END_STONE))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resStarmetalOre = new ResearchNode(BlocksAS.STARMETAL_ORE.get(), "STARMETAL_ORE", 4.25F, 1.75F)
                .addPage(text("STARMETAL_ORE.1"))
                .addPage(recipeTransmutation(BlocksAS.STARMETAL_ORE.get()))
                .addTomeLookup(ItemsAS.STARMETAL_INGOT.get(), 0, ResearchProgression.ATTUNEMENT)
                .addTomeLookup(BlocksAS.STARMETAL_ORE.get(), 0, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resCuttingTool = new ResearchNode(ItemsAS.CHISEL.get(), "CUTTING_TOOL", 5, 1)
                .addPage(text("CUTTING_TOOL.1"))
                .addPage(recipe(ItemsAS.CHISEL.get()))
                .addTomeLookup(ItemsAS.CHISEL.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("CUTTING_TOOL.3"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resStardust = new ResearchNode(ItemsAS.STARDUST.get(), "STARDUST", 6, 0.5F)
                .addPage(text("STARDUST.1"))
                .addPage(text("STARDUST.2"))
                .addTomeLookup(ItemsAS.STARDUST.get(), 0, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ItemStack structureResonator = ItemResonator.setUpgradeUnlocked(new ItemStack(ItemsAS.RESONATOR.get()), ItemResonator.ResonatorUpgrade.STARLIGHT, ItemResonator.ResonatorUpgrade.AREA_SIZE);
        ItemResonator.setCurrentUpgradeUnsafe(structureResonator, ItemResonator.ResonatorUpgrade.AREA_SIZE);
        ResearchNode resResonatorStructure = new ResearchNode(structureResonator, "RESONATOR_AREA_SIZE", 6.5F, 2)
                .addPage(text("RESONATOR_AREA_SIZE.1"))
                .addPage(recipe(stack -> ItemsAS.RESONATOR.get().equals(stack.getItem()) && ItemResonator.getCurrentUpgrade(null, stack) == ItemResonator.ResonatorUpgrade.AREA_SIZE))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resCelestialGateway = new ResearchNode(BlocksAS.CELESTIAL_GATEWAY.get(), "CELESTIAL_GATEWAY", 7.5F, 1.5F)
                .addPage(text("CELESTIAL_GATEWAY.1"))
                .addPage(recipe(BlocksAS.CELESTIAL_GATEWAY.get()))
                .addTomeLookup(BlocksAS.CELESTIAL_GATEWAY.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("CELESTIAL_GATEWAY.3"))
                .addPage(structure(StructureTypesAS.PTYPE_CELESTIAL_GATEWAY))
                .addPage(text("CELESTIAL_GATEWAY.5"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resAltar3 = new ResearchNode(BlocksAS.ALTAR_CONSTELLATION.get(), "ALTAR3", 7.25F, 0)
                .addPage(text("ALTAR3.1"))
                .addPage(recipe(BlocksAS.ALTAR_CONSTELLATION.get()))
                .addTomeLookup(BlocksAS.ALTAR_CONSTELLATION.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(structure(StructureTypesAS.PTYPE_ALTAR_CONSTELLATION))
                .addPage(text("ALTAR3.4"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resAttunePlayer = new ResearchNode(BlocksAS.ATTUNEMENT_ALTAR.get(), "ATT_PLAYER", 3.75F, 2.75F)
                .addPage(text("ATT_PLAYER.1"))
                .addPage(text("ATT_PLAYER.2"))
                .addPage(recipe(BlocksAS.ATTUNEMENT_ALTAR.get()))
                .addTomeLookup(BlocksAS.ATTUNEMENT_ALTAR.get(), 2, ResearchProgression.ATTUNEMENT)
                .addPage(text("ATT_PLAYER.4"))
                .addPage(structure(StructureTypesAS.PTYPE_ATTUNEMENT_ALTAR))
                .addPage(text("ATT_PLAYER.6"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resPerks = new ResearchNode(BlocksAS.SPECTRAL_RELAY.get(), "ATT_PERKS", 4.5F, 3.5F)
                .addPage(text("ATT_PERKS.1"))
                .addPage(text("ATT_PERKS.2"))
                .addPage(text("ATT_PERKS.3"))
                .addPage(text("ATT_PERKS.4"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resShiftingStar = new ResearchNode(ItemsAS.SHIFTING_STAR.get(), "SHIFT_STAR", 5.75F, 3.25F)
                .addPage(text("SHIFT_STAR.1"))
                .addPage(recipe(ItemsAS.SHIFTING_STAR.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resPerkSeal = new ResearchNode(ItemsAS.PERK_SEAL.get(), "ATT_PERKS_SEAL", 5.5F, 4.5F)
                .addPage(text("ATT_PERKS_SEAL.1"))
                .addPage(recipe(ItemsAS.PERK_SEAL.get()))
                .addTomeLookup(ItemsAS.PERK_SEAL.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resPerkGems = new ResearchNode(new ItemLike[] {
                ItemsAS.PERK_GEM_DAY.get(),
                ItemsAS.PERK_GEM_NIGHT.get(),
                ItemsAS.PERK_GEM_SKY.get()
        }, "ATT_PERK_GEMS", 4.75F, 5.25F)
                .addPage(text("ATT_PERK_GEMS.1"))
                .addPage(text("ATT_PERK_GEMS.2"))
                .addPage(text("ATT_PERK_GEMS.3"))
                .addTomeLookup(ItemsAS.PERK_GEM_DAY.get(), 1, ResearchProgression.ATTUNEMENT)
                .addTomeLookup(ItemsAS.PERK_GEM_NIGHT.get(), 1, ResearchProgression.ATTUNEMENT)
                .addTomeLookup(ItemsAS.PERK_GEM_SKY.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resAttuneCrystal = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "ATT_CRYSTAL", 3.5F, 4)
                .addPage(text("ATT_CRYSTAL.1"))
                .addPage(text("ATT_CRYSTAL.2"))
                .addTomeLookup(ItemsAS.ATTUNED_ROCK_CRYSTAL.get(), 0, ResearchProgression.ATTUNEMENT)
                .addTomeLookup(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get(), 0, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resRitualPedestal = new ResearchNode(BlocksAS.RITUAL_PEDESTAL.get(), "RIT_PEDESTAL", 3, 5)
                .addPage(text("RIT_PEDESTAL.1"))
                .addPage(recipe(BlocksAS.RITUAL_PEDESTAL.get()))
                .addTomeLookup(BlocksAS.RITUAL_PEDESTAL.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(structure(StructureTypesAS.PTYPE_RITUAL_PEDESTAL))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resRitualPedestalAcceleration = new ResearchNode(BlocksAS.RITUAL_PEDESTAL.get(), "PED_ACCEL", 1.75F, 5.5F)
                .addPage(text("PED_ACCEL.1"))
                .addPage(text("PED_ACCEL.2"))
                .addPage(text("PED_ACCEL.3"))
                .addPage(text("PED_ACCEL.4"))
                .register(ResearchProgression.ATTUNEMENT);

        //AS-REG-01 (resolved): ResearchNode now declares the (SpriteQuery, String, float, float) constructor, so
        //the only sprite-rendered node in the tree is expressible again.
        //(1.16.5 common/registry/RegistryResearch.java:482-486)
        ResearchNode resAlignmentCharge = new ResearchNode(new SpriteQuery(AssetLoader.TextureLocation.EFFECT, 6, 8, "relay_flare"),
                "QUICK_CHARGE", 0.75F, 4.5F)
                .addPage(text("QUICK_CHARGE.1"))
                .addPage(text("QUICK_CHARGE.2"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resToolChanneling = new ResearchNode(BlocksAS.SPECTRAL_RELAY.get(), "TOOL_CHANNEL", 1.25F, 3.25F)
                .addPage(text("TOOL_CHANNEL.1"))
                .addPage(text("TOOL_CHANNEL.2"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resBlinkWand = new ResearchNode(ItemsAS.BLINK_WAND.get(), "TRAVERSAL_WAND", 2.25F, 3.5F)
                .addPage(text("TRAVERSAL_WAND.1"))
                .addPage(recipe(ItemsAS.BLINK_WAND.get()))
                .addTomeLookup(ItemsAS.BLINK_WAND.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resGrappleWand = new ResearchNode(ItemsAS.GRAPPLE_WAND.get(), "GRAPPLE_WAND", 2.5F, 2.5F)
                .addPage(text("GRAPPLE_WAND.1"))
                .addPage(recipe(ItemsAS.GRAPPLE_WAND.get()))
                .addTomeLookup(ItemsAS.GRAPPLE_WAND.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resToolWands = new ResearchNode(new ItemLike[] {
                ItemsAS.ARCHITECT_WAND.get(),
                ItemsAS.EXCHANGE_WAND.get()
        }, "TOOL_WANDS", 1.5F, 2.25F)
                .addPage(text("TOOL_WANDS.1"))
                .addPage(recipe(ItemsAS.ARCHITECT_WAND.get()))
                .addTomeLookup(ItemsAS.ARCHITECT_WAND.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("TOOL_WANDS.3"))
                .addPage(recipe(ItemsAS.EXCHANGE_WAND.get()))
                .addTomeLookup(ItemsAS.EXCHANGE_WAND.get(), 3, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        resStarlightNetwork.addSourceConnectionFrom(resLens);
        resStarlightNetwork.addSourceConnectionFrom(resLinkingTool);
        resTransmutationOres.addSourceConnectionFrom(resStarlightNetwork);
        resStarmetalOre.addSourceConnectionFrom(resTransmutationOres);
        resCuttingTool.addSourceConnectionFrom(resStarmetalOre);
        resStardust.addSourceConnectionFrom(resCuttingTool);
        resResonatorStructure.addSourceConnectionFrom(resStardust);
        resCelestialGateway.addSourceConnectionFrom(resStardust);
        resAltar3.addSourceConnectionFrom(resStardust);

        resAttunePlayer.addSourceConnectionFrom(resStarmetalOre);
        resPerks.addSourceConnectionFrom(resAttunePlayer);
        resShiftingStar.addSourceConnectionFrom(resPerks);
        resPerkSeal.addSourceConnectionFrom(resPerks);
        resPerkGems.addSourceConnectionFrom(resPerks);
        resAttuneCrystal.addSourceConnectionFrom(resAttunePlayer);
        resRitualPedestal.addSourceConnectionFrom(resAttuneCrystal);
        resRitualPedestalAcceleration.addSourceConnectionFrom(resRitualPedestal);

        //1.16.5 common/registry/RegistryResearch.java:536
        resToolChanneling.addSourceConnectionFrom(resAlignmentCharge);
        resBlinkWand.addSourceConnectionFrom(resToolChanneling);
        resGrappleWand.addSourceConnectionFrom(resToolChanneling);
        resToolWands.addSourceConnectionFrom(resToolChanneling);
    }
}
