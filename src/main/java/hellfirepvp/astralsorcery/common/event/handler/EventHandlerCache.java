package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperInvulnerability;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperSpawnDeny;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperTemporaryFlight;
import hellfirepvp.astralsorcery.common.auxiliary.gateway.CelestialGatewayHandler;
import hellfirepvp.astralsorcery.common.data.config.entry.GeneralConfig;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.data.research.ResearchSyncHelper;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.PerkCooldownHelper;
import hellfirepvp.astralsorcery.common.perk.PerkEffectHelper;
import hellfirepvp.astralsorcery.common.perk.PerkLevelManager;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.perk.source.ModifierManager;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightTransmissionHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightUpdateHandler;
import hellfirepvp.astralsorcery.common.util.time.TimeStopController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class EventHandlerCache {

    private EventHandlerCache() {}

    public static void attachListeners(IEventBus eventBus) {
        eventBus.register(EventHandlerCache.class);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        SyncDataHolder.clearAll();
        PerkTree.PERK_TREE.clearCache(LogicalSide.CLIENT);
        PerkLevelManager.clearCache(LogicalSide.CLIENT);
        PerkAttributeHelper.clearClient();
        PerkAttributeType.clearCache(LogicalSide.CLIENT);
        PerkEffectHelper.clientClearAllPerks();
        ModifierManager.clearClientCache();
        PerkCooldownHelper.clearCache(LogicalSide.CLIENT);
        CelestialGatewayHandler.INSTANCE.updateClientCache(null);
        WorldSeedCache.clearClient();
        SkyHandler.getInstance().clientClearCache();
    }

    public static void onServerStart() {

    }

    public static void onServerStop() {
        SyncDataHolder.clearAll();
        PerkTree.PERK_TREE.clearCache(LogicalSide.SERVER);
        PerkAttributeHelper.clearServer();
        PerkAttributeType.clearCache(LogicalSide.SERVER);
        PerkCooldownHelper.clearCache(LogicalSide.SERVER);

        StarlightTransmissionHandler.getInstance().clearServer();
        StarlightUpdateHandler.getInstance().clearServer();
        EventHelperTemporaryFlight.clearServer();
        EventHelperSpawnDeny.clearServer();
        EventHelperInvulnerability.clearServer();
        ResearchHelper.clearServerCache();
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        LevelAccessor w = event.getLevel();
        if (w instanceof Level world) {
            SyncDataHolder.clearWorld(world.dimension());
            StarlightTransmissionHandler.getInstance().informWorldUnload(world);
            TimeStopController.onWorldUnload(world);
            SkyHandler.getInstance().informWorldUnload(world);
        }
    }

    @SubscribeEvent
    public static void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
            if (GeneralConfig.CONFIG.giveJournalOnJoin.get() && !progress.didReceiveTome()) {
                if (player.getInventory().add(new ItemStack(ItemsAS.TOME.get()))) {
                    ResearchManager.setTomeReceived(player);
                }
            }
            ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
            PerkEffectHelper.onPlayerConnectEvent(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EventHelperTemporaryFlight.onDisconnect(player);
            PerkEffectHelper.onPlayerDisconnectEvent(player);
            ModifierManager.onDisconnect(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original && event.getEntity() instanceof ServerPlayer newPlayer) {
            PerkEffectHelper.onPlayerCloneEvent(original, newPlayer);
        }
    }
}
