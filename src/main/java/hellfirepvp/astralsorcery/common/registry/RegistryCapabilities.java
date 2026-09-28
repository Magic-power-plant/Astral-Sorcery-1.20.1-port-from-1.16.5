/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.ChunkFluidEntry;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryCapabilities
 * Created by HellFirePvP
 * Date: 19.07.2019 / 13:59
 */
// Port notes for 1.16.5 common/registry/RegistryCapabilities.java (98 lines):
//
//   init(IEventBus)              (1.16.5 ref:42-46)   -> the registerDefault half is ported below as a
//                                                        RegisterCapabilitiesEvent subscriber. The second half,
//                                                        eventBus.addGenericListener(Chunk.class, ..), already
//                                                        exists in the 1.20.1 target as
//                                                        common/event/handler/EventHandlerMisc#attachListeners ->
//                                                        bus.addGenericListener(LevelChunk.class,
//                                                        EventHandlerMisc::onAttachChunkCapabilities. It must NOT
//                                                        be repeated here: AttachCapabilitiesEvent#addCapability
//                                                        throws on a duplicate key, and CHUNK_FLUID_KEY would be
//                                                        added twice per chunk.
//   attachChunkCapability(..)    (ref:48-50)          -> common/event/handler/EventHandlerMisc
//                                                        #onAttachChunkCapabilities (see above).
//   registerDefault/register     (ref:52-58)          -> ported below. CapabilityManager.INSTANCE
//                                                        #register(Class, IStorage, Callable) was replaced in 1.19
//                                                        by RegisterCapabilitiesEvent#register(Class); the
//                                                        capability instance is looked up through
//                                                        CapabilityManager#get(CapabilityToken) instead, which
//                                                        common/lib/CapabilitiesAS#CHUNK_FLUID already does.
//   serializeableProvider(E)     (ref:60-81)          -> common/capability/ChunkFluidEntry.Provider, which is the
//                                                        same ICapabilitySerializable (getCapability comparing
//                                                        against CapabilitiesAS.CHUNK_FLUID + serializeNBT /
//                                                        deserializeNBT delegating to the entry). 1.16.5 handed it
//                                                        CHUNK_FLUID.getDefaultInstance(); Capability lost both
//                                                        #getDefaultInstance() and its factory in 1.19, so the
//                                                        Provider allocates its own ChunkFluidEntry - which also
//                                                        fixes the 1.16.5 behaviour of every chunk sharing the one
//                                                        default instance.
//   serializeableStorage()       (ref:83-96)          -> gone: Capability.IStorage was deleted in 1.19. NBT
//                                                        handling is entirely the provider's business now, and
//                                                        ChunkFluidEntry still implements
//                                                        INBTSerializable<CompoundTag>, so nothing is lost.
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryCapabilities {

    private RegistryCapabilities() {}

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 1.16.5: registerDefault(ChunkFluidEntry.class, ChunkFluidEntry::new) (ref:43)
        event.register(ChunkFluidEntry.class);
    }
}
