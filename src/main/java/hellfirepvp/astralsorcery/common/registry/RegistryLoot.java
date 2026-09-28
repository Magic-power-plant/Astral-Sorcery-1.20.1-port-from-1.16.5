/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import hellfirepvp.astralsorcery.common.loot.global.LootModifierPerkVoidTrash;
import hellfirepvp.astralsorcery.common.loot.global.LootModifierScorchingHeat;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryLoot
 * Created by HellFirePvP
 * Date: 20.07.2019 / 21:48
 */
// Port notes for 1.16.5 common/registry/RegistryLoot.java (55 lines):
//
//   registerFunction(LootFunction.Serializer<T>, ResourceLocation) (ref:39-43, 46-48)
//                          -> already ported: common/lib/LootAS$Functions#register() performs the same five
//                             registrations, in the same order, under the same five keys (linear_luck_bonus,
//                             random_crystal_property, copy_crystal_properties, copy_constellation,
//                             copy_gateway_color), and common/CommonProxy#initialize calls it. The API moved:
//                             LootFunctionManager.func_237451_a_(String, Serializer) is
//                             Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ResourceLocation,
//                             new LootItemFunctionType(Serializer)) - LootFunctionType is LootItemFunctionType
//                             and it now wraps the serializer instead of being created from it. Repeating those
//                             calls here would throw on the duplicate keys.
//   registerGlobalModifier(GlobalLootModifierSerializer<T>, ResourceLocation) (ref:36-37, 50-53)
//                          -> NOT ported anywhere else in the 1.20.1 tree, so it is kept here. Forge deleted
//                             GlobalLootModifierSerializer and IForgeRegistryEntry in 1.19: a global loot
//                             modifier now contributes a Codec<? extends IGlobalLootModifier> (the CODEC field on
//                             LootModifierScorchingHeat / LootModifierPerkVoidTrash) to the
//                             GLOBAL_LOOT_MODIFIER_SERIALIZERS registry, and IGlobalLootModifier.DIRECT_CODEC
//                             dispatches on it. The registry primer is gone, so the two entries go in from
//                             RegisterEvent; this class subscribes itself to the mod bus because
//                             common/CommonProxy (outside this slice) has no RegistryLoot call.
//
// The keys below are the same two 1.16.5 used and are the ones listed in
// data/forge/loot_modifiers/global_loot_modifiers.json.
//
//AS-REG-15 (resolved): data/astralsorcery/loot_modifiers/scorching_heat.json and perk_void_trash.json were still
// in their 1.16.5 shape (a bare "conditions" array). 1.16.5 picked the serializer by file name; the 1.20.1
// codec-dispatch route needs an explicit "type" field, so both files now carry
// "type": "astralsorcery:scorching_heat" / "astralsorcery:perk_void_trash". Before that, Forge logged
// "Could not decode GlobalLootModifier with json id astralsorcery:... - Input does not contain a key [type]"
// at every datapack reload and neither modifier was ever applied.
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryLoot {

    private RegistryLoot() {}

    @SubscribeEvent
    public static void registerLootFunctions(RegisterEvent event) {
        event.register(Registries.LOOT_FUNCTION_TYPE, LootAS.Functions::register);
    }

    @SubscribeEvent
    public static void registerGlobalLootModifiers(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper -> {
            helper.register(AstralSorcery.key("scorching_heat"), LootModifierScorchingHeat.CODEC);
            helper.register(AstralSorcery.key("perk_void_trash"), LootModifierPerkVoidTrash.CODEC);
        });
    }
}
