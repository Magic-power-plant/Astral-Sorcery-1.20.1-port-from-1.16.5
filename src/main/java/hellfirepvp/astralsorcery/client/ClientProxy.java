/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.data.config.ClientConfig;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.effect.handler.EffectUpdater;
import hellfirepvp.astralsorcery.client.event.*;
import hellfirepvp.astralsorcery.client.event.effect.EffectRenderEventHandler;
import hellfirepvp.astralsorcery.client.event.effect.LightbeamRenderHelper;
import hellfirepvp.astralsorcery.client.registry.RegistryKeyBindings;
import hellfirepvp.astralsorcery.client.render.entity.layer.StarryLayerRenderer;
import hellfirepvp.astralsorcery.client.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.resource.AssetPreLoader;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournal;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalConstellationOverview;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPerkTree;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.common.base.patreon.manager.PatreonManagerClient;
import hellfirepvp.astralsorcery.client.screen.journal.bookmark.BookmarkProvider;
import hellfirepvp.astralsorcery.client.util.AreaOfInfluencePreview;
import hellfirepvp.astralsorcery.client.util.ColorizationHelper;
import hellfirepvp.astralsorcery.client.util.MouseUtil;
import hellfirepvp.astralsorcery.client.util.camera.CameraEventHelper;
import hellfirepvp.astralsorcery.client.structure.StructurePreviewHandler;
import hellfirepvp.astralsorcery.client.util.camera.ClientCameraManager;
import hellfirepvp.astralsorcery.client.util.draw.RenderInfo;
import hellfirepvp.astralsorcery.client.util.word.RandomWordGenerator;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.registry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientProxy
 * Created by HellFirePvP
 * Date: 19.04.2019 / 18:38
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    private ClientScheduler clientScheduler;

    private ClientConfig clientConfig;

    @Override
    public void initialize() {
        this.clientScheduler = new ClientScheduler();

        // 1.16.5 registered the four reload listeners right here, by casting Minecraft#getResourceManager() to
        // IReloadableResourceManager. 1.20.1/Forge routes that through the mod-bus RegisterClientReloadListenersEvent
        // (see registerReloadListeners below, attached in attachLifecycle) - the cast still exists but the resource
        // manager is not guaranteed to be constructed while the mod instance is being built.

        this.clientConfig = new ClientConfig();

        super.initialize();

        this.addTomeBookmarks();
        RandomWordGenerator.init();

        this.clientConfig.buildConfiguration();

        AstralSorcery.LOG.info("AstralSorcery ClientProxy initialized");
    }

    // 1.16.5: inlined in initialize() against IReloadableResourceManager#addReloadListener
    // (1.16.5 client/ClientProxy.java:78-92)
    private void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        // 1.16.5: AstralSorcery.isDoingDataGeneration() - the target AstralSorcery class does not carry that flag,
        // Forge's own DatagenModLoader#isRunningDataGen() is the same information.
        if (!DatagenModLoader.isRunningDataGen()) {
            event.registerReloadListener(AssetLibrary.INSTANCE);
            event.registerReloadListener(AssetPreLoader.INSTANCE);
            event.registerReloadListener(ColorizationHelper.onReload());
            // 1.16.5: IFutureReloadListener lambda -> 1.20.1: PreparableReloadListener lambda, same six parameters;
            // stage.markCompleteAwaitingOthers(Unit.INSTANCE) -> PreparationBarrier#wait(Unit.INSTANCE)
            event.registerReloadListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) ->
                    stage.wait(Unit.INSTANCE).thenRunAsync(() -> {
                        //TODO_PORT[AS-CLI-14]: 1.16.5 guarded this with
                        // `if (!SelectiveReloadStateHandler.INSTANCE.get().test(VanillaResourceType.LANGUAGES)) return;`
                        // so the perk text caches were only cleared when the language files were part of the reload.
                        // Forge 1.20.1 removed SelectiveReloadStateHandler/VanillaResourceType entirely (selective
                        // reloads are gone), so the caches are now cleared on every client resource reload - a
                        // superset of the reference behaviour. (1.16.5 client/ClientProxy.java:85-87)

                        // 1.16.5: PerkTree.PERK_TREE.getPerkPoints(CLIENT) returned Collection<PerkTreePoint<?>> and
                        // needed a .map(PerkTreePoint::getPerk); the 1.20.1 PerkTree returns the perks directly.
                        PerkTree.PERK_TREE.getPerkPoints(LogicalSide.CLIENT).stream()
                                .forEach(AbstractPerk::clearClientTextCaches);
                    }));
        }
    }

    @Override
    protected void initializeConfigurations() {
        super.initializeConfigurations();

        this.clientConfig.addConfigEntry(RenderingConfig.CONFIG);
    }

    @Override
    public void attachLifecycle(IEventBus modEventBus) {
        super.attachLifecycle(modEventBus);

        modEventBus.addListener(RegistryItems::registerColors);
        modEventBus.addListener(RegistryBlocks::registerColors);
        modEventBus.addListener(this::onClientSetup);
        // 1.16.5 called RegistryEntityTypes.initClient()/RegistryTileEntities.initClient() from onClientSetup;
        // in 1.20.1 renderer registration is the dedicated mod-bus EntityRenderersEvent.RegisterRenderers
        modEventBus.addListener(RegistryEntityTypes::initClient);
        modEventBus.addListener(RegistryBlockEntityTypes::initClient);
        modEventBus.addListener(this::registerReloadListeners);
        // 1.16.5 attached the starry player layers from onClientSetup by reaching into the skin map;
        // 1.20.1 exposes EntityRenderersEvent.AddLayers for exactly that, fired whenever the renderers are rebuilt
        modEventBus.addListener(this::onAddLayers);
        //1.16.5 onClientSetup called RegistryKeyBindings.init(); Forge 1.20.1 removed
        //ClientRegistry.registerKeyBinding, so key mappings are registered from the mod-bus
        //RegisterKeyMappingsEvent instead. Without this listener nothing registers the key binding and
        //PktToggleClientOption - the perk ability toggle - has no sender at all.
        //(1.16.5 client/ClientProxy.java:182)
        modEventBus.addListener(RegistryKeyBindings::init);
        //AS-CLI-13 (resolved): the astral sky. 1.16.5 installed it lazily per frame through
        //DimensionRenderInfo#setSkyRenderHandler; Forge 1.20.1 deleted that API, so the ChainingSkyRenderer is
        //registered once here through RegisterDimensionSpecialEffectsEvent. Without this,
        //client/sky/ChainingSkyRenderer - a complete DimensionSpecialEffects subclass - had no construction site
        //and the starfield, in-sky constellations, astral sun/moon and eclipse sky never rendered at all.
        modEventBus.addListener(SkyRenderEventHandler::onRegisterSkyEffects);
    }

    @Override
    public void attachEventHandlers(IEventBus eventBus) {
        super.attachEventHandlers(eventBus);

        EffectRenderEventHandler.getInstance().attachEventListeners(eventBus);
        AlignmentChargeRenderer.INSTANCE.attachEventListeners(eventBus);
        PerkExperienceRenderer.INSTANCE.attachEventListeners(eventBus);
        ItemHeldEffectRenderer.INSTANCE.attachEventListeners(eventBus);
        OverlayRenderer.INSTANCE.attachEventListeners(eventBus);

        CameraEventHelper.attachEventListeners(eventBus);
        MouseUtil.attachEventListeners(eventBus);
        GatewayInteractionHandler.attachEventListeners(eventBus);
        //AS-ITM-01: renders the Resonating Wand's multiblock ghost preview on RenderLevelStageEvent.
        StructurePreviewHandler.getInstance().attachEventListeners(eventBus);

        eventBus.addListener(EventPriority.LOWEST, SkyRenderEventHandler::onFog);
    }

    @Override
    public void attachTickListeners(Consumer<ITickHandler> registrar) {
        super.attachTickListeners(registrar);

        registrar.accept(this.clientScheduler);
        registrar.accept(RenderInfo.getInstance());
        registrar.accept(EffectUpdater.getInstance());
        //AS-CLI-16 (resolved): the note claimed PatreonManagerClient was "a stub without the INSTANCE singleton".
        //Stale - it implements ITickHandler at :36 and declares INSTANCE at :38. (1.16.5 client/ClientProxy.java:145)
        registrar.accept(PatreonManagerClient.INSTANCE);
        registrar.accept(ClientCameraManager.INSTANCE);
        registrar.accept(TimeStopEffectHandler.INSTANCE);
        registrar.accept(AlignmentChargeRenderer.INSTANCE);
        registrar.accept(PerkExperienceRenderer.INSTANCE);
        registrar.accept(AreaOfInfluencePreview.INSTANCE);

        LightbeamRenderHelper.attachTickListener(registrar);
        EffectRenderEventHandler.getInstance().attachTickListeners(registrar);
        //AS-ITM-01: expires the wand's structure preview once the multiblock completes or the player walks away.
        StructurePreviewHandler.getInstance().attachTickHandlers(registrar);
    }

    @Override
    public void scheduleClientside(Runnable r, int tickDelay) {
        this.clientScheduler.addRunnable(r, tickDelay);
    }

    // 1.16.5 CommonProxy declared openGuiClient(GuiType, CompoundNBT); the 1.20.1 CommonProxy does not (yet),
    // so this cannot carry @Override.
    public void openGuiClient(GuiType type, CompoundTag data) {
        Screen toOpen = type.deserialize(data);
        if (toOpen != null) {
            // 1.16.5: Minecraft#displayGuiScreen -> 1.20.1: Minecraft#setScreen
            Minecraft.getInstance().setScreen(toOpen);
        }
    }

    @Override
    public void openGui(Player player, GuiType type, Object... data) {
        if (player instanceof AbstractClientPlayer) {
            openGuiClient(type, type.serializeArguments(data));
            return;
        }
        super.openGui(player, type, data);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        RegistryMenuTypes.initClient();
        RegistryBlockRenderTypes.initBlocks();
        RegistryBlockRenderTypes.initFluids();
        RegistryItems.registerItemProperties();
    }

    // 1.16.5 did this inline in onClientSetup:
    //   Map<String, PlayerRenderer> playerRenderMap = Minecraft.getInstance().getRenderManager().getSkinMap();
    //   playerRenderMap.get("slim"|"default").addLayer(new StarryLayerRenderer<>(renderer, slim));
    // 1.20.1 rebuilds the skin map on every resource reload, so Forge fires EntityRenderersEvent.AddLayers instead.
    private void onAddLayers(EntityRenderersEvent.AddLayers event) {
        PlayerRenderer renderer = event.getSkin("slim");
        renderer.addLayer(new StarryLayerRenderer<>(renderer, true));
        renderer = event.getSkin("default");
        renderer.addLayer(new StarryLayerRenderer<>(renderer, false));
    }

    private void addTomeBookmarks() {
        //AS-CLI-17 / AS-CLI-18 (resolved): both notes claimed the two screens were "still a stub without the
        //static factory". Stale - ScreenJournalProgression#getJournalInstance is at :75 and
        //ScreenJournalConstellationOverview#getConstellationScreen at :69, and BookmarkProvider's constructor is
        //unchanged. Without these two the tome had a single tab, so the whole Constellations section (~760 lines
        //of finished UI, including the 540-line ScreenJournalConstellationDetail reachable only from the overview)
        //had no entry point at all, and an unattuned player saw no tabs whatsoever.
        //Order and indices verbatim from 1.16.5 client/ClientProxy.java:194-204.
        ScreenJournal.addBookmark(new BookmarkProvider("screen.astralsorcery.tome.progression", 10,
                ScreenJournalProgression::getJournalInstance,
                () -> true));
        ScreenJournal.addBookmark(new BookmarkProvider("screen.astralsorcery.tome.constellations", 20,
                ScreenJournalConstellationOverview::getConstellationScreen,
                () -> !ResearchHelper.getClientProgress().getSeenConstellations().isEmpty()));
        ScreenJournal.addBookmark(new BookmarkProvider("screen.astralsorcery.tome.perks", 30,
                ScreenJournalPerkTree::new,
                () -> ResearchHelper.getClientProgress().isAttuned()));
    }

}
