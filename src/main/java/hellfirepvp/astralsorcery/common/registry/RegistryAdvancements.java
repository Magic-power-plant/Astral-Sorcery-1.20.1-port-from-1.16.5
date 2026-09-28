package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.advancement.AltarCraftTrigger;
import hellfirepvp.astralsorcery.common.advancement.AttuneCrystalTrigger;
import hellfirepvp.astralsorcery.common.advancement.AttuneSelfTrigger;
import hellfirepvp.astralsorcery.common.advancement.DiscoverConstellationTrigger;
import hellfirepvp.astralsorcery.common.advancement.PerkLevelTrigger;
import hellfirepvp.astralsorcery.common.lib.AdvancementsAS;
import net.minecraft.advancements.CriteriaTriggers;

public class RegistryAdvancements {

    public static void init() {
        AdvancementsAS.DISCOVER_CONSTELLATION = CriteriaTriggers.register(new DiscoverConstellationTrigger());
        AdvancementsAS.ATTUNE_SELF = CriteriaTriggers.register(new AttuneSelfTrigger());
        AdvancementsAS.ATTUNE_CRYSTAL = CriteriaTriggers.register(new AttuneCrystalTrigger());
        AdvancementsAS.ALTAR_CRAFT = CriteriaTriggers.register(new AltarCraftTrigger());
        AdvancementsAS.PERK_LEVEL = CriteriaTriggers.register(new PerkLevelTrigger());
    }

}
