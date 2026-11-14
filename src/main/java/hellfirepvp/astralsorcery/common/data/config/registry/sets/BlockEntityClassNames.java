/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.config.registry.sets;

import java.util.Locale;
import java.util.Map;

/** Class-name compatibility for persisted acceleration-blacklist filters, not registry IDs. */
public final class BlockEntityClassNames {
    private static final Map<String, String> MOVED = Map.ofEntries(
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.AttunementAltarBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.altar.AttunementAltarBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.CelestialCrystalsBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.crystal.CelestialCrystalsBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.CelestialGatewayBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.gateway.CelestialGatewayBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.ChaliceBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.fountain.ChaliceBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.CollectorCrystalBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.crystal.CollectorCrystalBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.FountainBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.fountain.FountainBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.GemCrystalsBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.crystal.GemCrystalsBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.IlluminatorBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.illumination.IlluminatorBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.InfuserBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.infusion.InfuserBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.LensBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.starlight.LensBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.ObservatoryBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.observation.ObservatoryBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.PrismBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.starlight.PrismBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.RefractionTableBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.refraction.RefractionTableBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.RitualLinkBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.ritual.RitualLinkBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.RitualPedestalBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.ritual.RitualPedestalBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.SpectralRelayBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.starlight.SpectralRelayBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.TelescopeBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.observation.TelescopeBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.TranslucentBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.utility.TranslucentBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.TreeBeaconBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.treebeacon.TreeBeaconBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.TreeBeaconComponentBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.treebeacon.TreeBeaconComponentBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.VanishingBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.utility.VanishingBlockEntity"),
            Map.entry("hellfirepvp.astralsorcery.common.blockentity.WellBlockEntity", "hellfirepvp.astralsorcery.common.blockentity.starlight.WellBlockEntity"));
    private BlockEntityClassNames() {}

    public static String resolve(String configuredName) {
        return MOVED.getOrDefault(configuredName, configuredName);
    }

    public static boolean matches(String currentName, String configuredPrefix) {
        String prefix = configuredPrefix.toLowerCase(Locale.ROOT);
        if (prefix.isEmpty()) return false;
        if (currentName.toLowerCase(Locale.ROOT).startsWith(prefix)) return true;
        return MOVED.entrySet().stream().anyMatch(entry -> entry.getValue().equals(currentName)
                && entry.getKey().toLowerCase(Locale.ROOT).startsWith(prefix));
    }
}
