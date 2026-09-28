/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistrySounds
 * Created by HellFirePvP
 * Date: 02.08.2019 / 21:20
 */
// Port notes for 1.16.5 common/registry/RegistrySounds.java (84 lines):
//
// The class is intentionally left without members: all twenty-seven sound events of 1.16.5 init() (ref:31-62)
// already exist in common/lib/SoundsAS as DeferredRegister entries, with byte-for-byte the same json names and
// the same SoundCategory -> SoundSource assignment (BLOCKS / MASTER / PLAYERS). Re-running init() here would
// register every sound a second time.
//
//   init()                        (1.16.5 ref:30-63) -> common/lib/SoundsAS static initialisers. Verified
//                                                       one-for-one: block_coloredlens_attach, craft_attunement,
//                                                       craft_finish, altar_craft_start, altar_craft_finish,
//                                                       altar_craft_loop_t1..t4, altar_craft_loop_t4_waiting,
//                                                       attunement_altar_idle_loop,
//                                                       attunement_altar_player_attune,
//                                                       attunement_altar_item_start/_finish/_loop,
//                                                       infuser_craft_start/_loop/_finish, perk_seal,
//                                                       perk_unseal, perk_unlock,
//                                                       illumination_wand_highlight/_unhighlight/_light,
//                                                       gui_journal_close, gui_journal_page.
//   registerSound(String, SoundCategory)
//                                 (ref:65-70)        -> common/lib/SoundsAS#reg(String, SoundSource). Two API
//                                                       changes: SoundCategory is SoundSource, and the public
//                                                       SoundEvent(ResourceLocation) constructor was removed in
//                                                       1.19.3 in favour of
//                                                       SoundEvent#createVariableRangeEvent(ResourceLocation)
//                                                       (the fixed-range variant is createFixedRangeEvent).
//                                                       Because SoundEvent is now a record-like final class,
//                                                       common/util/sound/CategorizedSoundEvent can no longer
//                                                       extend it: it wraps the RegistryObject<SoundEvent> plus
//                                                       the category instead, which is why SoundsAS exposes
//                                                       CategorizedSoundEvent constants rather than
//                                                       RegistryObjects.
//   registerSound(String)         (ref:72-77)        -> dropped: dead code in 1.16.5 as well (no caller), and
//                                                       there is no uncategorised sound in the mod.
//   registerSound(T)              (ref:79-82)        -> DeferredRegister#register; SoundEvent#setRegistryName and
//                                                       the registry primer are gone (IForgeRegistryEntry was
//                                                       deleted in 1.19).
public final class RegistrySounds {

    private RegistrySounds() {}
}
