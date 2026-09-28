/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.perk.node.key.*;
import hellfirepvp.astralsorcery.common.perk.node.root.*;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeDodge;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeMiningSize;

import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryPerks
 * Created by HellFirePvP
 * Date: 25.07.2020 / 13:31
 */
// Port notes for 1.16.5 common/registry/RegistryPerks.java (58 lines):
// Pure config plumbing, no registry API involved - ported 1:1. All twenty-four ConfigEntry holders referenced
// below exist unchanged in the 1.20.1 tree.
//
//EXTERNAL: 1.16.5 CommonProxy#initializeConfigurations (1.16.5 CommonProxy.java:286) called
// RegistryPerks.initConfig(PerkConfig.CONFIG::newSubSection) right after addConfigEntry(PerkConfig.CONFIG);
// common/CommonProxy in the 1.20.1 tree does not, so the perk sub-sections are currently missing from the
// generated server config. That call is outside common/registry/** and is reported as an external change.
public class RegistryPerks {

    private RegistryPerks() {}

    public static void initConfig(Consumer<ConfigEntry> registrar) {
        registrar.accept(AttributeTypeMiningSize.CONFIG);
        registrar.accept(AttributeTypeDodge.CONFIG);

        registrar.accept(KeyBleed.CONFIG);
        registrar.accept(KeyCheatDeath.CONFIG);
        registrar.accept(KeyCullingAttack.CONFIG);
        registrar.accept(KeyDamageArmor.CONFIG);
        registrar.accept(KeyDamageEffects.CONFIG);
        registrar.accept(KeyDisarm.CONFIG);
        registrar.accept(KeyGrowables.CONFIG);
        registrar.accept(KeyLastBreath.CONFIG);
        registrar.accept(KeyLightningArc.CONFIG);
        registrar.accept(KeyMending.CONFIG);
        registrar.accept(KeyNoArmor.CONFIG);
        registrar.accept(KeyProjectileDistance.CONFIG);
        registrar.accept(KeyProjectileProximity.CONFIG);
        registrar.accept(KeyRampage.CONFIG);
        registrar.accept(KeySpawnLights.CONFIG);
        registrar.accept(KeyStoneEnrichment.CONFIG);
        registrar.accept(KeyVoidTrash.CONFIG);

        registrar.accept(RootAevitas.CONFIG);
        registrar.accept(RootArmara.CONFIG);
        registrar.accept(RootDiscidia.CONFIG);
        registrar.accept(RootEvorsio.CONFIG);
        registrar.accept(RootVicio.CONFIG);
    }
}
