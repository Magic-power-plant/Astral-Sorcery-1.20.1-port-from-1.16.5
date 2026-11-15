package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.item.ItemAquamarine;
import hellfirepvp.astralsorcery.common.item.ItemChisel;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet;
import hellfirepvp.astralsorcery.common.item.ItemGlassLens;
import hellfirepvp.astralsorcery.common.item.ItemHandTelescope;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.item.ItemKnowledgeShare;
import hellfirepvp.astralsorcery.common.item.ItemLinkingTool;
import hellfirepvp.astralsorcery.common.item.ItemParchment;
import hellfirepvp.astralsorcery.common.item.ItemResonatingGem;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.item.ItemStardust;
import hellfirepvp.astralsorcery.common.item.ItemStarmetalIngot;
import hellfirepvp.astralsorcery.common.item.ItemTome;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedCelestialCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedRockCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCelestialCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemRockCrystal;
import hellfirepvp.astralsorcery.common.item.dust.ItemIlluminationPowder;
import hellfirepvp.astralsorcery.common.item.dust.ItemNocturnalPowder;
import hellfirepvp.astralsorcery.common.item.gem.ItemPerkGemDay;
import hellfirepvp.astralsorcery.common.item.gem.ItemPerkGemNight;
import hellfirepvp.astralsorcery.common.item.gem.ItemPerkGemSky;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLensBreak;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLensDamage;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLensFire;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLensGrowth;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLensPush;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLensRegeneration;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLensSpectral;
import hellfirepvp.astralsorcery.common.item.tool.ItemCrystalAxe;
import hellfirepvp.astralsorcery.common.item.tool.ItemCrystalPickaxe;
import hellfirepvp.astralsorcery.common.item.tool.ItemCrystalShovel;
import hellfirepvp.astralsorcery.common.item.tool.ItemCrystalSword;
import hellfirepvp.astralsorcery.common.item.tool.ItemInfusedCrystalAxe;
import hellfirepvp.astralsorcery.common.item.tool.ItemInfusedCrystalPickaxe;
import hellfirepvp.astralsorcery.common.item.tool.ItemInfusedCrystalShovel;
import hellfirepvp.astralsorcery.common.item.tool.ItemInfusedCrystalSword;
import hellfirepvp.astralsorcery.common.item.useables.ItemPerkSeal;
import hellfirepvp.astralsorcery.common.item.useables.ItemShiftingStar;
import hellfirepvp.astralsorcery.common.item.useables.ItemShiftingStarAevitas;
import hellfirepvp.astralsorcery.common.item.useables.ItemShiftingStarArmara;
import hellfirepvp.astralsorcery.common.item.useables.ItemShiftingStarDiscidia;
import hellfirepvp.astralsorcery.common.item.useables.ItemShiftingStarEvorsio;
import hellfirepvp.astralsorcery.common.item.useables.ItemShiftingStarVicio;
import hellfirepvp.astralsorcery.common.item.wand.ItemArchitectWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemBlinkWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemExchangeWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemGrappleWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemWand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemsAS {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AstralSorcery.MODID);

    // Every id below resolves to the behaviour class that implements it. Where a bare `new Item(...)` is still used,
    // no behaviour class exists in the tree for that id and the item genuinely is a plain crafting component.
    //
    // Six ids that used to be registered here are gone: knowledge_share_empty, knowledge_share_written,
    // resonator_liquid, resonator_starlight, resonator_structure and infused_glass_engraved. They are not items in
    // either reference version - they are the *model override targets* named by the `written` / `upgrade` /
    // `engraved` ItemProperties registered in RegistryItemModels#registerItemProperties, i.e. the alternate models
    // models/item/knowledge_share.json, resonator.json and infused_glass.json switch to. Registering them as items
    // produced six phantom entries with no lang key (they rendered as "item.astralsorcery.knowledge_share_empty")
    // and no code or recipe ever referenced them. Three more - attunement_parchment, manifold_crystal and
    // resonating_tome - had no class, no model, no lang key, no reference of any kind, and exist in neither
    // reference version; they were removed for the same reason.
    //
    // common/item/quality/ItemDazzlingFrame and ItemDazzlingGem are deliberately NOT registered: their two lang keys
    // (item.astralsorcery.dazzling_frame / .dazzling_gem) are present but empty strings, they have no texture and no
    // model, and 1.21.1 dropped both. 1.16.5 carried them as unfinished placeholders; registering them would add two
    // unnamed, untextured items.

    public static final RegistryObject<Item> TOME = ITEMS.register("tome", () -> new ItemTome(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STARDUST = ITEMS.register("stardust", () -> new ItemStardust(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> ROCK_CRYSTAL = ITEMS.register("rock_crystal", () -> new ItemRockCrystal(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CELESTIAL_CRYSTAL = ITEMS.register("celestial_crystal", () -> new ItemCelestialCrystal(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ATTUNED_ROCK_CRYSTAL = ITEMS.register("attuned_rock_crystal", () -> new ItemAttunedRockCrystal(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ATTUNED_CELESTIAL_CRYSTAL = ITEMS.register("attuned_celestial_crystal", () -> new ItemAttunedCelestialCrystal(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> AQUAMARINE = ITEMS.register("aquamarine", () -> new ItemAquamarine(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> RESONATING_GEM = ITEMS.register("resonating_gem", () -> new ItemResonatingGem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
    //ItemStarmetalIngot overrides hasCustomEntity/createEntity to spawn EntityStarmetalIngot (lightning affinity).
    public static final RegistryObject<Item> STARMETAL_INGOT = ITEMS.register("starmetal_ingot", () -> new ItemStarmetalIngot(new Item.Properties().stacksTo(64)));
    // Note: "starmetal" block item is registered by BlocksAS.registerBlockWithItem; do not register a plain Item here.

    public static final RegistryObject<Item> CONSTELLATION_PAPER = ITEMS.register("constellation_paper", () -> new ItemConstellationPaper(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PARCHMENT = ITEMS.register("parchment", () -> new ItemParchment(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> KNOWLEDGE_SHARE = ITEMS.register("knowledge_share", () -> new ItemKnowledgeShare(new Item.Properties().stacksTo(1)));

    //The six wand/tool classes below all call properties.stacksTo(1) in their own constructor, so ItemsAS must not
    //hand them a Properties that already carries a durability: Item.Properties#stacksTo throws
    //"Unable to have damage AND stack." once maxDamage != 0. The previous placeholders passed durability(512) to
    //architect/blink/exchange/grapple, which would have crashed item registration the moment the real class was
    //wired in. 1.16.5 gives all four maxStackSize(1) and no maxDamage - they consume alignment charge, not
    //durability (see AlignmentChargeConsumer) - and 1.21.1 likewise builds new Properties().stacksTo(1).
    public static final RegistryObject<Item> WAND = ITEMS.register("wand", () -> new ItemWand(new Item.Properties()));
    public static final RegistryObject<Item> ARCHITECT_WAND = ITEMS.register("architect_wand", () -> new ItemArchitectWand(new Item.Properties()));
    public static final RegistryObject<Item> LINK_TOOL = ITEMS.register("linking_tool", () -> new ItemLinkingTool(new Item.Properties()));
    public static final RegistryObject<Item> BLINK_WAND = ITEMS.register("blink_wand", () -> new ItemBlinkWand(new Item.Properties()));
    public static final RegistryObject<Item> EXCHANGE_WAND = ITEMS.register("exchange_wand", () -> new ItemExchangeWand(new Item.Properties()));
    public static final RegistryObject<Item> GRAPPLE_WAND = ITEMS.register("grapple_wand", () -> new ItemGrappleWand(new Item.Properties()));
    public static final RegistryObject<Item> ILLUMINATION_WAND = ITEMS.register("illumination_wand", () -> new ItemIlluminationWand(new Item.Properties()));
    //1.16.5 gives the resonator maxStackSize(1) and no maxDamage; its upgrade state is NBT + player research, not
    //durability (see ItemResonator.ResonatorUpgrade and the "upgrade" ItemProperty). The placeholder's
    //durability(256) would have shown a damage bar the item never uses.
    public static final RegistryObject<Item> RESONATOR = ITEMS.register("resonator", () -> new ItemResonator(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GLASS_LENS = ITEMS.register("glass_lens", () -> new ItemGlassLens(new Item.Properties().stacksTo(16)));
    //1.16.5 ItemChisel: maxDamage(72). The placeholder's 256 matched neither reference (1.21.1 ChiselItem uses
    //durability(64)); 72 is kept because this tree's ItemChisel is the 1.16.5 implementation.
    public static final RegistryObject<Item> CHISEL = ITEMS.register("chisel", () -> new ItemChisel(new Item.Properties().durability(72)));
    public static final RegistryObject<Item> HAND_TELESCOPE = ITEMS.register("hand_telescope", () -> new ItemHandTelescope(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ILLUMINATION_POWDER = ITEMS.register("illumination_powder", () -> new ItemIlluminationPowder(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> NOCTURNAL_POWDER = ITEMS.register("nocturnal_powder", () -> new ItemNocturnalPowder(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> INFUSED_GLASS = ITEMS.register("infused_glass", () -> new ItemInfusedGlass(new Item.Properties().stacksTo(1).durability(5)));
    //1.16.5 gives every shifting star maxStackSize(1) (so does 1.21.1 ShiftingStarItem); the placeholder's 16 let
    //them stack, which the finishUsingItem/perk-reset path does not expect.
    public static final RegistryObject<Item> SHIFTING_STAR = ITEMS.register("shifting_star", () -> new ItemShiftingStar(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SHIFTING_STAR_AEVITAS = ITEMS.register("shifting_star_aevitas", () -> new ItemShiftingStarAevitas(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SHIFTING_STAR_ARMARA = ITEMS.register("shifting_star_armara", () -> new ItemShiftingStarArmara(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SHIFTING_STAR_DISCIDIA = ITEMS.register("shifting_star_discidia", () -> new ItemShiftingStarDiscidia(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SHIFTING_STAR_EVORSIO = ITEMS.register("shifting_star_evorsio", () -> new ItemShiftingStarEvorsio(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SHIFTING_STAR_VICIO = ITEMS.register("shifting_star_vicio", () -> new ItemShiftingStarVicio(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ENCHANTMENT_AMULET = ITEMS.register("enchantment_amulet", () -> new ItemEnchantmentAmulet(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANTLE = ITEMS.register("mantle", () -> new ItemMantle());
    public static final RegistryObject<Item> PERK_SEAL = ITEMS.register("perk_seal", () -> new ItemPerkSeal(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PERK_GEM_DAY = ITEMS.register("perk_gem_day", () -> new ItemPerkGemDay(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PERK_GEM_NIGHT = ITEMS.register("perk_gem_night", () -> new ItemPerkGemNight(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PERK_GEM_SKY = ITEMS.register("perk_gem_sky", () -> new ItemPerkGemSky(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> COLORED_LENS_BREAK = ITEMS.register("colored_lens_break", () -> new ItemColoredLensBreak(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COLORED_LENS_DAMAGE = ITEMS.register("colored_lens_damage", () -> new ItemColoredLensDamage(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COLORED_LENS_FIRE = ITEMS.register("colored_lens_fire", () -> new ItemColoredLensFire(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COLORED_LENS_GROWTH = ITEMS.register("colored_lens_growth", () -> new ItemColoredLensGrowth(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COLORED_LENS_PUSH = ITEMS.register("colored_lens_push", () -> new ItemColoredLensPush(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COLORED_LENS_REGENERATION = ITEMS.register("colored_lens_regeneration", () -> new ItemColoredLensRegeneration(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COLORED_LENS_SPECTRAL = ITEMS.register("colored_lens_spectral", () -> new ItemColoredLensSpectral(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CRYSTAL_SWORD = ITEMS.register("crystal_sword", () -> new ItemCrystalSword(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CRYSTAL_PICKAXE = ITEMS.register("crystal_pickaxe", () -> new ItemCrystalPickaxe(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CRYSTAL_AXE = ITEMS.register("crystal_axe", () -> new ItemCrystalAxe(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CRYSTAL_SHOVEL = ITEMS.register("crystal_shovel", () -> new ItemCrystalShovel(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> INFUSED_CRYSTAL_SWORD = ITEMS.register("infused_crystal_sword", () -> new ItemInfusedCrystalSword(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> INFUSED_CRYSTAL_PICKAXE = ITEMS.register("infused_crystal_pickaxe", () -> new ItemInfusedCrystalPickaxe(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> INFUSED_CRYSTAL_AXE = ITEMS.register("infused_crystal_axe", () -> new ItemInfusedCrystalAxe(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> INFUSED_CRYSTAL_SHOVEL = ITEMS.register("infused_crystal_shovel", () -> new ItemInfusedCrystalShovel(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
}
