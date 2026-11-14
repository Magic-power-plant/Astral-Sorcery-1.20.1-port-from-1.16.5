package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.perk.PerkConverter;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;


import static hellfirepvp.astralsorcery.common.lib.RegistriesAS.*;

@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryRegistries {
    private RegistryRegistries() {}

    public static void init(IEventBus modEventBus) {
        FluidsAS.FLUID_TYPES.register(modEventBus);
        FluidsAS.FLUIDS.register(modEventBus);
        RecipeTypesAS.RECIPE_TYPES.register(modEventBus);
        RecipeSerializersAS.RECIPE_SERIALIZERS.register(modEventBus);
        EffectsAS.EFFECTS.register(modEventBus);
        BlocksAS.BLOCKS.register(modEventBus);
        ItemsAS.ITEMS.register(modEventBus);
        EntityTypesAS.ENTITIES.register(modEventBus);
        BlockEntityTypesAS.BLOCK_ENTITIES.register(modEventBus);
        MenuTypesAS.MENU_TYPES.register(modEventBus);
        SoundsAS.SOUNDS.register(modEventBus);
        EnchantmentsAS.ENCHANTMENTS.register(modEventBus);
        WorldGenerationAS.FEATURES.register(modEventBus);
        WorldGenerationAS.BiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        WorldGenerationAS.init();
        AstralSorcery.LOG.info("AstralSorcery registries initialized");
    }

    // NewRegistryEvent#create returns a supplier that is only filled after the event, so calling get() here
    // left every RegistriesAS.REGISTRY_* null: the RegisterEvent handlers below skipped their registries and
    // every lookup (crystal attributes, altar effects, ...) failed. The onFill callback runs once the registry
    // exists and before RegisterEvent.
    @SubscribeEvent
    public static void buildRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<IConstellation>()
                .setName(REGISTRY_NAME_CONSTELLATIONS)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_CONSTELLATIONS = registry);

        event.create(new RegistryBuilder<ConstellationEffectProvider>()
                .setName(REGISTRY_NAME_CONSTELLATION_EFFECTS)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_CONSTELLATION_EFFECT = registry);

        event.create(new RegistryBuilder<MantleEffect>()
                .setName(REGISTRY_NAME_MANTLE_EFFECTS)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_MANTLE_EFFECT = registry);

        event.create(new RegistryBuilder<EngravingEffect>()
                .setName(REGISTRY_NAME_ENGRAVING_EFFECT)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_ENGRAVING_EFFECT = registry);

        event.create(new RegistryBuilder<PerkConverter>()
                .setName(REGISTRY_NAME_PERK_ATTRIBUTE_CONVERTERS)
                .disableSaving()
                .allowModification(), registry -> RegistriesAS.REGISTRY_PERK_ATTRIBUTE_CONVERTERS = registry);

        event.create(new RegistryBuilder<PerkAttributeModifier>()
                .setName(REGISTRY_NAME_PERK_CUSTOM_MODIFIERS)
                .disableSaving()
                .allowModification(), registry -> RegistriesAS.REGISTRY_PERK_CUSTOM_MODIFIERS = registry);

        event.create(new RegistryBuilder<StructureType>()
                .setName(REGISTRY_NAME_STRUCTURE_TYPES)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_STRUCTURE_TYPES = registry);

        event.create(new RegistryBuilder<PerkAttributeType>()
                .setName(REGISTRY_NAME_PERK_ATTRIBUTE_TYPES)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_PERK_ATTRIBUTE_TYPES = registry);

        event.create(new RegistryBuilder<PerkAttributeReader>()
                .setName(REGISTRY_NAME_PERK_ATTRIBUTE_READERS)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_PERK_ATTRIBUTE_READERS = registry);

        event.create(new RegistryBuilder<CrystalProperty>()
                .setName(REGISTRY_NAME_CRYSTAL_PROPERTIES)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_CRYSTAL_PROPERTIES = registry);

        event.create(new RegistryBuilder<PropertyUsage>()
                .setName(REGISTRY_NAME_CRYSTAL_USAGES)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_CRYSTAL_USAGES = registry);

        event.create(new RegistryBuilder<AltarRecipeEffect>()
                .setName(REGISTRY_NAME_ALTAR_EFFECTS)
                .disableSaving()
                .disableSync(), registry -> RegistriesAS.REGISTRY_ALTAR_EFFECTS = registry);
    }

    // 1.16.5 built REGISTRY_CONSTELLATIONS with an AddCallback that mirrored every registration into
    // ConstellationRegistry (1.16.5 common/registry/RegistryRegistries.java:47-48): the Forge registry was the
    // source of truth and ConstellationRegistry was the derived index. This tree inverted that - ConstellationsAS
    // only calls ConstellationRegistry.register - so the Forge registry stayed EMPTY while the index held all 16.
    // Verified on a running dedicated server: ConstellationRegistry.getAllConstellations() returned 16 while
    // RegistriesAS.REGISTRY_CONSTELLATIONS.getKeys() was 0 and getValue("astralsorcery:armara") was null.
    //
    // That broke every lookup-by-name and every iteration of the registry, i.e.:
    //   DistributionHandler#... iterates the registry, so the sky had no constellations to distribute;
    //   ItemMantle iterates getValues(), so the mantle offered none;
    //   ActiveCrystalAttunementRecipe / ActivePlayerAttunementRecipe read the in-progress attunement back from
    //     saved NBT through getValue(...) and would have got null after a world reload;
    //   BlockTransmutationSerializer (json and network), SimpleAltarRecipeSerializer, GuiType and
    //     ConstellationInstance all resolve constellations the same way.
    //
    // ConstellationsAS#init runs during mod construction (CommonProxy#initialize), so the objects exist well
    // before RegisterEvent fires. The index keeps being filled directly by ConstellationsAS; this only adds the
    // registry half back, rather than re-adding the AddCallback, which would double-insert into the index.
    @SubscribeEvent
    public static void registerConstellations(RegisterEvent event) {
        if (RegistriesAS.REGISTRY_CONSTELLATIONS == null
                || !event.getRegistryKey().location().equals(REGISTRY_NAME_CONSTELLATIONS)) {
            return;
        }
        for (IConstellation constellation : ConstellationRegistry.getAllConstellations()) {
            event.register(RegistriesAS.REGISTRY_CONSTELLATIONS.getRegistryKey(),
                    constellation.getRegistryName(), () -> constellation);
        }
    }

    // REGISTRY_CONSTELLATION_EFFECT, REGISTRY_MANTLE_EFFECT and REGISTRY_ENGRAVING_EFFECT are deliberately left
    // empty. Unlike the constellations above they have no reader anywhere in this tree - every mention is a port
    // comment - because ConstellationEffectRegistry, MantleEffectRegistry and RegistryEngravingEffects resolve
    // their own entries directly. Filling them would add registrations nothing looks up.

    // 1.16.5 PrimerEventHandler#registerAltarRecipeEffects (common/registry/internal/PrimerEventHandler.java:224-227)
    // called RegistryRecipeTypes.initAltarEffects() from inside the RegistryEvent.Register<AltarRecipeEffect>
    // handler and then flushed the primer into the registry. Same order here, against the key-dispatched
    // RegisterEvent.
    @SubscribeEvent
    public static void registerAltarRecipeEffects(RegisterEvent event) {
        if (RegistriesAS.REGISTRY_ALTAR_EFFECTS == null
                || !event.getRegistryKey().location().equals(REGISTRY_NAME_ALTAR_EFFECTS)) {
            return;
        }
        RegistryRecipeTypes.initAltarEffects();
        for (AltarRecipeEffect effect : RegistryRecipeTypes.getAltarEffects()) {
            event.register(RegistriesAS.REGISTRY_ALTAR_EFFECTS.getRegistryKey(),
                    effect.getRegistryName(), () -> effect);
        }
    }

    // 1.16.5 PrimerEventHandler attached a RegistryEvent.Register<StructureType> listener
    // (common/registry/internal/PrimerEventHandler.java:78) that flushed the primer's StructureType entries;
    // RegistryStructureTypes#init collects them, this flushes them.
    @SubscribeEvent
    public static void registerStructureTypes(RegisterEvent event) {
        if (RegistriesAS.REGISTRY_STRUCTURE_TYPES == null
                || !event.getRegistryKey().location().equals(REGISTRY_NAME_STRUCTURE_TYPES)) {
            return;
        }
        RegistryStructureTypes.init();
        for (StructureType type : RegistryStructureTypes.getStructureTypes()) {
            event.register(RegistriesAS.REGISTRY_STRUCTURE_TYPES.getRegistryKey(),
                    type.getRegistryName(), () -> type);
        }
    }

    // 1.16.5 PrimerEventHandler attached RegistryEvent.Register listeners for CrystalProperty and PropertyUsage
    // (common/registry/internal/PrimerEventHandler.java:89-90, 216-222) which flushed the primer into
    // REGISTRY_CRYSTAL_PROPERTIES / REGISTRY_CRYSTAL_USAGES. Neither registry was ever filled in this tree, which
    // broke common/crystal/CrystalAttributes.Attribute#deserialize - it resolves every stored attribute through
    // REGISTRY_CRYSTAL_PROPERTIES#getValue and silently drops the ones it cannot find. Both collections are built
    // during mod construction (CommonProxy#initialize -> RegistryCrystalPropertyUsages.init /
    // RegistryCrystalProperties.init), so they are complete by the time this fires.
    @SubscribeEvent
    public static void registerCrystalProperties(RegisterEvent event) {
        if (event.getRegistryKey().location().equals(REGISTRY_NAME_CRYSTAL_PROPERTIES)
                && RegistriesAS.REGISTRY_CRYSTAL_PROPERTIES != null) {
            for (CrystalProperty property : RegistryCrystalProperties.getProperties()) {
                event.register(RegistriesAS.REGISTRY_CRYSTAL_PROPERTIES.getRegistryKey(),
                        property.getRegistryName(), () -> property);
            }
            return;
        }
        if (event.getRegistryKey().location().equals(REGISTRY_NAME_CRYSTAL_USAGES)
                && RegistriesAS.REGISTRY_CRYSTAL_USAGES != null) {
            for (PropertyUsage usage : RegistryCrystalPropertyUsages.getUsages()) {
                event.register(RegistriesAS.REGISTRY_CRYSTAL_USAGES.getRegistryKey(),
                        usage.getRegistryName(), () -> usage);
            }
        }
    }

    @SubscribeEvent
    public static void registerBuiltInPerkAttributeReaders(RegisterEvent event) {
        if (event.getRegistryKey().location().equals(REGISTRY_NAME_PERK_ATTRIBUTE_TYPES)
                && RegistriesAS.REGISTRY_PERK_ATTRIBUTE_TYPES != null) {
            for (PerkAttributeType type : RegistryPerkAttributeTypes.getTypes()) {
                event.register(RegistriesAS.REGISTRY_PERK_ATTRIBUTE_TYPES.getRegistryKey(),
                        type.getRegistryName(), () -> type);
            }
            return;
        }
        if (event.getRegistryKey().location().equals(REGISTRY_NAME_PERK_ATTRIBUTE_CONVERTERS)
                && RegistriesAS.REGISTRY_PERK_ATTRIBUTE_CONVERTERS != null) {
            for (PerkConverter converter : RegistryPerkConverters.getConverters()) {
                event.register(RegistriesAS.REGISTRY_PERK_ATTRIBUTE_CONVERTERS.getRegistryKey(),
                        converter.getRegistryName(), () -> converter);
            }
            return;
        }
        if (event.getRegistryKey().location().equals(REGISTRY_NAME_PERK_CUSTOM_MODIFIERS)
                && RegistriesAS.REGISTRY_PERK_CUSTOM_MODIFIERS != null) {
            for (PerkAttributeModifier modifier : RegistryPerkCustomModifiers.getModifiers()) {
                event.register(RegistriesAS.REGISTRY_PERK_CUSTOM_MODIFIERS.getRegistryKey(),
                        modifier.getRegistryName(), () -> modifier);
            }
            return;
        }
        if (RegistriesAS.REGISTRY_PERK_ATTRIBUTE_READERS == null ||
                !event.getRegistryKey().location().equals(REGISTRY_NAME_PERK_ATTRIBUTE_READERS)) {
            return;
        }

        for (PerkAttributeReader reader : RegistryPerkAttributeReaders.getReaders()) {
            event.register(RegistriesAS.REGISTRY_PERK_ATTRIBUTE_READERS.getRegistryKey(),
                    reader.getType().getRegistryName(), () -> reader);
        }
    }
}
