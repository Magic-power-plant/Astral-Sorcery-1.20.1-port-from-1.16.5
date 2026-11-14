package hellfirepvp.astralsorcery.common;

import hellfirepvp.astralsorcery.common.util.object.SidedReference;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.cmd.CommandAstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.lib.GameRulesAS;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeTypeHandler;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.data.WorldDataDirectory;
import hellfirepvp.astralsorcery.common.data.config.ServerConfig;
import hellfirepvp.astralsorcery.common.data.config.base.BaseConfiguration;
import hellfirepvp.astralsorcery.common.data.config.entry.*;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchIOThread;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletRandomizeHelper;
import hellfirepvp.astralsorcery.common.enchantment.amulet.PlayerAmuletHandler;
import hellfirepvp.astralsorcery.common.event.handler.*;
import hellfirepvp.astralsorcery.common.event.helper.*;
import hellfirepvp.astralsorcery.common.integration.IntegrationCurios;
import hellfirepvp.astralsorcery.common.lib.CreativeTabsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlockProperties;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import hellfirepvp.astralsorcery.common.registry.RegistryBootstrap;
import hellfirepvp.astralsorcery.common.registry.RegistryAdvancements;
import hellfirepvp.astralsorcery.common.registry.RegistryPerks;
import hellfirepvp.astralsorcery.common.registry.RegistryResearch;
import hellfirepvp.astralsorcery.common.registry.RegistryItems;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigRegistries;
import hellfirepvp.astralsorcery.common.data.config.entry.WandsConfig;
import hellfirepvp.astralsorcery.common.data.config.registry.AmuletEnchantmentRegistry;
import hellfirepvp.astralsorcery.common.data.config.registry.EntityTransmutationRegistry;
import hellfirepvp.astralsorcery.common.data.config.registry.FluidRarityRegistry;
import hellfirepvp.astralsorcery.common.data.config.registry.OreBlockRarityRegistry;
import hellfirepvp.astralsorcery.common.data.config.registry.OreItemRarityRegistry;
import hellfirepvp.astralsorcery.common.data.config.registry.TechnicalEntityRegistry;
import hellfirepvp.astralsorcery.common.data.config.registry.BlockEntityAccelerationBlacklistRegistry;
import hellfirepvp.astralsorcery.common.data.config.registry.WeightedPerkAttributeRegistry;
import hellfirepvp.astralsorcery.common.registry.RegistryWorldGeneration;
import hellfirepvp.astralsorcery.common.registry.RegistryMantleEffects;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.patreon.manager.PatreonManager;
import hellfirepvp.astralsorcery.common.util.time.TimeStopController;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktOpenGui;
import hellfirepvp.astralsorcery.common.perk.PerkCooldownHelper;
import hellfirepvp.astralsorcery.common.perk.PerkLevelManager;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.data.PerkTreeLoader;
import hellfirepvp.astralsorcery.common.perk.data.PerkTypeHandler;
import hellfirepvp.astralsorcery.common.perk.source.ModifierManager;
import hellfirepvp.astralsorcery.common.blockentity.treebeacon.TreeBeaconBlockEntity;
import hellfirepvp.astralsorcery.common.auxiliary.gateway.CelestialGatewayHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightNetworkRegistry;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightTransmissionHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightUpdateHandler;
import hellfirepvp.astralsorcery.common.starlight.network.TransmissionChunkTracker;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import hellfirepvp.astralsorcery.common.tick.TickManager;
import hellfirepvp.astralsorcery.common.util.loot.BlockDropCaptureAssist;
import hellfirepvp.astralsorcery.common.event.ServerLifecycleListener;
import hellfirepvp.astralsorcery.common.util.collision.CollisionManager;
import hellfirepvp.astralsorcery.common.auxiliary.BlockBreakHelper;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.base.Mods;
import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.event.BlockChangeNotifier;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.data.world.cache.WorldCacheIOThread;
import hellfirepvp.astralsorcery.common.data.world.cache.WorldCacheManager;
import hellfirepvp.astralsorcery.common.event.handler.EventHandlerWorldCacheIO;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkHandler;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeLimiter;
import hellfirepvp.astralsorcery.common.perk.tick.PerkTickHelper;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class CommonProxy {

    //The three creative tabs moved to common/lib/CreativeTabsAS. Held as plain static fields here they were built by
    //CreativeModeTab.builder() but never registered to Registries.CREATIVE_MODE_TAB, and nothing ever filled them, so
    //the creative menu held no Astral Sorcery item. See CreativeTabsAS and onBuildCreativeTabContents below.

    public static final net.minecraft.world.item.Rarity RARITY_CELESTIAL = net.minecraft.world.item.Rarity.create("AS_CELESTIAL", net.minecraft.ChatFormatting.BLUE);
    public static final net.minecraft.world.item.Rarity RARITY_ARTIFACT = net.minecraft.world.item.Rarity.create("AS_ARTIFACT", net.minecraft.ChatFormatting.GOLD);
    public static final net.minecraft.world.item.Rarity RARITY_VESTIGE = net.minecraft.world.item.Rarity.create("AS_VESTIGE", net.minecraft.ChatFormatting.RED);

    public static final net.minecraft.world.item.ArmorMaterial ARMOR_MATERIAL_IMBUED_LEATHER = hellfirepvp.astralsorcery.common.item.armor.ArmorMaterialImbuedLeather.INSTANCE;

    public static final DamageSource DAMAGE_SOURCE_BLEED = new DamageSource(Holder.direct(new DamageType("astralsorcery.bleed", 0.1F)));
    public static final DamageSource DAMAGE_SOURCE_STELLAR = new DamageSource(Holder.direct(new DamageType("astralsorcery.stellar", 0.1F)));
    public static final DamageSource DAMAGE_SOURCE_REFLECT = new DamageSource(Holder.direct(new DamageType("astralsorcery.reflect", 0.1F)));

    private final CommonScheduler commonScheduler = new CommonScheduler();
    private final TickManager tickManager = new TickManager();
    private final List<ServerLifecycleListener> serverLifecycleListeners = Lists.newArrayList();

    public TickManager getTickManager() {
        return this.tickManager;
    }

    private CommonConfig commonConfig;
    private ServerConfig serverConfig;

    public void initialize() {
        AstralSorcery.LOG.info("Initializing AstralSorcery CommonProxy");
        this.commonConfig = new CommonConfig();
        this.serverConfig = new ServerConfig();
        initializeConfigurations();
        //1.16.5 CommonProxy.java:177 - must run after initializeConfigurations has added the data registries and
        //before the spec is built, or ConfigDataAdapter#registryStore stays null.
        ConfigRegistries.getRegistries().buildDataRegistries(this.serverConfig);
        //serverConfig is built in #onCommonSetup, NOT here: 1.16.5 does it at CommonProxy.java:354, and the
        //difference is load-bearing. buildConfiguration() serializes each entry's defaults, and
        //WeightedPerkAttributeRegistry's defaults hold PerkAttributeType references whose registry names are only
        //assigned by RegistryPerkAttributeTypes.init() further down this very method. Building here threw
        //NullPointerException("... because this.type is null") out of PerkAttributeEntry#serialize and the mod
        //failed to construct outright. A SERVER-type ModConfig registered from FMLCommonSetupEvent is still loaded
        //in time, because server configs are read at world load.
        this.commonConfig.buildConfiguration();
        SyncDataHolder.initialize();
        TransmissionClassRegistry.setupRegistry();
        SourceClassRegistry.setupRegistry();
        StarlightNetworkRegistry.setupRegistry();
        AltarRecipeTypeHandler.init();
        //AS-CFG-02: 1.16.5 PrimerEventHandler.java:109. This is the only caller of the four non-json crafting
        //registries' init() (LiquidStarlightCraftingRegistry, AttunementCraftingRegistry, WorldMeltableRegistry,
        //WorldFreezingRegistry). Without it TileAttunementAltar iterates an empty registry, so player and crystal
        //attunement never start - a hard progression gate - and rock crystals never grow, celestial and gem
        //clusters never form, crystals never merge, infused wood never forms and Fornax melts nothing.
        RegistryBootstrap.initializeBuiltinRecipes();
        //1.16.5 CommonProxy.java:171. Registers the "astralsorcery:default" perk converter. Every other perk type
        //self-registers from a static field initialiser, but DEFAULT is built with `new Type<>` and only reaches
        //CONVERTER_MAP through here, so without this call PerkTypeHandler#hasCustomType(DEFAULT.getKey()) is false
        //and a perk json carrying an explicit "perk_class": "astralsorcery:default" throws JsonParseException,
        //which aborts the whole tree load. None of the 418 shipped perks use that spelling, but datapacks may.
        PerkTypeHandler.init();
        //LootAS.Functions are registered from RegistryLoot#registerLootFunctions (RegisterEvent)
        PacketChannel.registerPackets();
        //RegistryAdvancements.init() assigns every AdvancementsAS trigger field. Without it those five fields
        //stayed null, so the 11 astralsorcery advancements failed to load ("Invalid criterion trigger:
        //astralsorcery:find_constellation", ...) AND ResearchManager#... threw NPE the first time a player
        //discovered a constellation, attuned, gained a perk level or finished an altar craft.
        //CriteriaTriggers in 1.20.1 is still a plain HashMap (not a frozen registry), so mod construction is the
        //right time, exactly as in 1.16.5 (common/CommonProxy.java:169).
        RegistryAdvancements.init();
        ModifierManager.init();
        RegistryBootstrap.initializeDomainEntries();
        attachTickListeners(this.tickManager::register);

        this.serverLifecycleListeners.add(ResearchIOThread.getInstance());
        this.serverLifecycleListeners.add(ServerLifecycleListener.wrap(EventHandlerCache::onServerStart, EventHandlerCache::onServerStop));
        this.serverLifecycleListeners.add(ServerLifecycleListener.wrap(CelestialGatewayHandler.INSTANCE::onServerStart, CelestialGatewayHandler.INSTANCE::onServerStop));
        this.serverLifecycleListeners.add(ServerLifecycleListener.start(PerkTree.PERK_TREE::setupServerPerkTree));
        this.serverLifecycleListeners.add(ServerLifecycleListener.start(PerkLevelManager::loadPerkLevels));
        this.serverLifecycleListeners.add(ServerLifecycleListener.stop(BlockBreakHelper::clearServerCache));
        this.serverLifecycleListeners.add(ServerLifecycleListener.stop(TreeBeaconBlockEntity.TreeWatcher::clearServerCache));
        //1.16.5 CommonProxy.java:189. PlayerAffectionFlags keeps a static per-UUID map, so it has to be dropped
        //on server stop like every other server-side cache here.
        this.serverLifecycleListeners.add(ServerLifecycleListener.stop(PlayerAffectionFlags::clearServerCache));
    }

    protected void initializeConfigurations() {
        //AS-CFG-01: the nine config-driven data registries of 1.16.5 CommonProxy#initializeConfigurations
        //(1.16.5 CommonProxy.java:263-271). None of them was registered, and ConfigDataAdapter#getConfiguredValues
        //short-circuits to an empty list while its registryStore is null, so every consumer silently saw "no data":
        //  AmuletEnchantmentRegistry      -> no enchantment could be influenced by an Enchantment Amulet
        //  WeightedPerkAttributeRegistry  -> perk gem attribute rolls
        //  OreBlockRarityRegistry.MINERALIS_RITUAL / STONE_ENRICHMENT -> the Mineralis ritual and the Stone
        //                                    Enrichment perk produced nothing
        //  OreItemRarityRegistry.VOID_TRASH_REWARD -> the Void Trash perk zeroes the stack BEFORE looking up a
        //                                    reward, so an empty registry meant net item destruction
        //  EntityTransmutationRegistry    -> Pelotrio
        //  FluidRarityRegistry            -> chunk fluid rarity
        //and two of them failed OPEN rather than closed: TechnicalEntityRegistry and
        //TileAccelerationBlacklistRegistry permitted everything.
        ConfigRegistries.getRegistries().addDataRegistry(FluidRarityRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(TechnicalEntityRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(BlockEntityAccelerationBlacklistRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(AmuletEnchantmentRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(WeightedPerkAttributeRegistry.INSTANCE);
        ConfigRegistries.getRegistries().addDataRegistry(OreItemRarityRegistry.VOID_TRASH_REWARD);
        ConfigRegistries.getRegistries().addDataRegistry(OreBlockRarityRegistry.STONE_ENRICHMENT);
        ConfigRegistries.getRegistries().addDataRegistry(OreBlockRarityRegistry.MINERALIS_RITUAL);
        ConfigRegistries.getRegistries().addDataRegistry(EntityTransmutationRegistry.INSTANCE);

        //1.16.5 CommonProxy.java:273-274. The TileTreeBeacon one is not optional: all six of its ForgeConfigSpec
        //fields stayed null, and TileTreeBeacon#doHarvestCycle dereferences Config.CONFIG.maxCount.get() on every
        //tick, so a saved world containing a Tree Beacon threw ReportedException("Ticking block entity") the
        //moment its chunk loaded.
        ToolsConfig.CONFIG.newSubSection(WandsConfig.CONFIG);
        MachineryConfig.CONFIG.newSubSection(TreeBeaconBlockEntity.Config.CONFIG);

        this.serverConfig.addConfigEntry(GeneralConfig.CONFIG);
        this.serverConfig.addConfigEntry(ToolsConfig.CONFIG);
        this.serverConfig.addConfigEntry(EntityConfig.CONFIG);
        this.serverConfig.addConfigEntry(CraftingConfig.CONFIG);
        this.serverConfig.addConfigEntry(LightNetworkConfig.CONFIG);
        this.serverConfig.addConfigEntry(LogConfig.CONFIG);
        this.serverConfig.addConfigEntry(PerkConfig.CONFIG);
        //1.16.5 CommonProxy.java:286 - registers the 24 perk sub-sections (root perks, key perks and the two
        //attribute types) under the perk config. RegistryPerks#initConfig existed but had no caller, so none of
        //those sub-sections reached the generated server config.
        RegistryPerks.initConfig(PerkConfig.CONFIG::newSubSection);
        this.serverConfig.addConfigEntry(AmuletRandomizeHelper.CONFIG);
        this.serverConfig.addConfigEntry(MachineryConfig.CONFIG);
        this.commonConfig.addConfigEntry(WorldGenConfig.CONFIG);
        //1.16.5 CommonProxy.java:291. Without this the CFG_* entries of WorldGenerationAS.Config never get a
        //ForgeConfigSpec, so isEnabled()/everyBiome/worldNames are null and every worldgen gate NPEs.
        RegistryWorldGeneration.addConfigEntries(WorldGenConfig.CONFIG::newSubSection);

        ConstellationEffectRegistry.addConfigEntries(this.serverConfig);
        MantleEffectRegistry.addConfigEntries(this.serverConfig);
    }

    public void attachLifecycle(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onEnqueueIMC);
        modEventBus.addListener(this::onBuildCreativeTabContents);
        modEventBus.addListener(BaseConfiguration::refreshConfiguration);
        RegistryBootstrap.attach(modEventBus);
    }

    //1.16.5 pushed items into a tab at construction time (Item.Properties#group) and overrode Item#fillItemGroup for
    //the ones with NBT variants. 1.20.1 removed both: tab contents are assembled here, per tab, and Forge fires this
    //on the logical client only.
    //
    //Items that implement CreativeTabItem pick their own tab and decide what stacks to emit - that is what gives the
    //fillCreativeTab overrides (constellation papers, attuned crystals, mantle variants, pre-attributed crystal tools
    //and lenses, ...) a caller; until now every one of them was dead code. Anything else goes into the main tab as a
    //single plain stack, which is what 1.16.5's blanket .group(ITEM_GROUP_AS) did for those items. BlockItems whose
    //block asks for a specific tab through CustomItemBlockProperties#getItemGroup are honoured - that accessor is
    //1.16.5's per-block override and had no caller either.
    private void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        for (RegistryObject<Item> entry : ItemsAS.ITEMS.getEntries()) {
            Item item = entry.get();
            if (item instanceof CreativeTabItem tabItem) {
                if (tabItem.isInTab(event.getTab())) {
                    tabItem.fillCreativeTab(event);
                }
                continue;
            }
            if (event.getTab() == this.getDefaultTabFor(item)) {
                event.accept(item);
            }
        }
    }

    private CreativeModeTab getDefaultTabFor(Item item) {
        if (item instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof CustomItemBlockProperties properties
                && properties.getItemGroup() != null) {
            return properties.getItemGroup();
        }
        return CreativeTabsAS.TAB_AS.get();
    }

    public void attachEventHandlers(IEventBus eventBus) {
        eventBus.addListener(this::onServerStarting);
        eventBus.addListener(this::onServerStopping);
        eventBus.addListener(this::onServerStarted);
        eventBus.addListener(this::onServerStopped);
        eventBus.addListener(this::onPlayerLogin);
        eventBus.addListener(this::onPlayerLogout);
        eventBus.addListener(this::onPlayerClone);
        eventBus.addListener(this::onRegisterCommands);
        eventBus.addListener(this::onRegisterReloadListeners);
        this.tickManager.attachListeners(eventBus);
        EventHandlerWorldCacheIO.init(eventBus);

        EventHandlerInteract.attachListeners(eventBus);
        EventHandlerCache.attachListeners(eventBus);
        EventHandlerBlockStorage.attachListeners(eventBus);
        EventHandlerMisc.attachListeners(eventBus);
        PerkAttributeLimiter.attachListeners(eventBus);
        EventHelperSpawnDeny.attachListeners(eventBus);
        EventHelperInvulnerability.attachListeners(eventBus);
        RegistryMantleEffects.attachEventListeners(eventBus);
        EventHelperEntityFreeze.attachListeners(eventBus);
        EventHelperDamageCancelling.attachListeners(eventBus);
        TransmissionChunkTracker.INSTANCE.attachListeners(eventBus);

        eventBus.addListener(PlayerAmuletHandler::onEnchantmentAdd);
        eventBus.addListener(BlockDropCaptureAssist.INSTANCE::onDrop);
        //AS-CFG-04: 1.16.5 CommonProxy.java:228. CelestialGatewayHandler#loadIntoCache is the only writer of the
        //server-side SidedReference and onWorldInit is its only caller, so without this every addPosition logged
        //"Couldn't add gateway at ... - loading the world failed" and gateways persisted in an existing world were
        //never read back into the runtime cache.
        eventBus.addListener(CelestialGatewayHandler.INSTANCE::onWorldInit);

        BlockChangeNotifier.addListener(new EventHandlerAutoLink());
    }

    public File getASServerDataDirectory() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            // A null File parent would silently put playerdata in the process working directory.
            throw new IllegalStateException("Cannot access Astral Sorcery save data without an active server");
        }
        // Resolve against the active save, including during level loading before ServerStartingEvent.
        // Do not retain a directory from the previous integrated-server session.
        return WorldDataDirectory.getOrCreate(server.getWorldPath(LevelResource.ROOT));
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandAstralSorcery.register(event.getDispatcher());
    }

    //1.16.5 CommonProxy.java:212 + 375-377. PerkTreeLoader is a SimpleJsonResourceReloadListener over
    //data/astralsorcery/perks (418 files ship in this tree), and registering it here is the ONLY thing that ever
    //calls PerkTree#updateOriginPerkTree. Without it loadedPerkTree stayed null, so setupServerPerkTree logged
    //"No PerkTree data found!" and updateTreeData never ran - which means AbstractPerk#validate was never called
    //on any perk, so not a single perk existed, not one perk event listener was attached, perk allocation could
    //not resolve a perk, and attunement could not grant a root perk. The entire perk module was dead from this
    //one missing line.
    private void onRegisterReloadListeners(AddReloadListenerEvent event) {
        event.addListener(PerkTreeLoader.INSTANCE);
    }

    private void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        File dataDirectory = WorldDataDirectory.getOrCreate(server.getWorldPath(LevelResource.ROOT));
        File legacyDirectory = new File(server.getServerDirectory(), AstralSorcery.MODID);
        if (!legacyDirectory.toPath().toAbsolutePath().normalize().equals(dataDirectory.toPath())
                && (new File(legacyDirectory, "playerdata").exists()
                || new File(legacyDirectory, "gateway_filter.dat").exists())) {
            // The old shared directory contains no world identity. Importing it into every save would
            // preserve the cross-world progression leak. Leave it untouched for an explicit migration.
            AstralSorcery.LOG.warn("Legacy Astral Sorcery data found at {}. This world uses {}. "
                            + "Legacy data is NOT imported automatically because its world is unknown. "
                            + "With the server stopped, back up both locations and copy only the intended "
                            + "world's playerdata and gateway_filter.dat; see docs/playerdata-storage.md.",
                    legacyDirectory.getAbsolutePath(), dataDirectory.getAbsolutePath());
        }
    }

    private void onServerStarted(ServerStartedEvent event) {
        WorldCacheIOThread.onServerStart();
        this.serverLifecycleListeners.forEach(ServerLifecycleListener::onServerStart);
    }

    private void onServerStopping(ServerStoppingEvent event) {
        ResearchHelper.clearServerCache();
        LinkHandler.clearServer();
        this.serverLifecycleListeners.forEach(ServerLifecycleListener::onServerStop);
        WorldCacheManager.scheduleSaveAll();
        WorldCacheIOThread.onServerStop();
    }

    private void onServerStopped(ServerStoppedEvent event) {
        WorldCacheManager.cleanUp();
    }

    //1.16.5 CommonProxy has no world load/unload listener at all: the starlight network is loaded lazily
    //through DataAS.DOMAIN_AS.getData(world, DataAS.KEY_STARLIGHT_NETWORK) (WorldCacheDomain), and the
    //unload side is handled by EventHandlerCache#onUnload -> StarlightTransmissionHandler#informWorldUnload,
    //exactly as in 1.16.5. The listeners that used to live here only existed to drive the invented
    //StarlightNetworkSavedData / WorldNetworkHandler static cache, both of which are now gone.

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            ResearchHelper.loadPlayerKnowledge(sp);
            PacketChannel.sendToPlayer(sp, new hellfirepvp.astralsorcery.common.network.packet.PktSyncKnowledge(
                    ResearchHelper.getProgressServer(sp)));
        }
    }

    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        ResearchHelper.savePlayerKnowledge(player);
    }

    private void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player original = event.getOriginal();
            Player player = event.getEntity();
            original.reviveCaps();
            ResearchHelper.getProgress(player, LogicalSide.SERVER).acceptMergeFrom(
                    ResearchHelper.getProgress(original, LogicalSide.SERVER));
            original.invalidateCaps();
        }
    }

    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        registrar.accept(WorldCacheManager.getInstance());
        registrar.accept(this.commonScheduler);
        registrar.accept(StarlightTransmissionHandler.getInstance());
        registrar.accept(StarlightUpdateHandler.getInstance());
        //1.16.5 CommonProxy#attachTickListeners:242. Without this the SyncDataHolder dirty set is never drained,
        //so PktSyncData is never broadcast and no client sees light connections, block endpoints, time freeze
        //state or patreon flares change.
        registrar.accept(SyncDataHolder.getTickInstance());
        registrar.accept(LinkHandler.getInstance());
        //SkyHandler drives WorldContext (the constellation day cycle). It implements ITickHandler but was never
        //registered, so getContext(level, SERVER) stayed null forever. (1.16.5 common/CommonProxy.java:244)
        registrar.accept(SkyHandler.getInstance());
        registrar.accept(PlayerAmuletHandler.INSTANCE);
        registrar.accept(PerkTickHelper.INSTANCE);
        //Both were fully implemented but had no caller: PatreonManager never synced its flare data, and
        //TimeStopController never despawned or ticked its TimeStopZones, so a time freeze lasted forever.
        //(1.16.5 common/CommonProxy.java:247-248)
        registrar.accept(PatreonManager.INSTANCE);
        registrar.accept(TimeStopController.INSTANCE);
        registrar.accept(AlignmentChargeHandler.INSTANCE);
        registrar.accept(ModifierManager.INSTANCE);
        EventHelperTemporaryFlight.attachTickListener(registrar);
        EventHelperInvulnerability.attachTickListener(registrar);
        EventHelperSpawnDeny.attachTickListener(registrar);
        EventHelperEntityFreeze.attachTickListener(registrar);
        registrar.accept(EventHelperEnchantmentTick.INSTANCE);
        //Registers the two TimeoutListContainers that expire perk cooldowns on each side; without them a perk
        //cooldown started by CooldownPerk never ran out. (1.16.5 common/CommonProxy.java:257)
        PerkCooldownHelper.attachTickListeners(registrar);
        //AS-WORLD-11 (resolved): 1.16.5 CommonProxy.java:258. This registers the TimeoutListContainer that expires
        //affection flags; without it a flag set by ConstellationEffect#playEffect would never time out - and with
        //the reader stubbed to false it was never read either, so MantleEffectVicio charged for ritual flight.
        PlayerAffectionFlags.attachTickListeners(registrar);
    }

    public boolean arePatreonEffectsEnabled() { return true; }

    @javax.annotation.Nullable
    public net.minecraft.world.entity.player.Player findClientPlayer(java.util.UUID id) { return null; }

    @javax.annotation.Nullable
    public net.minecraft.world.item.crafting.RecipeManager getClientRecipeManager() { return null; }

    public void scheduleClientside(Runnable r, int tickDelay) {}

    public void scheduleClientside(Runnable r) {
        this.scheduleClientside(r, 0);
    }

    public void scheduleDelayed(Runnable r, int tickDelay) {
        this.commonScheduler.addRunnable(r, tickDelay);
    }

    public void scheduleDelayed(Runnable r) {
        this.scheduleDelayed(r, 0);
    }

    public void openGui(Player player, GuiType type, Object... data) {
        if (player instanceof ServerPlayer sp) {
            PktOpenGui pkt = new PktOpenGui(type, type.serializeArguments(data));
            PacketChannel.sendToPlayer(sp, pkt);
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        //1.16.5 CommonProxy.java:354. Deliberately not in #initialize - see the note there.
        this.serverConfig.buildConfiguration();
        CollisionManager.init();
        GameRulesAS.IGNORE_SKYLIGHT_CHECK_RULE = GameRules.register("asIgnoreSkylightCheck",
                GameRules.Category.UPDATES, GameRules.BooleanValue.create(false));
        //AS-CFG-03: 1.16.5 PrimerEventHandler.java:111. Deliberately here and NOT in #initialize: that runs from
        //the mod constructor, and RegistryResearch immediately calls ItemsAS.ROCK_CRYSTAL.get() /
        //BlocksAS.OBSERVATORY.get(), which throw "Registry Object not present" that early. Without this call the
        //entire Astral Tome was empty - 76 research nodes, 261 journal pages and the 106-entry BookLookupRegistry
        //item-to-page lookup all had no data.
        RegistryResearch.init();
        //1.16.5 PrimerEventHandler.java:126. Vanilla DispenseItemBehavior#bootStrap registers bucket behaviour
        //per hardcoded item, so without this the Liquid Starlight bucket fell through to
        //DefaultDispenseItemBehavior and a dispenser spat it out as an item instead of placing the fluid.
        event.enqueueWork(RegistryItems::registerDispenseBehaviors);
        event.enqueueWork(hellfirepvp.astralsorcery.common.registry.extension.ExtensionRegistration::complete);
        AstralSorcery.LOG.info("AstralSorcery common setup complete");
    }

    private void onEnqueueIMC(InterModEnqueueEvent event) {
        Mods.CURIOS.executeIfPresent(() -> IntegrationCurios::initIMC);
    }
}
