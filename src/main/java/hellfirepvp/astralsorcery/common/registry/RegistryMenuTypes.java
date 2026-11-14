/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.client.screen.ScreenObservatory;
import hellfirepvp.astralsorcery.client.screen.container.ScreenContainerAltarAttunement;
import hellfirepvp.astralsorcery.client.screen.container.ScreenContainerAltarConstellation;
import hellfirepvp.astralsorcery.client.screen.container.ScreenContainerAltarDiscovery;
import hellfirepvp.astralsorcery.client.screen.container.ScreenContainerAltarRadiance;
import hellfirepvp.astralsorcery.client.screen.container.ScreenContainerTome;
import hellfirepvp.astralsorcery.common.container.ContainerObservatory;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Client screen bindings for the menu types declared by MenuTypesAS. */
public class RegistryMenuTypes {

    private RegistryMenuTypes() {}

    @OnlyIn(Dist.CLIENT)
    public static void initClient() {
        MenuScreens.register(MenuTypesAS.TOME.get(), ScreenContainerTome::new);
        MenuScreens.register(MenuTypesAS.OBSERVATORY.get(),
                (ContainerObservatory menu, Inventory playerInventory, Component name) -> new ScreenObservatory(menu));
        MenuScreens.register(MenuTypesAS.ALTAR_DISCOVERY.get(), ScreenContainerAltarDiscovery::new);
        MenuScreens.register(MenuTypesAS.ALTAR_ATTUNEMENT.get(), ScreenContainerAltarAttunement::new);
        MenuScreens.register(MenuTypesAS.ALTAR_CONSTELLATION.get(), ScreenContainerAltarConstellation::new);
        MenuScreens.register(MenuTypesAS.ALTAR_RADIANCE.get(), ScreenContainerAltarRadiance::new);
    }
}
