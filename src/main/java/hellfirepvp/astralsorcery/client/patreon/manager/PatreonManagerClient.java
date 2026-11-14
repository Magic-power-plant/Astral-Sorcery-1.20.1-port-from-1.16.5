/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.manager;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.patreon.ClientPatreonProviders;
import hellfirepvp.astralsorcery.client.patreon.effect.ClientPatreonEffect;
import hellfirepvp.astralsorcery.client.patreon.entity.PatreonEntityRenderer;
import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.data.sync.client.ClientPatreonFlares;
import hellfirepvp.astralsorcery.common.event.helper.CacheEventBus;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectDefinition;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectHelper;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonPartialEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import java.util.*;

/** Owns render listeners, local effect ticks and visual handles for one client world. */
public final class PatreonManagerClient implements ITickHandler {
    public static final PatreonManagerClient INSTANCE = new PatreonManagerClient();
    private final Map<UUID, PatreonEffectDefinition> definitions = new HashMap<>();
    private final Map<UUID, ClientPatreonEffect> effects = new HashMap<>();
    private final Map<PatreonPartialEntity, PatreonEntityRenderer> renderers = new IdentityHashMap<>();
    private final List<ITickHandler> tickListeners = new ArrayList<>();
    private final CacheEventBus eventBus = CacheEventBus.of(MinecraftForge.EVENT_BUS);
    private Level activeWorld;
    private PatreonManagerClient() {}

    public void clear() {
        renderers.values().forEach(PatreonEntityRenderer::close);
        renderers.clear();
        eventBus.unregisterAll();
        effects.values().forEach(ClientPatreonEffect::close);
        effects.clear();
        definitions.clear();
        tickListeners.clear();
        activeWorld = null;
    }

    private void synchronizeEffects() {
        Collection<PatreonEffectDefinition> current = PatreonEffectHelper.getEffects();
        if (current.size() == definitions.size()
                && current.stream().allMatch(d -> definitions.get(d.getEffectUUID()) == d)) return;
        clear();
        for (PatreonEffectDefinition definition : current) {
            definitions.put(definition.getEffectUUID(), definition);
            ClientPatreonEffect effect = null;
            try {
                effect = ClientPatreonProviders.create(definition);
                effect.initialize();
                effect.attachEventListeners(eventBus);
                effect.attachTickListeners(tickListeners::add);
                effects.put(definition.getEffectUUID(), effect);
            } catch (Exception exception) {
                if (effect != null) {
                    eventBus.unregister(effect);
                    tickListeners.remove(effect);
                    effect.close();
                }
                AstralSorcery.LOG.warn("Cannot create patreon visual {}", definition.getEffectUUID(), exception);
            }
        }
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Level world = Minecraft.getInstance().level;
        Player viewer = Minecraft.getInstance().player;
        if (world == null || viewer == null) { clear(); return; }
        if (activeWorld != world) clear();
        boolean enabled = RenderingConfig.CONFIG.patreonEffects.get();
        if (enabled) synchronizeEffects();
        else if (!effects.isEmpty() || !renderers.isEmpty()) clear();
        activeWorld = world;
        Vector3 viewerPos = Vector3.atEntityCenter(viewer);
        Set<PatreonPartialEntity> visible = Collections.newSetFromMap(new IdentityHashMap<>());
        SyncDataHolder.executeClient(SyncDataHolder.DATA_PATREON_FLARES, ClientPatreonFlares.class, data -> {
            for (Collection<PatreonPartialEntity> owned : data.getEntities()) {
                for (PatreonPartialEntity entity : owned) {
                    if (entity.isRemoved() || !world.dimension().equals(entity.getLastTickedDimension())) continue;
                    entity.tick(world);
                    if (!enabled || entity.getPos().distanceSquared(viewerPos)
                            > RenderingConfig.CONFIG.getMaxEffectRenderDistanceSq()) continue;
                    ClientPatreonEffect effect = effects.get(entity.getEffectUUID());
                    if (effect == null) continue;
                    visible.add(entity);
                    PatreonEntityRenderer renderer = renderers.computeIfAbsent(entity, effect::createRenderer);
                    renderer.tickClient();
                    renderer.tickEffects(world);
                }
            }
        });
        renderers.entrySet().removeIf(entry -> {
            if (visible.contains(entry.getKey())) return false;
            entry.getValue().close();
            return true;
        });
        if (!enabled) return;
        for (Player player : world.players()) {
            for (PatreonEffectDefinition definition : PatreonEffectHelper.getPatreonEffects(LogicalSide.CLIENT, player.getUUID())) {
                ClientPatreonEffect effect = effects.get(definition.getEffectUUID());
                if (effect != null) effect.doClientEffect(player);
            }
            for (ITickHandler listener : tickListeners) {
                if (listener.getHandledTypes().contains(TickEvent.Type.PLAYER) && listener.canFire(TickEvent.Phase.END)) {
                    listener.tick(TickEvent.Type.PLAYER, player, LogicalSide.CLIENT);
                }
            }
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() { return EnumSet.of(TickEvent.Type.CLIENT); }
    @Override
    public boolean canFire(TickEvent.Phase phase) { return phase == TickEvent.Phase.END; }
    @Override
    public String getName() { return "Patreon Flare Manager (Client)"; }
}
