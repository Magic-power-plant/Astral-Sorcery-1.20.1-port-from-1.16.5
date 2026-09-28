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
 * Class: RegistryGameRules
 * Created by HellFirePvP
 * Date: 04.07.2019 / 18:53
 *
 * @see hellfirepvp.astralsorcery.common.lib.GameRulesAS
 */
// Port notes for 1.16.5 common/registry/RegistryGameRules.java (30 lines):
//
// The single statement of init() (1.16.5 ref:27) already exists, verbatim, in
// common/CommonProxy#onCommonSetup (common/CommonProxy.java:321-322):
//
//   GameRulesAS.IGNORE_SKYLIGHT_CHECK_RULE = GameRules.register("asIgnoreSkylightCheck",
//           GameRules.Category.UPDATES, GameRules.BooleanValue.create(false));
//
// Same rule name, same category, same default, same target field, so this class must not repeat it:
// GameRules#register appends to the static GAME_RULE_TYPES map under a freshly built GameRules.Key, so a second
// call would leave two distinct keys sharing the id "asIgnoreSkylightCheck" - two entries in every
// /gamerule listing and in every saved level.dat, with common/util/MiscUtils#canSeeSky (the only reader, via
// GameRulesAS.IGNORE_SKYLIGHT_CHECK_RULE) bound to whichever key was assigned last.
//
//   net.minecraft.world.GameRules           -> net.minecraft.world.level.GameRules
//   GameRules#register(String, Category, Type<T extends Value<T>>)
//                                           -> unchanged and still public (Forge widens it; verified against
//                                              forge-1.20.1-47.4.20_mapped_official_1.20.1.jar). Also unchanged:
//                                              GameRules.Category.UPDATES and
//                                              GameRules.BooleanValue#create(boolean).
//   GameRules.RuleKey<T>                    -> GameRules.Key<T>, which is why common/lib/GameRulesAS declares the
//                                              field as GameRules.Key<GameRules.BooleanValue>.
//
// 1.16.5 ran init() from CommonProxy#initialize (1.16.5 common/CommonProxy.java:165), i.e. during mod
// construction; 1.20.1 runs the same statement from FMLCommonSetupEvent instead, which is still before any
// ServerLevel (and therefore any GameRules instance) is created. Keeping it where it is also keeps it on the
// server-setup thread that Forge hands to the setup event.
public final class RegistryGameRules {

    private RegistryGameRules() {}
}
