/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static hellfirepvp.astralsorcery.common.lib.EntityTypesAS.*;

/** Common entity attribute bindings for EntityTypesAS. */
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryEntityTypes {

    private RegistryEntityTypes() {}

    @SubscribeEvent
    public static void initAttributes(EntityAttributeCreationEvent event) {
        // 1.16.5: AttributeModifierMap.MutableAttribute#create() -> 1.20.1: AttributeSupplier.Builder#build()
        event.put(FLARE.get(), EntityFlare.createAttributes().build());
        event.put(SPECTRAL_TOOL.get(), EntitySpectralTool.createAttributes().build());
    }

}
