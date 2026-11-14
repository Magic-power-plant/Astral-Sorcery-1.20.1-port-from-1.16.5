package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.common.capability.ChunkFluidEntry;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.effect.EffectDropModifier;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.item.ItemTome;
import hellfirepvp.astralsorcery.common.lib.CapabilitiesAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class EventHandlerMisc {

    public static void attachListeners(IEventBus bus) {
        bus.addListener(EventHandlerMisc::onSpawnEffectCloud);
        bus.addListener(EventHandlerMisc::onPlayerSleepEclipse);
        bus.addListener(EventHandlerMisc::onChunkLoad);
        bus.addListener(EventHandlerMisc::onLecternOpen);
        bus.addListener(EventHandlerMisc::onCrystalToss);
        bus.addGenericListener(LevelChunk.class, EventHandlerMisc::onAttachChunkCapabilities);
    }

    private static void onAttachChunkCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(CapabilitiesAS.CHUNK_FLUID_KEY, new ChunkFluidEntry.Provider());
    }

    private static void onCrystalToss(ItemTossEvent event) {
        if (!event.getPlayer().level().isClientSide()) {
            ItemStack thrown = event.getEntity().getItem();
            if (thrown.getItem() instanceof ItemCrystalBase) {
                event.getEntity().setThrower(event.getPlayer().getUUID());
            }
        }
    }

    private static void onLecternOpen(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        net.minecraft.world.level.block.entity.LecternBlockEntity lectern =
                MiscUtils.getBlockEntityAt(event.getLevel(), event.getPos(), net.minecraft.world.level.block.entity.LecternBlockEntity.class, false);
        if (lectern != null) {
            ItemStack contained = lectern.getBook();
            if (contained.getItem() instanceof ItemTome) {
                event.setCanceled(true);
                AstralSorcery.getProxy().openGui(event.getEntity(), GuiType.TOME);
            }
        }
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        ChunkAccess ch = event.getChunk();
        if (ch instanceof LevelChunk chunk && !event.getLevel().isClientSide()) {
            chunk.getCapability(CapabilitiesAS.CHUNK_FLUID).ifPresent(entry -> {
                if (!entry.isInitialized()) {
                    LevelAccessor w = event.getLevel();
                    if (w instanceof WorldGenLevel) {
                        long seed = ((WorldGenLevel) w).getSeed();
                        long chX = chunk.getPos().x;
                        long chZ = chunk.getPos().z;
                        seed ^= chX << 32;
                        seed ^= chZ;
                        entry.generate(seed);
                        chunk.setUnsaved(true);
                    }
                }
            });
        }
    }

    private static void onPlayerSleepEclipse(PlayerSleepInBedEvent event) {
        WorldContext ctx = SkyHandler.getContext(event.getEntity().level());
        if (ctx != null && ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
            if (event.getResultStatus() == null) {
                event.setResult(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
            }
        }
    }

    private static void onSpawnEffectCloud(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AreaEffectCloud cloud) {
            if (cloud.getPotion().getEffects().stream()
                    .anyMatch(instance -> instance.getEffect() instanceof EffectDropModifier)) {
                event.setCanceled(true);
            }
        }
    }
}
