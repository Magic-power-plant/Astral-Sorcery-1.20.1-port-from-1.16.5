/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.util.data.NameUtil;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.effect.mob.EffectCustomTexture;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryEffects
 * Created by HellFirePvP
 * Date: 26.08.2019 / 19:12
 */
// Port notes for 1.16.5 common/registry/RegistryEffects.java (41 lines):
//
// init() (1.16.5 ref:29-33) did three things per effect; two of them have moved:
//
//   new Effect* + registry primer (ref:30-32, 38)
//                    -> common/lib/EffectsAS, a DeferredRegister of ForgeRegistries.MOB_EFFECTS holding the same
//                       three entries. Effect is MobEffect in 1.20.1.
//   setRegistryName(NameUtil.fromClass(effect, "Effect")) (ref:36)
//                    -> gone with IForgeRegistryEntry (1.19). The three names NameUtil produced - "bleed",
//                       "cheat_death", "drop_modifier" - are spelled out in the EffectsAS register() calls.
//   attachEventListeners(MinecraftForge.EVENT_BUS) (ref:37)
//                    -> NOT ported anywhere else, so it is restored here. EffectCheatDeath#onDeath
//                       (LivingDeathEvent, EventPriority.LOW) and EffectDropModifier#onDrops (LivingDropsEvent,
//                       EventPriority.HIGH) are only ever hooked up by this call; without it both potion
//                       effects are inert. EffectBleed does not override it, exactly as in 1.16.5, and is still
//                       passed through so the three effects keep behaving uniformly.
//
// The attachment used to run inside the registry event; here it runs from FMLCommonSetupEvent, the first
// lifecycle point at which the RegistryObjects are guaranteed to be filled. The class subscribes itself to the
// mod bus because common/CommonProxy (outside this slice) has no RegistryEffects call - if one is ever added
// next to its RegistryMantleEffects.attachEventListeners(eventBus) line, the guard below keeps the listeners
// from being attached twice.
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryEffects {

    private static boolean listenersAttached = false;

    private RegistryEffects() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> attachEventListeners(MinecraftForge.EVENT_BUS));
    }

    public static void attachEventListeners(IEventBus bus) {
        if (listenersAttached) {
            return;
        }
        listenersAttached = true;

        attach(bus, EffectsAS.EFFECT_BLEED.get());
        attach(bus, EffectsAS.EFFECT_CHEAT_DEATH.get());
        attach(bus, EffectsAS.EFFECT_DROP_MODIFIER.get());
    }

    private static <T extends EffectCustomTexture> T attach(IEventBus bus, T effect) {
        effect.attachEventListeners(bus);
        return effect;
    }
}
