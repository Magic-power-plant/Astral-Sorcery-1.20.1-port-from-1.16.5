/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.container.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerTypesAS
 * Created by HellFirePvP
 * Date: 09.08.2019 / 21:22
 */
//TODO_PORT[AS-LIB-CT-01]: 1.16.5 declares these as plain mutable static ContainerType fields that
// common/registry/RegistryContainerTypes#init fills in during the registry event
// (1.16.5 common/lib/ContainerTypesAS.java:25-31). The 1.20.1 tree already registers the very same six
// menu types through a DeferredRegister in MenuTypesAS (registered from
// common/registry/RegistryRegistries.java, outside this slice), so declaring them a second time here would
// register every menu type twice. This class therefore aliases the MenuTypesAS RegistryObjects under the
// 1.16.5 names; the RegistryObject wrapper is the part that cannot be made identical to 1.16.5.
public class ContainerTypesAS {

    private ContainerTypesAS() {}

    public static final RegistryObject<MenuType<ContainerTome>> TOME = MenuTypesAS.TOME;
    public static final RegistryObject<MenuType<ContainerObservatory>> OBSERVATORY = MenuTypesAS.OBSERVATORY;

    public static final RegistryObject<MenuType<ContainerAltarDiscovery>> ALTAR_DISCOVERY = MenuTypesAS.ALTAR_DISCOVERY;
    public static final RegistryObject<MenuType<ContainerAltarAttunement>> ALTAR_ATTUNEMENT = MenuTypesAS.ALTAR_ATTUNEMENT;
    public static final RegistryObject<MenuType<ContainerAltarConstellation>> ALTAR_CONSTELLATION = MenuTypesAS.ALTAR_CONSTELLATION;
    public static final RegistryObject<MenuType<ContainerAltarTrait>> ALTAR_RADIANCE = MenuTypesAS.ALTAR_RADIANCE;

}
