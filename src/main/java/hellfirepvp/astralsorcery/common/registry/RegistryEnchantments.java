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
 * Class: RegistryEnchantments
 * Created by HellFirePvP
 * Date: 02.05.2020 / 12:43
 *
 * @see hellfirepvp.astralsorcery.common.loot.global.LootModifierScorchingHeat
 */
// Port notes for 1.16.5 common/registry/RegistryEnchantments.java (44 lines):
//
// Both registrations of init() (1.16.5 ref:32-37) already exist in common/lib/EnchantmentsAS as DeferredRegister
// entries of ForgeRegistries.ENCHANTMENTS, under the same two names:
//
//   NIGHT_VISION    (ref:33-34) -> EnchantmentsAS.NIGHT_VISION    ("astralsorcery:night_vision")
//   SCORCHING_HEAT  (ref:35-36) -> EnchantmentsAS.SCORCHING_HEAT  ("astralsorcery:scorching_heat")
//
//   register(T)     (ref:39-42) -> DeferredRegister#register; Enchantment#setRegistryName and the registry
//                                  primer are gone (IForgeRegistryEntry deleted in 1.19), so the name moved into
//                                  the register() call. net.minecraft.enchantment.Enchantment is
//                                  net.minecraft.world.item.enchantment.Enchantment.
//
// Repeating init() here would register both enchantments a second time, so this class is intentionally left
// without members. The scorching_heat enchantment is what data/astralsorcery/loot_modifiers/scorching_heat.json
// matches on - see the note on RegistryLoot for the loot-modifier half.
public final class RegistryEnchantments {

    private RegistryEnchantments() {}
}
