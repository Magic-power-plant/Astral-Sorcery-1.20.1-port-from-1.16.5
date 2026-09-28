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

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryContainerTypes
 * Created by HellFirePvP
 * Date: 09.08.2019 / 21:15
 */
// Port notes for 1.16.5 common/registry/RegistryContainerTypes.java (74 lines):
//
//   init()                 (1.16.5 ref:39-47) -> common/lib/MenuTypesAS, a DeferredRegister of
//                                                ForgeRegistries.MENU_TYPES holding the same six entries under
//                                                the same six names (tome, observatory, altar_discovery,
//                                                altar_attunement, altar_constellation, altar_radiance).
//                                                Registering them again here would fail on the duplicate keys.
//   register(String/ResourceLocation, IContainerFactory<C>)
//                          (ref:64-73)        -> ContainerType is MenuType and new ContainerType<>(factory) is
//                                                IForgeMenuType.create(IContainerFactory) - the plain MenuType
//                                                constructor cannot carry the extra FriendlyByteBuf the AS
//                                                containers read. #setRegistryName and the registry primer are
//                                                gone (IForgeRegistryEntry deleted in 1.19); the six
//                                                Container*Provider.Factory classes of 1.16.5 are the
//                                                Container*::new (FriendlyByteBuf) constructors MenuTypesAS
//                                                passes.
//   initClient()           (ref:49-62)        -> kept below; ScreenManager is MenuScreens and
//                                                ScreenManager#registerFactory is MenuScreens#register.
//                                                common/../client/ClientProxy#onClientSetup still calls this, as
//                                                1.16.5 did.
//                                                ScreenManager.IScreenFactory<C, S> is
//                                                MenuScreens.ScreenConstructor<M, U>, same
//                                                (menu, inventory, title) -> screen shape, so the 1.16.5
//                                                anonymous class for the observatory (ref:52-57) is the lambda
//                                                below; ScreenObservatory still ignores the inventory and the
//                                                title and builds itself from the container alone.
//                                                PlayerInventory -> Inventory, ITextComponent -> Component.
public class RegistryContainerTypes {

    private RegistryContainerTypes() {}

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
