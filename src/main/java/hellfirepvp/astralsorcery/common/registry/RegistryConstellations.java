/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import java.util.function.Supplier;

import static hellfirepvp.astralsorcery.common.lib.ConstellationsAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryConstellations
 * Created by HellFirePvP
 * Date: 01.06.2019 / 15:58
 */
// 1.16.5 RegistryConstellations#init did two things: buildConstellations() and registerSignatureItems().
// In the 1.20.1 target the constellation geometry/registration half already lives in
// common/lib/ConstellationsAS#buildConstellations (called from ConstellationsAS#init), so duplicating it here
// would build and register a second, competing set of constellations. Only the signature-item half -
// which ConstellationsAS does not do - is ported into this class.
//
//AS-REG-02 (resolved): the four Minor constellations' anonymous ConstellationEffectProperties overrides
// (1.16.5 common/registry/RegistryConstellations.java:380-471) now live in
// common/lib/ConstellationsAS#buildConstellations, where the constellations are actually created:
//   gelu    -> multiplyPotency(1.4F); multiplySize(2);
//   ulteria -> multiplyEffectAmplifier(2F); multiplyPotency(0.5F); multiplySize(0.5F);
//   alcara  -> setCorrupted(true); multiplySize(1.2F);
//   vorux   -> multiplyEffectAmplifier(0.8F); multiplyPotency(5F); multiplySize(1.2F);
// Without them IMinorConstellation#affectConstellationEffect fell through to its no-op default, so a Minor
// constellation slotted into a ritual as its trait changed nothing at all - even though the hook has three live
// callers (TileRitualPedestal, StarlightReceiverRitualPedestal and CEffectArmara).
public class RegistryConstellations {

    public static void init() {
        // buildConstellations(); -> see the class comment: owned by common/lib/ConstellationsAS in 1.20.1.

        registerSignatureItems();
    }

    //Deliberately NOT called from CommonProxy, unlike 1.16.5. The signature items this registers are read only by
    //IConstellation#getConstellationSignatureItems, whose sole consumer in 1.16.5 is the build-time datagen
    //provider datagen/data/recipes/altar/RadianceAltarRecipeProvider - nothing reads them at runtime, and the
    //recipes it generated already ship as JSON in this tree. The 1.21.1 source has dropped them entirely
    //(InitConstellations.java:63-66 has every addSignatureItem call commented out). Wiring init() here would
    //therefore add a caller with no observable behaviour; it becomes necessary only if the datagen providers are
    //ported, at which point this must run after ConstellationsAS#init has built the constellations.

