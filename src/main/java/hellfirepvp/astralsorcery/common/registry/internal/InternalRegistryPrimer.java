/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.internal;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: InternalRegistryPrimer
 * Created by HellFirePvP
 * Date: 26.06.2017 / 14:49
 */
//BLOCKED_PORT[AS-REG-03]: InternalRegistryPrimer cannot be ported to 1.20.1 (1.16.5
// common/registry/internal/InternalRegistryPrimer.java:30-51). Every member of the 1.16.5 class is expressed in
// terms of net.minecraftforge.registries.IForgeRegistryEntry, which Forge deleted in 1.19:
//
//   private final Map<Class<?>, List<IForgeRegistryEntry<?>>> primed;
//   public <V extends IForgeRegistryEntry<V>> V register(V entry)          // uses entry.getRegistryType()
//   <T extends IForgeRegistryEntry<T>> List<?> getEntries(Class<T> type)
//   public <V extends IForgeRegistryEntry<V>> V getCached(IForgeRegistry<V> registry, ResourceLocation key)
//                                                                          // uses entry.getRegistryName()
//
// There is no 1.20.1 replacement for IForgeRegistryEntry#getRegistryType()/#getRegistryName(): registry objects no
// longer carry their own name or registry type, the name is supplied at registration time instead. The whole
// "prime entries now, flush them into the IForgeRegistry when RegistryEvent.Register fires" mechanism this class
// implements is exactly what net.minecraftforge.registries.DeferredRegister does natively in 1.20.1.
//
// The 1.20.1 target consequently has no primer at all:
//   - Vanilla-registry objects are created and deferred by the common/lib/*AS + common/crafting/*AS
//     DeferredRegister holders, all of which are handed to the mod event bus by
//     common/registry/RegistryRegistries#init(IEventBus).
//   - Mod-owned registries are created in RegistryRegistries#buildRegistries(NewRegistryEvent) and populated in
//     RegistryRegistries#registerBuiltInPerkAttributeReaders(RegisterEvent).
//   - AstralSorcery.getProxy().getRegistryPrimer() (the 1.16.5 access path into this class) does not exist in the
//     target and must not be reintroduced.
//
// Nothing is dropped silently: the per-type registration bodies the primer used to flush are listed in the
// BLOCKED_PORT comment of PrimerEventHandler in this package, together with where each one now lives.
// This class is intentionally left without members - adding any would create a second, dead registration path
// that competes with the DeferredRegister-based one.
public final class InternalRegistryPrimer {

    private InternalRegistryPrimer() {}
}
