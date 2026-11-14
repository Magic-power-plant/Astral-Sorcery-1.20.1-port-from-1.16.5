package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.nojson.AttunementCraftingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.LiquidStarlightCraftingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.WorldFreezingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.WorldMeltableRegistry;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.CreativeTabsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RegistryBootstrap {
    private RegistryBootstrap() {}

    public static void attach(IEventBus modEventBus) {
        CreativeTabsAS.CREATIVE_TABS.register(modEventBus);
        FluidsAS.FLUID_TYPES.register(modEventBus);
        FluidsAS.FLUIDS.register(modEventBus);
        RecipeTypesAS.RECIPE_TYPES.register(modEventBus);
        RecipeSerializersAS.RECIPE_SERIALIZERS.register(modEventBus);
        EffectsAS.MOB_EFFECTS.register(modEventBus);
        BlocksAS.BLOCKS.register(modEventBus);
        ItemsAS.ITEMS.register(modEventBus);
        EntityTypesAS.ENTITY_TYPES.register(modEventBus);
        BlockEntityTypesAS.BLOCK_ENTITY_TYPES.register(modEventBus);
        MenuTypesAS.MENU_TYPES.register(modEventBus);
        SoundsAS.SOUND_EVENTS.register(modEventBus);
        EnchantmentsAS.ENCHANTMENTS.register(modEventBus);
        WorldGenerationAS.FEATURES.register(modEventBus);
        WorldGenerationAS.BiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        WorldGenerationAS.init();
        AstralSorcery.LOG.info("AstralSorcery registries initialized");
    }

    /** Build dependencies before Forge register events and before configuration defaults are serialized. */
    public static void initializeDomainEntries() {
        RegistryConstellations.init();
        RegistryMantleEffects.init();
        RegistryConstellationEffects.init();
        RegistryPerkAttributeTypes.init();
        RegistryCrystalPropertyUsages.init();
        RegistryCrystalProperties.init();
        RegistryCrystalProperties.initDefaultAttributes();
        RegistryEngravingEffects.init();
        RegistryStructureTypes.init();
        RegistryAltarRecipeEffects.init();
    }

    public static void initializeBuiltinRecipes() {
        LiquidStarlightCraftingRegistry.INSTANCE.init();
        AttunementCraftingRegistry.INSTANCE.init();
        WorldMeltableRegistry.INSTANCE.init();
        WorldFreezingRegistry.INSTANCE.init();
    }

    @SubscribeEvent
    public static void createDomainRegistries(NewRegistryEvent event) {
        RegistriesAS.all().forEach(handle -> handle.create(event));
    }

    @SubscribeEvent
    public static void registerDomainEntries(RegisterEvent event) {
        RegistriesAS.all().forEach(handle -> handle.registerEntries(event));
    }

}
