package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.container.ContainerAltarAttunement;
import hellfirepvp.astralsorcery.common.container.ContainerAltarConstellation;
import hellfirepvp.astralsorcery.common.container.ContainerAltarDiscovery;
import hellfirepvp.astralsorcery.common.container.ContainerAltarTrait;
import hellfirepvp.astralsorcery.common.container.ContainerObservatory;
import hellfirepvp.astralsorcery.common.container.ContainerTome;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Six menu types retain their existing serialized identifiers; screen bindings live in client.registry.RegistryMenuTypes.
public class MenuTypesAS {

    private MenuTypesAS() {}

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AstralSorcery.MODID);

    public static final RegistryObject<MenuType<ContainerTome>> TOME = MENU_TYPES.register("tome", () -> IForgeMenuType.create(ContainerTome::new));
    public static final RegistryObject<MenuType<ContainerObservatory>> OBSERVATORY = MENU_TYPES.register("observatory", () -> IForgeMenuType.create(ContainerObservatory::new));

    public static final RegistryObject<MenuType<ContainerAltarDiscovery>> ALTAR_DISCOVERY = MENU_TYPES.register("altar_discovery", () -> IForgeMenuType.create(ContainerAltarDiscovery::new));
    public static final RegistryObject<MenuType<ContainerAltarAttunement>> ALTAR_ATTUNEMENT = MENU_TYPES.register("altar_attunement", () -> IForgeMenuType.create(ContainerAltarAttunement::new));
    public static final RegistryObject<MenuType<ContainerAltarConstellation>> ALTAR_CONSTELLATION = MENU_TYPES.register("altar_constellation", () -> IForgeMenuType.create(ContainerAltarConstellation::new));
    public static final RegistryObject<MenuType<ContainerAltarTrait>> ALTAR_RADIANCE = MENU_TYPES.register("altar_radiance", () -> IForgeMenuType.create(ContainerAltarTrait::new));
}