    private static void registerSignatureItems() {
        aevitas.addSignatureItem(tag(ItemTags.SAPLINGS));
        aevitas.addSignatureItem(tag(itemTag("stardust")));
        aevitas.addSignatureItem(tag(Tags.Items.SEEDS_WHEAT));
        aevitas.addSignatureItem(Blocks.SUGAR_CANE.asItem());

        discidia.addSignatureItem(Items.FLINT);
        discidia.addSignatureItem(tag(Tags.Items.INGOTS_IRON));
        discidia.addSignatureItem(tag(ItemTags.ARROWS));
        discidia.addSignatureItem(tag(Tags.Items.DUSTS_REDSTONE));

        armara.addSignatureItem(tag(Tags.Items.INGOTS_IRON));
        armara.addSignatureItem(tag(Tags.Items.LEATHER));
        armara.addSignatureItem(Items.CLAY_BALL);
        armara.addSignatureItem(tag(Tags.Items.DUSTS_GLOWSTONE));

        vicio.addSignatureItem(tag(Tags.Items.FEATHERS));
        vicio.addSignatureItem(Items.SUGAR);
        vicio.addSignatureItem(Items.STRING);
        vicio.addSignatureItem(tag(ItemTags.FISHES));

        evorsio.addSignatureItem(tag(Tags.Items.COBBLESTONE));
        evorsio.addSignatureItem(Items.FLINT);
        evorsio.addSignatureItem(tag(Tags.Items.GUNPOWDER));
        evorsio.addSignatureItem(Blocks.TNT.asItem());


        lucerna.addSignatureItem(tag(Tags.Items.DUSTS_GLOWSTONE));
        lucerna.addSignatureItem(Blocks.TORCH.asItem());
        lucerna.addSignatureItem(tag(Tags.Items.DUSTS_REDSTONE));
        lucerna.addSignatureItem(tag(ItemTags.COALS));

        mineralis.addSignatureItem(tag(Tags.Items.ORES_IRON));
        mineralis.addSignatureItem(tag(Tags.Items.INGOTS_GOLD));
        mineralis.addSignatureItem(tag(Tags.Items.INGOTS_IRON));
        mineralis.addSignatureItem(tag(Tags.Items.DUSTS_REDSTONE));

        horologium.addSignatureItem(tag(itemTag("stardust")));
        horologium.addSignatureItem(tag(Tags.Items.GEMS_DIAMOND));
        horologium.addSignatureItem(tag(Tags.Items.ENDER_PEARLS));
        horologium.addSignatureItem(Items.CLOCK);

        octans.addSignatureItem(tag(ItemTags.FISHES));
        octans.addSignatureItem(Items.FISHING_ROD);
        octans.addSignatureItem(tag(Tags.Items.DYES_BLUE));
        octans.addSignatureItem(Items.CLAY_BALL);

        bootes.addSignatureItem(tag(Tags.Items.CROPS_WHEAT));
        bootes.addSignatureItem(tag(Tags.Items.BONES));
        bootes.addSignatureItem(Items.APPLE);
        bootes.addSignatureItem(tag(Tags.Items.LEATHER));

        fornax.addSignatureItem(Items.COAL);
        fornax.addSignatureItem(tag(Tags.Items.DUSTS_REDSTONE));
        fornax.addSignatureItem(tag(Tags.Items.INGOTS_IRON));
        fornax.addSignatureItem(tag(Tags.Items.GUNPOWDER));

        pelotrio.addSignatureItem(Items.ROTTEN_FLESH);
        pelotrio.addSignatureItem(Items.BLAZE_POWDER);
        pelotrio.addSignatureItem(Items.APPLE);
        pelotrio.addSignatureItem(tag(Tags.Items.EGGS));


        gelu.addSignatureItem(Items.SNOWBALL);
        gelu.addSignatureItem(Blocks.ICE.asItem());
        gelu.addSignatureItem(tag(Tags.Items.GEMS_QUARTZ));
        gelu.addSignatureItem(tag(Tags.Items.FEATHERS));

        ulteria.addSignatureItem(tag(itemTag("starmetal")));
        ulteria.addSignatureItem(tag(Tags.Items.LEATHER));
        ulteria.addSignatureItem(tag(Tags.Items.GEMS_DIAMOND));
        ulteria.addSignatureItem(tag(Tags.Items.RODS_BLAZE));

        alcara.addSignatureItem(tag(Tags.Items.CROPS_NETHER_WART));
        alcara.addSignatureItem(tag(Tags.Items.ENDER_PEARLS));
        alcara.addSignatureItem(Blocks.SOUL_SAND.asItem());
        alcara.addSignatureItem(tag(ItemTags.COALS));

        vorux.addSignatureItem(Items.BLAZE_POWDER);
        vorux.addSignatureItem(ItemsAS.NOCTURNAL_POWDER.get());
        vorux.addSignatureItem(tag(Tags.Items.GUNPOWDER));
        vorux.addSignatureItem(Items.NETHER_BRICK);
    }

    // 1.16.5 IConstellation had an addSignatureItem(ITag.INamedTag<Item>) overload. The ported IConstellation only
    // offers (ItemStack), (Item) and (Supplier<Ingredient>), so tag-based signature items go through the supplier
    // overload. Ingredient.of(TagKey) resolves lazily, matching the 1.16.5 tag-wrapper behaviour.
    private static Supplier<Ingredient> tag(TagKey<Item> tag) {
        return () -> Ingredient.of(tag);
    }

    //The mod-owned signature-item tags are datapack data only: data/astralsorcery/tags/items/stardust.json and
    //.../starmetal.json are what decide which items they contain. 1.16.5 kept those two ids in the tag-declaration
    //class common/lib/TagsAS because it also had to build the tag wrappers there; 1.20.1 has no wrapper types and no
    //reason to duplicate what the json already states, so the handle is created from the same id the file is named
    //after, at the point of use. TagDataIntegrityTest guards that both files stay present and non-empty.
    private static TagKey<Item> itemTag(String path) {
        return ItemTags.create(AstralSorcery.key(path));
    }
}
